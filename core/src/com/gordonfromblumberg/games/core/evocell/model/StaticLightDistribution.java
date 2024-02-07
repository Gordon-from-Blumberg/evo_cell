package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class StaticLightDistribution implements LightDistribution {
    private final WorldParams worldParams;

    public StaticLightDistribution(WorldParams worldParams) {
        this.worldParams = worldParams;
    }

    @Override
    public int getLight(int x, int y, int turn) {
        float k = (y + 1f) / worldParams.getHeight();
        return (int) (worldParams.getMinLight() + k * (worldParams.getMaxLight() - worldParams.getMinLight()));
    }
}
