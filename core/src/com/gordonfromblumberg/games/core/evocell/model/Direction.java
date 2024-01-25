package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.utils.RandomGen;

public enum Direction {
    up(0, 1),
    right(1, 0),
    down(0, -1),
    left(-1, 0);

    public static final Direction[] ALL = values();

    public final int x;
    public final int y;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Direction random() {
        return ALL[RandomGen.INSTANCE.nextInt(4)];
    }

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
