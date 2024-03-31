package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class StaticHumidityDistribution implements HumidityDistribution {
    private final WorldParams worldParams;

    public StaticHumidityDistribution(WorldParams worldParams) {
        this.worldParams = worldParams;
    }

    @Override
    public int getHumidity(int x, int y, int turn) {
        return (10 + 1) * (x + y) / (worldParams.getWidth() + worldParams.getHeight());
    }
}
