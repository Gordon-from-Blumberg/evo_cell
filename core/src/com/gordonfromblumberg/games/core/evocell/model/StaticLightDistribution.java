package com.gordonfromblumberg.games.core.evocell.model;

public class StaticLightDistribution implements LightDistribution {
    private final int height;
    private final int minLight, maxLight;

    public StaticLightDistribution(int height, int minLight, int maxLight) {
        this.height = height;
        this.minLight = minLight;
        this.maxLight = maxLight;
    }

    @Override
    public int getLight(int x, int y, int turn) {
        float k = (y + 1f) / height;
        return (int) (minLight + k * (maxLight - minLight));
    }
}
