package com.gordonfromblumberg.games.core.evocell.model;

@FunctionalInterface
public interface ExpressionMapping {
    int getValue(CellGrid grid, LivingCell bot, ExpressionParameter p1, ExpressionParameter p2);

    interface ExpressionParameter {
        boolean bool(CellGrid grid, LivingCell bot);
        int number(CellGrid grid, LivingCell bot);
    }
}
