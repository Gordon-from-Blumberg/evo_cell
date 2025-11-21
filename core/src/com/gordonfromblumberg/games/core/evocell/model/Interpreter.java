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

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        expressionMarker = configManager.getByte("interpreter.expressionMarker");
    }

    private final Pool<Step> stepPool = new Pool<>() {
        @Override
        protected Step newObject() {
            return new Step();
        }
    };

    private final Queue<Step> stack = new Queue<>();
    private final IntSet evaluatedGenes = new IntSet();

    public Interpreter() {

    }

    public void run(GameWorld world, EvoLivingCell bot) {


        reset();
    }

    void run(GameWorld world, EvoLivingCell bot, byte geneIndex, byte geneValueIndex, Step step) {
        ValueType expectedType = ValueType.action;
        byte activeGeneIndex = geneIndex;
        Gene activeGene = bot.dna.getGene(activeGeneIndex);
        byte i = (byte) (geneValueIndex - 1);

        while (++i < geneValueCount) {
            byte value = activeGene.getValue(i);
            Step newStep;
            if (expectedType == ValueType.action) {
                ActionDef actionDef = Actions.actionDefs.get(value);
                if (actionDef == null) {
                    continue;
                }

                newStep = obtainActionStep(activeGeneIndex, i, actionDef);
                switch (actionDef.type()) {
                    case spec:
                        switch (actionDef.name()) {
                            case "if" -> {
                                newStep.type = StepType.ifStatement;
                                newStep.requiredParameters = 3;
                                expectedType = ValueType.parameter;
                            }
                            case "goto" -> {
                                newStep.type = StepType.gotoStatement;
                                prevSpec = newStep;
                                i = (byte) modPos(activeGene.getValue(modPos(i + 1, geneValueCount)), geneValueCount);
                            }
                            case "gotoGene" -> {
                                newStep.type = StepType.gotoStatement;
                                prevSpec = newStep;
                                activeGeneIndex = (byte) modPos(activeGene.getValue(modPos(i + 1, geneValueCount)),
                                                                bot.dna.genes.size);
                                activeGene = bot.dna.getGene(activeGeneIndex);
                            }
                            case "activateAndGotoGene" -> {
                                newStep.type = StepType.spec;
                                prevSpec = newStep;
                                activeGeneIndex = (byte) modPos(activeGene.getValue(modPos(i + 1, geneValueCount)),
                                                                bot.dna.genes.size);
                                activeGene = bot.dna.getGene(activeGeneIndex);
                                bot.activeGeneIndex = activeGeneIndex;
                            }
                            default -> throw new IllegalStateException("Unknown spec instruction " + actionDef.name());
                        }
                        break;

                    case specaction:
                        newStep.parameterType = StepType.action;
                        newStep.requiredParameters = switch (actionDef.name()) {
                            case "2actions" -> 2;
                            case "3actions" -> 3;
                            default -> throw new IllegalStateException("Unknown specaction " + actionDef.name());
                        };
                        expectedType = ValueType.action;
                        break;

                    case action:
                        newStep.parameterType = StepType.expression;
                        newStep.requiredParameters = actionDef.parameters() == null ? 0 : actionDef.parameters().length;
                        break;
                }
            }

            if (!newStep.isReady()) {
                stack.addLast(newStep);
            } else {

            }
        }
    }

    void readActionsToStep(Step step, EvoLivingCell bot, int geneIndex, int geneValueIndex) {
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
     * Считывает весь ген, помещая полученные action как parameters в указанный step.
     * Если ген уже был считан (есть в evaluatedGenes), ничего не делает.
     * @param step Объект Step, в который сохраняются считанные действия
     * @param bot Бот
     * @param geneIndex Индекс гена
     */
    void readGene(Step step, EvoLivingCell bot, int geneIndex) {
        if (!evaluatedGenes.add(geneIndex)) {
            return;
        }

        readActionsToStep(step, bot, geneIndex, 0);
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
        while (actionDef == null && geneValueIndex < geneValueCount) {
            byte value = gene.getValue(geneValueIndex++);
            actionDef = Actions.actionDefs.get(value);
        }

        step.lastRead = geneValueIndex - 1;
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
                            readActionsToStep(step, bot, geneIndex, index);
                        }
                        case "gotoGene" -> {
                            step.type = StepType.gotoStatement;
                            step.stopReadActions = true;
                            int newGeneIndex = geneValueIndex < geneValueCount
                                    ? modPos(gene.getValue(geneValueIndex), bot.dna.genes.size)
                                    : modPos(geneIndex + 1, bot.dna.genes.size);
                            readGene(step, bot, newGeneIndex);
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
        return step;
    }

    boolean check(EvoLivingCell bot) {
        return bot.hp > 0 && bot.energy > 0 && bot.organics > 0;
    }

    void reset() {
        while (stack.notEmpty()) {
            stepPool.free(stack.removeLast());
        }
        evaluatedGenes.clear();
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

        void run(GameWorld world, EvoLivingCell bot) {
            if (type == StepType.expression) {
                throw new IllegalStateException("run should not be invoked for expression step");
            }

            //todo
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
            stepPool.freeAll(this.parameters);
            this.parameters.clear();
            this.value = 0;
            this.type = null;
            this.stepDef = null;
            this.geneIndex = -1;
            this.geneValueIndex = -1;
            this.stopReadActions = false;
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
