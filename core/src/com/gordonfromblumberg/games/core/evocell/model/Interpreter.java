package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.*;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evocell.model.ActionDef.Type;
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

    Step readAction(EvoLivingCell bot, int geneIndex, int geneValueIndex) {
        final Step step = obtainStep(geneIndex, geneValueIndex);

        final Gene gene = bot.dna.getGene(geneIndex);

        ActionDef actionDef = null;
        while (actionDef == null && geneValueIndex < geneValueCount) {
            byte value = gene.getValue(geneValueIndex++);
            actionDef = Actions.actionDefs.get(value);
        }

        if (actionDef != null) {
            step.actionDef = actionDef;
            switch (actionDef.type()) {
                case spec -> {
                    switch (actionDef.name()) {
                        case "if" -> {}
                        case "goto" -> {}
                        case "gotoGene" -> {}
                    }
                }
                case specaction -> {
                    step.type = StepType.actionGroup;
                    int requiredSubActions = switch (actionDef.name()) {
                        case "2actions" -> 2;
                        case "3actions" -> 3;
                        default -> throw new IllegalStateException("Unknown specaction " + actionDef.name());
                    };
                    int lastRead = geneValueIndex - 1;
                    while (requiredSubActions-- > 0 && lastRead + 1 < geneValueCount) {
                        Step subStep = readAction(bot, geneIndex, lastRead + 1);
                        lastRead = subStep.lastRead;
                        step.addParameter(subStep);
                    }
                    step.lastRead = lastRead;
                }
                case action -> {
                    step.type = StepType.action;
                    int requiredParameters = actionDef.parameters() == null ? 0 : actionDef.parameters().length;
                    int lastRead = geneValueIndex - 1;
                    while (requiredParameters-- > 0 && lastRead + 1 < geneValueCount) {
                        Step subStep = readParameter(gene, geneIndex, lastRead + 1);
                        lastRead = subStep.lastRead;
                        step.addParameter(subStep);
                    }
                    step.lastRead = lastRead;
                }
            }
        }

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
                int requiredParameters = expressionDef.defaultValues().length;
                int lastRead = geneValueIndex - 1;
                while (requiredParameters-- > 0 && lastRead + 1 < geneValueCount) {
                    Step subStep = readParameter(gene, geneIndex, lastRead + 1);
                    lastRead = subStep.lastRead;
                    step.addParameter(subStep);
                }
                step.lastRead = lastRead;
            }
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

    private class Step implements Pool.Poolable {
        private final Array<Step> parameters = new Array<>(4);

        byte value;
        StepType type;
        Object actionDef;
        int geneIndex;
        int geneValueIndex;
        int lastRead;

       void addParameter(Step step) {
           parameters.add(step);
        }

        @Override
        public void reset() {
            stepPool.freeAll(this.parameters);
            this.parameters.clear();
            this.value = 0;
            this.type = null;
            this.actionDef = null;
            this.geneIndex = -1;
            this.geneValueIndex = -1;
        }
    }

    private enum StepType {
        action,
        expression,
        ifStatement,
        gotoStatement,
        actionGroup
    }

    /**
     * Ожидаемый тип значения
     */
    private enum ValueType {
        /**
         * Подходят specaction, action
         */
        action,
        /**
         * Параметр для действия или выражения, может быть маркером выражения
         */
        parameter,
        /**
         * Код выражения (функции), если не существует - текущее значение используется как число,
         * предыдущий маркер игнорируется
         */
        expressionCode
    }
}
