package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

public abstract class LivingCell implements Poolable {
    private static final Logger log = LogManager.create(LivingCell.class);

    private static final int maxHp;
    static final int energyConsumption;
    static final int maxEnergy;
    static final int rotateCost;
    static final int moveCost;
    static final int regenerateCost;
    static final int offspringProducingCost;
    static final int agingStart;
    static final int maxAge;

    protected static int nextId = 1;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        maxHp = configManager.getInteger("livingCell.maxHp");
        energyConsumption = configManager.getInteger("livingCell.energyConsumption");
        maxEnergy = configManager.getInteger("livingCell.maxEnergy");
        rotateCost = configManager.getInteger("livingCell.rotateCost");
        moveCost = configManager.getInteger("livingCell.moveCost");
        regenerateCost = configManager.getInteger("livingCell.regenerateCost");
        offspringProducingCost = configManager.getInteger("livingCell.offspringProducingCost");
        agingStart = configManager.getInteger("livingCell.agingStart");
        maxAge = configManager.getInteger("livingCell.maxAge");
    }

    int id;
    int lastTurnUpdated;
    int hp;
    int energy;
    int organics;
    int minerals;
    int age;
    boolean isDead;
    Cell cell;
    Direction dir;

    public void init() {
        id = nextId++;
        hp = maxHp;
    }

    public void update(GameWorld world) {
        if (lastTurnUpdated == world.getTurn())
            return;

        if (++age == maxAge) {
            log.debug("Cell #" + id + " dies from aging");
            die();
            return;
        }

        if (hp < maxHp && energy > regenerateCost) {
            ++hp;
            energy -= regenerateCost;
        }

        if (minerals > organics) {
            hp -= minerals / organics;
        }

        _update(world);

        checkHp();
        checkOrganics();
        int energyDiff = -energyConsumption;
        if (age > agingStart) {
            energyDiff -= (age - agingStart) / 10;
        }
        energy += energyDiff;
        checkEnergy();

        lastTurnUpdated = world.getTurn();
        world.updateCellStatistic(this);
    }

    protected abstract void _update(GameWorld world);

    public void photosynthesize() {
        energy += cell.sunLight - 1;
        if (cell.sunLight >= 5) {
            if (minerals > 0) {
                changeMinerals(-2);
                ++organics;
            } else if (cell.minerals > 0) {
                cell.changeMinerals(-2);
                ++organics;
            }
        }
    }

    public abstract void produceOffspring(GameWorld world);

    void die() {
        cell.energy += energy;
        cell.organics += organics;
        cell.minerals += minerals;
        cell.object = null;
        isDead = true;
    }

    public void rotateLeft() {
        dir = dir.prev();
        changeEnergy(-rotateCost);
    }

    public void rotateRight() {
        dir = dir.next();
        changeEnergy(-rotateCost);
    }

    public void move(CellGrid grid) {
        Cell target = getForwardCell(grid);
        if (target != null && target.object == null) {
            setCell(target);
        }
        changeEnergy(-moveCost);
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

    public Cell getForwardCell(CellGrid grid) {
        return grid.getCell(cell, dir);
    }

    public void produceFat() {
        changeEnergy(-5);
        int energyDiff = Math.min(energy, 15);
        int organicsDiff = energyDiff / 5;
        changeEnergy(-energyDiff);
        organics += organicsDiff;
    }

    public void consumeFat() {
        changeEnergy(-1);
        int organicsDiff = Math.min(organics, 2);
        int energyDiff = organicsDiff * 6;
        organics -= organicsDiff;
        energy += energyDiff;
    }

    public void absorbMinerals() {
        --energy;
        int mineralsToAbsorb = Math.min(cell.getMinerals(), 3);
        cell.changeMinerals(-mineralsToAbsorb);
        minerals += mineralsToAbsorb;
    }

    protected Cell findCellToProduceOffspring(CellGrid grid) {
        Cell result = grid.getCell(cell, dir);
        if (result != null && result.object == null)
            return result;
        result = grid.getCell(cell, dir.prev());
        if (result != null && result.object == null)
            return result;
        result = grid.getCell(cell, dir.next());
        if (result != null && result.object == null)
            return result;
        result = grid.getCell(cell, dir.opposite());
        return result != null && result.object == null ? result : null;
    }

    private void checkHp() {
        if (hp <= 0) {
            log.debug("Cell #" + id + " dies with HP " + hp);
            die();
        }
    }

    private void checkEnergy() {
        if (energy <= 0 || energy >= maxEnergy) {
            log.debug("Cell #" + id + " dies with energy " + energy);
            die();
        }
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void changeEnergy(int diff) {
        energy += diff;
        if (energy < 0) energy = 0;
    }

    public int getOrganics() {
        return organics;
    }

    public void setOrganics(int organics) {
        this.organics = organics;
    }

    public void changeOrganics(int diff) {
        organics += diff;
        if (organics < 0) organics = 0;
    }

    private void checkOrganics() {
        if (organics <= 0) {
            log.debug("Cell #" + id + " dies with organics " + organics);
            die();
        }
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

    public int getAge() {
        return age;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    @Override
    public void reset() {
        id = 0;
        lastTurnUpdated = 0;
        hp = 0;
        energy = 0;
        organics = 0;
        minerals = 0;
        age = 0;
        isDead = false;
        cell = null;
        dir = null;
    }
}
