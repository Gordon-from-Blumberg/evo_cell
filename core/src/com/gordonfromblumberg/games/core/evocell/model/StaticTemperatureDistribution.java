package com.gordonfromblumberg.games.core.evocell.model;

public class StaticTemperatureDistribution implements TemperatureDistribution {
    private final int width;
    private final int min, max;

    public StaticTemperatureDistribution(int width, int min, int max) {
        this.width = width;
        this.min = min;
        this.max = max;
    }

    @Override
    public int getTemperature(int x, int y, int turn) {
        return min + (max - min) * (x + 1) / width;
    }
}
