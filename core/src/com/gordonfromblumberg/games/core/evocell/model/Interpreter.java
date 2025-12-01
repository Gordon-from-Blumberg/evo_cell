package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.StringBuilder;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

import static com.gordonfromblumberg.games.core.common.utils.MathHelper.modPos;
import static com.gordonfromblumberg.games.core.evocell.model.Gene.geneValueCount;
import static com.gordonfromblumberg.games.core.evocell.model.Step.StepType;

public class Interpreter {
    private static final Logger log = LogManager.create(Interpreter.class);
    private static final byte expressionMarker;
    private static final int gotoLimit;
    private static final IntMap<String> indents = new IntMap<>();

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        expressionMarker = configManager.getByte("interpreter.expressionMarker");
        gotoLimit = configManager.getInteger("interpreter.gotoLimit");

        final String baseIndent = "  ";
        indents.put(0, "");
        for (int i = 1; i < 16; ++i) {
            indents.put(i, indents.get(i - 1) + baseIndent);
        }
    }

    private final Pool<Step> stepPool = new Pool<>() {
        @Override
        protected Step newObject() {
            return new Step();
        }
    };

    // gotoGene: geneIndex << 24
    // goto : (geneIndex << 8) | geneValueIndex
    private final IntMap<Step> parsedGotos = new IntMap<>();
    private final IntSet evaluatedGenes = new IntSet();
    private final IntIntMap evaluatedGotos = new IntIntMap();
    private final IntSet printedGotos = new IntSet();
    private final ObjectIntMap<String> actionCounter = new ObjectIntMap<>();

    public Interpreter() { }

    public void run(GameWorld world, EvoLivingCell bot) {
        byte activeGeneIndex = bot.activeGeneIndex;

        Step geneActions = readGene(bot, activeGeneIndex, Actions.actionDefs);

        evaluatedGenes.add(activeGeneIndex);
        run(world, bot, geneActions);

        reset(geneActions);
    }

    public void runEmbryo(GameWorld world, EvoLivingCell bot) {
        Step geneActions = readGene(bot, 0, Actions.embryoActionDefs);

        evaluatedGenes.add(0);
        run(world, bot, geneActions);

        reset(geneActions);
    }

    public String print(EvoLivingCell bot) {
        Step embryoActions = readGene(bot, 0, Actions.embryoActionDefs);
        final StringBuilder sb = new StringBuilder("Embryo gene #0 {\n");
        for (Step stepAction : embryoActions.parameters()) {
            printStep(sb, stepAction, 0);
        }
        sb.append("}\n\n");

        byte activeGeneIndex = bot.activeGeneIndex;
        Step geneActions = readGene(bot, activeGeneIndex, Actions.actionDefs);
        sb.append("Active gene #").append(activeGeneIndex).append(" {\n");

        for (Step stepAction : geneActions.parameters()) {
            printStep(sb, stepAction, 0);
        }
        sb.append("}\n");

//        for (Step geneStep : genesToPrint) {
//
//        }

        reset(geneActions);
        return sb.toString();
    }

    void readActionsAsGroup(Step step, EvoLivingCell bot, int geneIndex, int geneValueIndex, IntMap<ActionDef> actionMap) {
        step.type = StepType.actionGroup;
        int lastRead = geneValueIndex - 1;
        while (lastRead + 1 < geneValueCount) {
            Step subStep = readAction(bot, geneIndex, lastRead + 1, actionMap);
            lastRead = subStep.lastRead;
            step.addParameter(subStep);
            if (subStep.stopReadActions) {
                step.stopReadActions = true;
                break;
            }
        }
    }

    /**
     * Считывает весь ген, помещая полученные action как parameters в step с типом actionGroup.
     * Если ген уже был считан (есть в evaluatedGenes), возвращает его.
     * @param bot Бот
     * @param geneIndex Индекс гена
     * @param actionMap Соответствие кодов и действий
     * @return Step типа actionGroup со всеми считанными action внутри
     */
    Step readGene(EvoLivingCell bot, int geneIndex, IntMap<ActionDef> actionMap) {
        final int key = geneToKey(geneIndex);
        Step step = parsedGotos.get(key);
        if (step == null) {
            step = obtainStep(-1, -1);
            parsedGotos.put(key, step);
            readActionsAsGroup(step, bot, geneIndex, 0, actionMap);
        }
        return step;
    }

    /**
     * Считывает action с указанной позиции, включая все необходимые параметры.
     * Если ген заканчивается, не считанные параметры заполняются значениями по умолчанию.
     * @param bot Бот
     * @param geneIndex Индекс гена
     * @param geneValueIndex Позиция в гене
     * @param actionMap Соответствие кодов и действий
     * @return Объект Step со считанным action
     */
    Step readAction(EvoLivingCell bot, int geneIndex, int geneValueIndex, IntMap<ActionDef> actionMap) {
        final Step step = obtainStep(geneIndex, geneValueIndex);

        final Gene gene = bot.dna.getGene(geneIndex);

        ActionDef actionDef = null;
        byte value = 0;
        while (actionDef == null && geneValueIndex < geneValueCount) {
            value = gene.getValue(geneValueIndex++);
            actionDef = actionMap.get(value);
        }

        step.lastRead = geneValueIndex - 1;
        step.value = value;
        if (actionDef != null) {
            step.stepDef = actionDef;

            switch (actionDef.type()) {
                case spec -> {
                    switch (actionDef.name()) {
                        case "if" -> {
                            step.type = StepType.ifStatement;
                            if (step.lastRead + 1 >= geneValueCount) {
                                break;
                            }
                            Step condition = readParameter(gene, geneIndex, step.lastRead + 1);
                            step.lastRead = condition.lastRead;
                            if (step.lastRead + 1 >= geneValueCount) {
                                break;
                            }
                            Step trueAction = readAction(bot, geneIndex, step.lastRead + 1, actionMap);
                            step.addParameter(condition);
                            step.addParameter(trueAction);
                            if (trueAction.lastRead + 1 < geneValueCount) {
                                Step falseAction = readAction(bot, geneIndex, trueAction.lastRead + 1, actionMap);
                                step.addParameter(falseAction);
                                step.lastRead = falseAction.lastRead;
                                if (trueAction.stopReadActions && falseAction.stopReadActions) {
                                    step.stopReadActions = true;
                                }
                            } else {
                                step.lastRead = trueAction.lastRead;
                            }
                        }
                        case "goto" -> {
                            step.type = StepType.gotoStatement;
                            step.stopReadActions = true;
                            int index = geneValueIndex < geneValueCount
                                    ? modPos(gene.getValue(geneValueIndex), geneValueCount)
                                    : 0;
                            step.value = (byte) index;
                            int key = gotoToKey(geneIndex, geneValueIndex - 1);
                            Step subStep = parsedGotos.get(key);
                            if (subStep == null) {
                                subStep = obtainStep(-1, -1);
                                parsedGotos.put(key, subStep);
                                readActionsAsGroup(subStep, bot, geneIndex, index, actionMap);
                            }
                            step.addParameter(subStep);
                        }
                        case "gotoGene" -> {
                            step.type = StepType.gotoStatement;
                            step.stopReadActions = true;
                            int newGeneIndex = geneValueIndex < geneValueCount
                                    ? modPos(gene.getValue(geneValueIndex), bot.dna.genes.size)
                                    : modPos(geneIndex + 1, bot.dna.genes.size);
                            step.value = (byte) newGeneIndex;
                            step.addParameter(readGene(bot, newGeneIndex, actionMap));
                        }
                        case "stop" -> {
                            step.type = StepType.action;
                            step.stopReadActions = true;
                        }
                    }
                }

                case specaction -> {
                    step.type = StepType.actionGroup;
                    int requiredSubActions = switch (actionDef.name()) {
                        case "2actions" -> 2;
                        case "3actions" -> 3;
                        default -> throw new IllegalStateException("Unknown specaction " + actionDef.name());
                    };
                    while (requiredSubActions-- > 0 && step.lastRead + 1 < geneValueCount) {
                        Step subStep = readAction(bot, geneIndex, step.lastRead + 1, actionMap);
                        step.lastRead = subStep.lastRead;
                        step.addParameter(subStep);
                        if (subStep.stopReadActions) {
                            step.stopReadActions = true;
                            break;
                        }
                    }
                }

                case action -> {
                    step.type = StepType.action;
                    int requiredParameters = actionDef.parameters() == null ? 0 : actionDef.parameters().length;
                    while (requiredParameters-- > 0 && step.lastRead + 1 < geneValueCount) {
                        Step subStep = readParameter(gene, geneIndex, step.lastRead + 1);
                        step.lastRead = subStep.lastRead;
                        step.addParameter(subStep);
                    }
                }
            }
        }

        fillParametersByDefault(step);
        return step;
    }

    Step readParameter(Gene gene, int geneIndex, int geneValueIndex) {
        Step step = obtainStep(geneIndex, geneValueIndex);
        step.type = StepType.expression;
        byte value = gene.getValue(geneValueIndex++);
        if (value < expressionMarker && geneValueIndex < geneValueCount) {
            byte exprValue = gene.getValue(geneValueIndex++);
            ExpressionDef expressionDef = Expressions.expressionDefs.get(exprValue);
            if (expressionDef != null) {
                int requiredParameters = expressionDef.defaultParameters().length;
                int lastRead = geneValueIndex - 1;
                while (requiredParameters-- > 0 && lastRead + 1 < geneValueCount) {
                    Step subStep = readParameter(gene, geneIndex, lastRead + 1);
                    lastRead = subStep.lastRead;
                    step.addParameter(subStep);
                }
                step.stepDef = expressionDef;
                step.value = exprValue;
                step.lastRead = lastRead;
                fillParametersByDefault(step);
            }
        } else {
            step.value = value;
            step.lastRead = geneValueIndex - 1;
        }
        return step;
    }

    /**
     * @param world GameWorld
     * @param bot EvoLivingCell bot
     * @param step Action step
     * @return if true actions running should be stopped
     */
    boolean run(GameWorld world, EvoLivingCell bot, Step step) {
        if (step.type == null) {
            return false;
        }

        final Array<Step> parameters = step.parameters();
        switch (step.type) {
            case action -> {
                ActionDef actionDef = (ActionDef) step.stepDef;
                ActionMapping actionMapping = Actions.actionsMap.get(actionDef.name());
                int counter = actionCounter.getAndIncrement(actionDef.tag(), 0, 1);
                int parameter = parameters.size > 0 ? parameters.get(0).number(world.getGrid(), bot) : 0;
                actionMapping.act(world, bot, counter, parameter);
                if (!check(bot)) {
                    bot.die();
                    return true;
                }
                if ("stop".equals(actionDef.name())) {
                    return true;
                }
            }

            case actionGroup -> {
                for (Step subAction : parameters) {
                    if (run(world, bot, subAction)) {
                        return true;
                    }
                }
            }

            case ifStatement -> {
                if (parameters.size < 2) {
                    return false;
                }

                return parameters.get(0).bool(world.getGrid(), bot)
                        ? run(world, bot, parameters.get(1))
                        : parameters.size > 2 && run(world, bot, parameters.get(2));
            }

            case gotoStatement -> {
                ActionDef actionDef = (ActionDef) step.stepDef;
                switch (actionDef.name()) {
                    case "goto" -> {
                        final int key = Interpreter.gotoToKey(step.geneIndex, step.geneValueIndex);
                        if (evaluatedGotos.getAndIncrement(key, 0, 1) < Interpreter.gotoLimit) {
                            run(world, bot, parameters.get(0));
                            return true;
                        }
                    }
                    case "gotoGene" -> {
                        if (evaluatedGenes.add(step.value)) {
                            run(world, bot, parameters.get(0));
                            return true;
                        }
                    }
                }
            }

            case expression -> throw new IllegalStateException("run should not be invoked for expression step");
        }

        return false;
    }

    Step obtainStep(int geneIndex, int geneValueIndex) {
        Step step = stepPool.obtain();
        step.geneIndex = geneIndex;
        step.geneValueIndex = geneValueIndex;
        step.wasReset = false;
        return step;
    }

    void fillParametersByDefault(Step step) {
        if (step.stepDef instanceof ActionDef actionDef && actionDef.parameters() != null) {
            for (int i = step.parameters().size, n = actionDef.parameters().length; i < n; ++i) {
                Step parameterStep = obtainStep(-1, -1);
                parameterStep.type = StepType.expression;
                parameterStep.value = actionDef.parameters()[i].defaultValue().get();
                step.addParameter(parameterStep);
            }

        } else if (step.stepDef instanceof ExpressionDef expressionDef) {
            for (int i = step.parameters().size, n = expressionDef.defaultParameters().length; i < n; ++i) {
                Step parameterStep = obtainStep(-1, -1);
                parameterStep.type = StepType.expression;
                parameterStep.value = expressionDef.defaultParameters()[i];
                step.addParameter(parameterStep);
            }
        }
    }

    void printStep(StringBuilder sb, Step step, int indent) {
        switch (step.type) {
            case action -> {
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent))
                  .append(((ActionDef) step.stepDef).description());
                if (step.parameters().notEmpty()) {
                    sb.append(" {\n");
                    for (Step parStep : step.parameters()) {
                        printStep(sb, parStep, indent + 1);
                    }
                    sb.append(step.geneValueIndex).append('\t')
                      .append(step.value).append(' ').append(indents.get(indent)).append("}");
                }
                sb.append('\n');
            }

            case actionGroup -> {
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent))
                  .append("group of actions {\n");
                for (Step subAction : step.parameters()) {
                    printStep(sb, subAction, indent + 1);
                }
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent))
                  .append("}\n");
            }

            case ifStatement -> {
                if (step.parameters().size < 2)
                    return;
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent))
                  .append("if (\n");
                printStep(sb, step.parameters().first(), indent + 1);
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent))
                  .append(") {\n");
                printStep(sb, step.parameters().get(1), indent + 1);
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent))
                  .append("}\n");
                if (step.parameters().size > 2) {
                    sb.append(step.geneValueIndex).append('\t')
                      .append(step.value).append(' ').append(indents.get(indent))
                      .append("else {\n");
                    printStep(sb, step.parameters().get(2), indent + 1);
                    sb.append(step.geneValueIndex).append('\t')
                      .append(step.value).append(' ').append(indents.get(indent))
                      .append("}\n");
                }
            }

            case gotoStatement -> {
                ActionDef actionDef = (ActionDef) step.stepDef;
                switch (actionDef.name()) {
                    case "goto" -> {
                        sb.append(step.geneValueIndex).append('\t')
                          .append(step.value).append(' ').append(indents.get(indent))
                          .append(actionDef.description()).append(' ').append(step.value);
                        final int key = Interpreter.gotoToKey(step.geneIndex, step.geneValueIndex);
                        if (printedGotos.add(key)) {
                            sb.append(" {\n");
                            for (Step subAction : step.parameters()) {
                                printStep(sb, subAction, indent + 1);
                            }
                            sb.append(step.geneValueIndex).append('\t')
                              .append(step.value).append(' ').append(indents.get(indent))
                              .append("}");
                        }
                        sb.append('\n');
                    }
                    case "gotoGene" -> sb.append(step.geneValueIndex).append('\t')
                                     .append(step.value).append(' ').append(indents.get(indent))
                                     .append(actionDef.description()).append(' ').append(step.value)
                                     .append('\n');
                }
            }

            case expression -> {
                ExpressionDef exprDef = (ExpressionDef) step.stepDef;
                sb.append(step.geneValueIndex).append('\t')
                  .append(step.value).append(' ').append(indents.get(indent));
                if (exprDef != null) {
                    sb.append(exprDef.name()).append(" (\n");
                    for (Step parStep : step.parameters()) {
                        printStep(sb, parStep, indent + 1);
                    }
                    sb.append(step.geneValueIndex).append('\t')
                      .append(step.value).append(' ').append(indents.get(indent))
                      .append(')');
                } else {
                    sb.append(step.value);
                }
                sb.append('\n');
            }
        }
    }

    boolean check(EvoLivingCell bot) {
        return bot.hp > 0 && bot.energy > 0 && bot.organics > 0;
    }

    private void reset(Step step) {
        parsedGotos.clear();
        evaluatedGenes.clear();
        evaluatedGotos.clear();
        printedGotos.clear();
        actionCounter.clear();
        free(step);
    }

    private void free(Step step) {
        if (!step.wasReset) {
            step.wasReset = true;
            for (Step subStep : step.parameters()) {
                if (!subStep.wasReset) {
                    free(subStep);
                }
            }
            stepPool.free(step);
        }
    }

    private static int geneToKey(int geneIndex) {
        return (geneIndex + 1) << 24;
    }

    private static int gotoToKey(int geneIndex, int geneValueIndex) {
        return (geneIndex << 8) | geneValueIndex;
    }


}
