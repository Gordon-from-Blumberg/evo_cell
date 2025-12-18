package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class StaticLightDistribution implements LightDistribution {
    private final WorldParams worldParams;

    public StaticLightDistribution(WorldParams worldParams) {
        this.worldParams = worldParams;
    }

    @Override
    public int getLight(int x, int y, int turn) {
        int worldHeight = worldParams.getHeight();
        return worldParams.getMinLight()
                + (worldParams.getMaxLight() - worldParams.getMinLight() + 1) * y * y / (worldHeight * worldHeight);
    }
}
