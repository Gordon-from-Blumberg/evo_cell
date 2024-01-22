package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

public abstract class LivingCell implements Poolable {
    private static final int MAX_HP;
    static final int ENERGY_CONSUMPTION;
    static final int MAX_ENERGY;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        MAX_HP = configManager.getInteger("livingCell.maxHp");
        ENERGY_CONSUMPTION = configManager.getInteger("livingCell.energyConsumption");
        MAX_ENERGY = configManager.getInteger("livingCell.maxEnergy");
    }

    int lastTurnUpdated;
    int hp;
    int energy;
    int organics;
    int minerals;
    int age;
    Cell cell;
    Direction dir;

    public void init() {
        hp = MAX_HP;
    }

    public void update(GameWorld world) {
        ++age;
        lastTurnUpdated = world.getTurn();

        photosynthesize();

        energy -= ENERGY_CONSUMPTION;

        checkEnergy();
    }

    public void photosynthesize() {
        energy += cell.sunLight - 1;
        if (minerals > 0) {
            changeMinerals(-2);
            ++organics;
        } else if (cell.minerals > 0) {
            cell.changeMinerals(-2);
            ++organics;
        }
    }

    void die() {
        cell.energy += energy;
        cell.organics += organics;
        cell.minerals += minerals;
        cell.object = null;
    }

    public Cell getCell() {
        return cell;
    }

    public void setCell(Cell cell) {
        if (cell.object != null) {
            throw new IllegalStateException("Cell must be empty");
        }
        if (this.cell != null) {
            this.cell.object = null;
        }
        this.cell = cell;
        cell.object = this;
    }

    private void checkEnergy() {
        if (energy < 0 || energy >= MAX_ENERGY) {
            die();
        }
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getOrganics() {
        return organics;
    }

    public void setOrganics(int organics) {
        this.organics = organics;
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

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    @Override
    public void reset() {
        lastTurnUpdated = 0;
        energy = 0;
        organics = 0;
        minerals = 0;
        age = 0;
        cell = null;
        dir = null;
    }
}
