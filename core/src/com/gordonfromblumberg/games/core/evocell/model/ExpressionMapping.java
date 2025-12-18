package com.gordonfromblumberg.games.core.evocell.model;

@FunctionalInterface
public interface ExpressionMapping {
    int getValue(CellGrid grid, Bot bot, ExpressionParameter p1, ExpressionParameter p2);

    interface ExpressionParameter {
        boolean bool(CellGrid grid, Bot bot);
        int number(CellGrid grid, Bot bot);
    }
}
