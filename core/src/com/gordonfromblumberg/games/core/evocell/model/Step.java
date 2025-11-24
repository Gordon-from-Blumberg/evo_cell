package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

public class Step implements Pool.Poolable, ExpressionMapping.ExpressionParameter {
    private final Array<Step> parameters = new Array<>(4);

    byte value;
    StepType type;
    Object stepDef;
    int geneIndex;
    int geneValueIndex;
    int lastRead;
    boolean stopReadActions;
    boolean wasReset;

    void addParameter(Step step) {
        parameters.add(step);
    }

    Array<Step> parameters() {
        return parameters;
    }

    @Override
    public boolean bool() {
        return number() > 0;
    }

    @Override
    public int number() {
        if (stepDef instanceof ExpressionDef exprDef) {
            ExpressionMapping expression = Expressions.expressionsMap.get(exprDef.name());
            Step par2 = parameters.size > 1 ? parameters.get(1) : null;
            return expression.getValue(parameters.get(0), par2);
        } else if (type == StepType.expression) {
            return value;
        } else {
            throw new IllegalStateException("number should not be invoked on " + type + " step with stepDef " + stepDef);
        }
    }

    @Override
    public void reset() {
        this.parameters.clear();
        this.value = 0;
        this.type = null;
        this.stepDef = null;
        this.geneIndex = -1;
        this.geneValueIndex = -1;
        this.stopReadActions = false;
    }

    @Override
    public String toString() {
        return geneIndex + "_" + geneValueIndex + "_" + type + "_" + stepDefToString();
    }

    private String stepDefToString() {
        return stepDef instanceof ActionDef a ? a.name()
                : stepDef instanceof ExpressionDef e ? e.name() : "";
    }

    enum StepType {
        action,
        actionGroup,
        ifStatement,
        gotoStatement,
        expression
    }
}
