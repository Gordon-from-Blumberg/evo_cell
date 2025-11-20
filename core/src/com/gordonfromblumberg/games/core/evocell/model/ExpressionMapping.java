package com.gordonfromblumberg.games.core.evocell.model;

@FunctionalInterface
public interface ExpressionMapping {
    int getValue(ExpressionParameter p1, ExpressionParameter p2);

    interface ExpressionParameter {
        boolean bool();
        int number();
    }
}
