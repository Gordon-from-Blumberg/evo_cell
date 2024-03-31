package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class StaticTemperatureDistribution implements TemperatureDistribution {
    private final WorldParams worldParams;

    public StaticTemperatureDistribution(WorldParams worldParams) {
        this.worldParams = worldParams;
    }

    @Override
    public int getTemperature(int x, int y, int turn) {
        return worldParams.getMinTemperature()
                + (worldParams.getMaxTemperature() - worldParams.getMinTemperature() + 1) * x / worldParams.getWidth();
    }
}
