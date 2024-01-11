package com.gordonfromblumberg.games.core.evotree.model;

public enum Direction {
    up(0) {
        @Override
        Direction opposite() {
            return down;
        }
    },
    right(1) {
        @Override
        Direction opposite() {
            return left;
        }
    },
    down(2) {
        @Override
        Direction opposite() {
            return up;
        }
    },
    left(3) {
        @Override
        Direction opposite() {
            return right;
        }
    };

    static final Direction[] ALL = values();

    private final int code;

    Direction(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    abstract Direction opposite();
}
