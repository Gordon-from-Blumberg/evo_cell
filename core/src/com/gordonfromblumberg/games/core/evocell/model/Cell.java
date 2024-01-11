package com.gordonfromblumberg.games.core.evocell.model;

public class Cell {
    int x, y;
    int sunLight;
    int organics;
    int minerals;
    LivingCell object;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public LivingCell getObject() {
        return object;
    }

    public void setObject(LivingCell object) {
        this.object = object;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
