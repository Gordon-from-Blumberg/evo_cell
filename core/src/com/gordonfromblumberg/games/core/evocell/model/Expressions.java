package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

public final class Expressions {
    static final ObjectMap<String, ExpressionMapping> expressionsMap = new ObjectMap<>();
    static final IntMap<ExpressionDef> expressionDefs = new IntMap<>();

    static {
        expressionsMap.put("equals", (p1, p2) -> p1.number() == p2.number() ? 1 : 0);
        expressionsMap.put("not", (p1, p2) -> p1.bool() ? 0 : 1);
        expressionsMap.put("sum", (p1, p2) -> p1.number() + p2.number());
    }

    private Expressions() {
        throw new UnsupportedOperationException("Expressions should not be instantiated");
    }

    private static void loadExpressionDefs() {

    }
}


