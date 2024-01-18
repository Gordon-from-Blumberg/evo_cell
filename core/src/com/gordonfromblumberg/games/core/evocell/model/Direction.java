package com.gordonfromblumberg.games.core.evocell.model;

public enum Direction {
    up,
    right,
    down,
    left;

    static final Direction[] ALL = values();

    public Direction opposite() {
        return ALL[(ordinal() + 2) % 4];
    }

    public Direction next() {
        return ALL[(ordinal() + 1) % 4];
    }

    public Direction prev() {
        return ALL[(ordinal() + 3) % 4];
    }
}
