package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.math.MathUtils;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;
import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class Cell {
    private static final float MINERALS_APPEARING_PROB;
    private static final float MINERALS_INCREASING_PROB;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        MINERALS_APPEARING_PROB = configManager.getFloat("cell.mineralsAppearing");
        MINERALS_INCREASING_PROB = configManager.getFloat("cell.mineralsIncreasing");
    }

    final int x, y;
    int sunLight;
    int temperature;
    int organics;
    int minerals;
    int energy;
    int humidity;
    int water;
    int turnsAfterWaterUpdate;
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

    public void update(GameWorld world) {
        final CellGrid grid = world.getGrid();
        float mineralsIncreasing;
        if (minerals > 0) {
            mineralsIncreasing = MINERALS_INCREASING_PROB;
        } else {
            mineralsIncreasing = MINERALS_APPEARING_PROB;
            for (Direction d : Direction.ALL) {
                Cell n = grid.getCell(this, d);
                if (n != null && n.minerals > 0) {
                    mineralsIncreasing *= 5;
                    break;
                }
            }
        }

        WorldParams params = world.getParams();
        if (RandomGen.INSTANCE.nextBool(mineralsIncreasing * MathUtils.map(params.getMaxLight(), params.getMinLight(),
                1f, 2f, sunLight))) {
            ++minerals;
        }

        int energyDiff = -3;
        if (organics > 0) {
            int organicsDiff = Math.min(temperature <= 0 ? 0 : temperature >= 20 ? 4 : 2, organics);
            changeOrganics(-organicsDiff);
            if (organics == 0) {
                energy = 0;
            }
            minerals += (organicsDiff + 1) / 2;
            energyDiff -= organicsDiff;
        }
        if (energy > 0) {
            changeEnergy(energyDiff);
        }

        int waterDiff = humidity - water;
        if (waterDiff != 0 && ++turnsAfterWaterUpdate >= 12 - Math.abs(waterDiff)) {
            turnsAfterWaterUpdate = 0;
            if (waterDiff > 0) ++water;
            else --water;
        }

        final LivingCell livingCell = object;
        if (livingCell != null && livingCell.lastTurnUpdated != world.getTurn()) {
            livingCell.update(world);
            if (livingCell.isDead) {
                livingCell.release();
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSunLight() {
        return sunLight;
    }

    public void setSunLight(int sunLight) {
        this.sunLight = sunLight;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getOrganics() {
        return organics;
    }

    public void setOrganics(int organics) {
        this.organics = organics;
    }

    void changeOrganics(int diff) {
        organics += diff;
        if (organics < 0) organics = 0;
    }

    public int getMinerals() {
        return minerals;
    }

    public void setMinerals(int minerals) {
        this.minerals = minerals;
    }

    void changeMinerals(int diff) {
        minerals += diff;
        if (minerals < 0) minerals = 0;
    }

    public int getEnergy() {
        return energy;
    }

    void changeEnergy(int diff) {
        energy += diff;
        if (energy < 0) energy = 0;
    }
}
