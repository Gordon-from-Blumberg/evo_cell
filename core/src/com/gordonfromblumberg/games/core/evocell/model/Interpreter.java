package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.*;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

import static com.gordonfromblumberg.games.core.common.utils.MathHelper.modPos;
import static com.gordonfromblumberg.games.core.evocell.model.Gene.geneValueCount;

public class Interpreter {
    private static final Logger log = LogManager.create(Interpreter.class);
    private static final byte expressionMarker;
    private static final int gotoLimit;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        expressionMarker = configManager.getByte("interpreter.expressionMarker");
        gotoLimit = configManager.getInteger("interpreter.gotoLimit");
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


    public Interpreter() { }

    public void run(GameWorld world, EvoLivingCell bot) {
        byte activeGeneIndex = bot.activeGeneIndex;

        Step geneActions = readGene(bot, activeGeneIndex);
        evaluatedGenes.add(activeGeneIndex);

        geneActions.run(world, bot);

        reset(geneActions);
    }

    void readActionsAsGroup(Step step, EvoLivingCell bot, int geneIndex, int geneValueIndex) {
        step.type = StepType.actionGroup;
        int lastRead = geneValueIndex - 1;
        while (lastRead + 1 < geneValueCount) {
            Step subStep = readAction(bot, geneIndex, lastRead + 1);
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
     * @return Step типа actionGroup со всеми считанными action внутри
     */
    Step readGene(EvoLivingCell bot, int geneIndex) {
        final int key = geneToKey(geneIndex);
        Step step = parsedGotos.get(key);
        if (step == null) {
            step = obtainStep(-1, -1);
            parsedGotos.put(key, step);
            readActionsAsGroup(step, bot, geneIndex, 0);
        }
        return step;
    }

    /**
     * Считывает action с указанной позиции, включая все необходимые параметры.
     * Если ген заканчивается, не считанные параметры заполняются значениями по умолчанию.
     * @param bot Бот
     * @param geneIndex Индекс гена
     * @param geneValueIndex Позиция в гене
     * @return Объект Step со считанным action
     */
    Step readAction(EvoLivingCell bot, int geneIndex, int geneValueIndex) {
        final Step step = obtainStep(geneIndex, geneValueIndex);

        final Gene gene = bot.dna.getGene(geneIndex);

        ActionDef actionDef = null;
        byte value = 0;
        while (actionDef == null && geneValueIndex < geneValueCount) {
            value = gene.getValue(geneValueIndex++);
            actionDef = Actions.actionDefs.get(value);
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
                            Step trueAction = readAction(bot, geneIndex, step.lastRead + 1);
                            step.addParameter(condition);
                            step.addParameter(trueAction);
                            if (trueAction.lastRead + 1 < geneValueCount) {
                                Step falseAction = readAction(bot, geneIndex, trueAction.lastRead + 1);
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
                                readActionsAsGroup(subStep, bot, geneIndex, index);
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
                            step.addParameter(readGene(bot, newGeneIndex));
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
                        Step subStep = readAction(bot, geneIndex, step.lastRead + 1);
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

        step.fillParametersByDefault();
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
                step.value = exprValue;
                step.lastRead = lastRead;
                step.fillParametersByDefault();
            }
        } else {
            step.value = value;
            step.lastRead = geneValueIndex - 1;
        }
        return step;
    }

    Step obtainStep(int geneIndex, int geneValueIndex) {
        Step step = stepPool.obtain();
        step.geneIndex = geneIndex;
        step.geneValueIndex = geneValueIndex;
        step.wasReset = false;
        return step;
    }

    boolean check(EvoLivingCell bot) {
        return bot.hp > 0 && bot.energy > 0 && bot.organics > 0;
    }

    void reset(Step step) {
        parsedGotos.clear();
        evaluatedGenes.clear();
        evaluatedGotos.clear();
        stepPool.free(step);
    }

    private static int geneToKey(int geneIndex) {
        return (geneIndex + 1) << 24;
    }

    private static int gotoToKey(int geneIndex, int geneValueIndex) {
        return (geneIndex << 8) | geneValueIndex;
    }

    private class Step implements Pool.Poolable, ExpressionMapping.ExpressionParameter {
        private final Array<Step> parameters = new Array<>(4);

        byte value;
        StepType type;
        Object stepDef;
        int geneIndex;
        int geneValueIndex;
        int lastRead;
        boolean stopReadActions;
        boolean wasReset;

        /**
         * @param world GameWorld
         * @param bot EvoLivingCell bot
         * @return if true actions running should be stopped
         */
        boolean run(GameWorld world, EvoLivingCell bot) {
            if (type == null) {
                return false;
            }

            switch (type) {
                case action -> {
                    ActionDef actionDef = (ActionDef) stepDef;
                    ActionMapping actionMapping = Actions.actionsMap.get(actionDef.name());
                    int parameter = parameters.size > 0 ? parameters.get(0).number() : 0;
                    actionMapping.act(world, bot, parameter);
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
                        if (subAction.run(world, bot)) {
                            return true;
                        }
                    }
                }

                case ifStatement -> {
                    if (parameters.size < 2) {
                        return false;
                    }

                    return parameters.get(0).bool()
                            ? parameters.get(1).run(world, bot)
                            : parameters.size > 2 && parameters.get(2).run(world, bot);
                }

                case gotoStatement -> {
                    ActionDef actionDef = (ActionDef) stepDef;
                    switch (actionDef.name()) {
                        case "goto" -> {
                            final int key = gotoToKey(geneIndex, geneValueIndex);
                            if (evaluatedGotos.getAndIncrement(key, 0, 1) < gotoLimit) {
                                parameters.get(0).run(world, bot);
                                return true;
                            }
                        }
                        case "gotoGene" -> {
                            if (evaluatedGenes.add(value)) {
                                parameters.get(0).run(world, bot);
                                return true;
                            }
                        }
                    }
                }

                case expression ->
                        throw new IllegalStateException("run should not be invoked for expression step");
            }

            return false;
        }

        void addParameter(Step step) {
           parameters.add(step);
        }

        void fillParametersByDefault() {
            if (stepDef instanceof ActionDef actionDef && actionDef.parameters() != null) {
                for (int i = parameters.size, n = actionDef.parameters().length; i < n; ++i) {
                    Step parameterStep = obtainStep(-1, -1);
                    parameterStep.type = StepType.expression;
                    parameterStep.value = actionDef.parameters()[i].defaultValue().get();
                    addParameter(parameterStep);
                }

            } else if (stepDef instanceof ExpressionDef expressionDef) {
                for (int i = parameters.size, n = expressionDef.defaultParameters().length; i < n; ++i) {
                    Step parameterStep = obtainStep(-1, -1);
                    parameterStep.type = StepType.expression;
                    parameterStep.value = expressionDef.defaultParameters()[i];
                    addParameter(parameterStep);
                }
            }
        }

        @Override
        public boolean bool() {
            return number() > 0;
        }

        @Override
        public int number() {
            if (stepDef instanceof ExpressionDef exprDef) {
                ExpressionMapping expression = Expressions.expressionsMap.get(exprDef.name());
                return expression.getValue(parameters.get(0), parameters.get(1));
            } else if (type == StepType.expression) {
                return value;
            } else {
                throw new IllegalStateException("number should not be invoked on " + type + " step with stepDef " + stepDef);
            }
        }

        @Override
        public void reset() {
            if (!wasReset) {
                wasReset = true;
                for (Step subStep : this.parameters) {
                    if (!subStep.wasReset) {
                        stepPool.free(subStep);
                    }
                }
                this.parameters.clear();
                this.value = 0;
                this.type = null;
                this.stepDef = null;
                this.geneIndex = -1;
                this.geneValueIndex = -1;
                this.stopReadActions = false;
            }
        }

        @Override
        public String toString() {
            return geneIndex + "_" + geneValueIndex + "_" + type + "_" + stepDefToString();
        }

        private String stepDefToString() {
            return stepDef instanceof ActionDef a ? a.name()
                    : stepDef instanceof ExpressionDef e ? e.name() : "";
        }
    }

    private enum StepType {
        action,
        expression,
        ifStatement,
        gotoStatement,
        actionGroup
    }
}
