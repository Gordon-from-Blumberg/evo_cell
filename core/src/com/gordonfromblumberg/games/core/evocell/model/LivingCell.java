package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evocell.model.LivingCellParameters.ParameterName;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

import static com.gordonfromblumberg.games.core.common.utils.MathHelper.modPos;

public abstract class LivingCell implements Poolable {
    private static final Logger log = LogManager.create(LivingCell.class);

    private static final int maxHp;
    private static final int actionLimitPerTurn = 3;
    static final int energyConsumption;
    static final int energyConsumptionGrow;
    static final int maxEnergy;
    static final int rotateCost;
    static final int rotateCostGrow;
    static final int moveCost;
    static final int moveCostGrow;
    static final int regenerateCost;
    static final int increaseParameterCost;
    static final int offspringProducingCost;
    static final int agingStart;
    static final int maxAge;

    protected static int nextId = 1;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        maxHp = configManager.getInteger("livingCell.maxHp");
        energyConsumption = configManager.getInteger("livingCell.energyConsumption");
        energyConsumptionGrow = configManager.getInteger("livingCell.energyConsumptionGrow");
        maxEnergy = configManager.getInteger("livingCell.maxEnergy");
        rotateCost = configManager.getInteger("livingCell.rotateCost");
        rotateCostGrow = configManager.getInteger("livingCell.rotateCostGrow");
        moveCost = configManager.getInteger("livingCell.moveCost");
        moveCostGrow = configManager.getInteger("livingCell.moveCostGrow");
        regenerateCost = configManager.getInteger("livingCell.regenerateCost");
        increaseParameterCost = configManager.getInteger("livingCell.increaseParameterCost");
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
    int wishedTemperature;
    int temperature;
    int heat;
    int water;
    boolean isDead;
    Cell cell;
    Direction dir;
    final LivingCellParameters parameters = new LivingCellParameters();

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

        int mass = mass();
        heat += 2 * (cell.temperature - temperature);
        int tempDiff = heat / mass;
        temperature += tempDiff;
        heat -= tempDiff * mass;
        hp -= Math.abs(temperature - wishedTemperature) / 3;

//        if (water == 0) {
//            hp -= 2;
//        } else if (water < organics / 2 + 1) {
//            --hp;
//        }

        if (hp <= 0) {
            die();
            return;
        }

        photosynthesize();
        _update(world);

        checkHp();
        checkOrganics();
        int energyDiff = getEnergyConsumption();
        energy -= energyDiff;
        checkEnergy();

        lastTurnUpdated = world.getTurn();
        world.updateCellStatistic(this);
    }

    protected abstract void _update(GameWorld world);

    public void photosynthesize() {
        final int chlorophyll = parameters.get(ParameterName.chlorophyll);
        final int sunLight = cell.sunLight;
        if (chlorophyll > 0 && sunLight >= 8 - chlorophyll) {
            int energyDiff = (int) (sunLight * (0.7f + 0.3f * chlorophyll));
            if (energyDiff > 0 && minerals == 0 && cell.minerals == 0) {
                energyDiff -= Math.max(1, energyDiff / 3);
            }
            energy += energyDiff;

            if (minerals > 0) {
                --minerals;
                ++organics;
            } else if (cell.minerals > 0) {
                --cell.minerals;
                ++organics;
            }
        }
    }

    void produceOffspring(GameWorld world, int counter) {
        changeEnergy(-offspringProducingCost);
        if (counter > 0) return;

        Cell targetCell = findCellToProduceOffspring(world.getGrid());
        if (targetCell != null) {
            LivingCell offspring = getOffspringInstance();
            offspring.setCell(targetCell);

            int offspringEnergy = energy / 4;
            changeEnergy(-offspringEnergy);
            offspring.setEnergy(offspringEnergy);

            int offspringOrganics = organics / 4;
            changeOrganics(-offspringOrganics);
            offspring.setOrganics(offspringOrganics);

            int offspringMinerals = minerals / 4;
            changeMinerals(-offspringMinerals);
            offspring.setMinerals(offspringMinerals);

            int offspringWater = water / 4;
            changeWater(-offspringWater);
            offspring.setWater(offspringWater);

            offspring.setDir(Direction.random());
            offspring.setTemperature(temperature);
            initOffspring(world, offspring);
            offspring.lastTurnUpdated = world.getTurn();
            world.updateCellStatistic(offspring);
        }
    }

    void die() {
        if (!isDead) {
            cell.energy += Math.max(energy, 0);
            cell.organics += Math.max(organics, 0);
            cell.minerals += minerals;
            cell.object = null;
            isDead = true;
        }
    }

    public void rotateLeft(int counter) {
        int cost = (1 + counter) * getRotateCost();
        changeEnergy(-cost);
        if (counter <= actionLimitPerTurn)
            dir = dir.prev();
    }

    public void rotateRight(int counter) {
        int cost = (1 + counter) * getRotateCost();
        changeEnergy(-cost);
        if (counter <= actionLimitPerTurn)
            dir = dir.next();
    }

    protected int getRotateCost() {
        float grow = rotateCostGrow * (0.5f + 0.2f * parameters.get(ParameterName.moving));
        return (int) (rotateCost + mass() / grow);
    }

    public void move(CellGrid grid, int counter) {
        int cost = (counter + 1) * getMoveCost();
        changeEnergy(-cost);
        if (counter <= actionLimitPerTurn && energy > 0) {
            Cell target = getForwardCell(grid);
            if (target != null && target.object == null) {
                setCell(target);
            }
        }
    }

    protected int getMoveCost() {
        float grow = moveCostGrow * (0.5f + 0.2f * parameters.get(ParameterName.moving));
        return (int) (moveCost + mass() / grow);
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

    public void produceOrganics() {
        changeEnergy(-5);
        int energyDiff = Math.min(energy, 20);
        changeEnergy(-energyDiff);
        organics += energyDiff / 20;
    }

    public void digestOrganics() {
        changeEnergy(-1);
        int organicsDiff = Math.min(organics, 1);
        changeOrganics(-organicsDiff);
        energy += organicsDiff * 19;
        heat += organicsDiff * organics;
    }

    public void absorbMinerals(int counter) {
        int cost = 1 + 3 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn) {
            int mineralsToAbsorb = Math.min(cell.getMinerals(), 3 + parameters.get(ParameterName.bigMouth) / 2);
            cell.changeMinerals(-mineralsToAbsorb);
            minerals += mineralsToAbsorb;
        }
    }

    public void increaseParameter(int parameter, int counter) {
        parameter = modPos(parameter, parameters.count());
        int cost = parameters.getIncreaseCost(parameter) + increaseParameterCost;
        changeEnergy(-cost);
        if (energy > 0 && counter < 1) {
            parameters.increase(parameter);
        }
    }

    public void increaseParameterEmbryo(int parameter) {
        parameter = modPos(parameter, parameters.count());
        int cost = Math.max(1, parameters.getIncreaseCost(parameter) / 2);
        changeEnergy(-cost);
        if (energy > 0) {
            parameters.increase(parameter);
        }
    }

    public void decreaseParameter(int parameter, int counter) {
        parameter = modPos(parameter, parameters.count());
        int cost = parameters.getDecreaseCost(parameter) + increaseParameterCost / 2;
        changeEnergy(-cost);
        if (energy > 0 && counter < 1) {
            parameters.decrease(parameter);
        }
    }

    public void decreaseParameterEmbryo(int parameter) {
        parameter = modPos(parameter, parameters.count());
        int cost = Math.max(1, parameters.getDecreaseCost(parameter) / 2);
        changeEnergy(-cost);
        if (energy > 0) {
            parameters.decrease(parameter);
        }
    }

    public static int getCellProperty(LivingCell bot, int property) {
        int index = modPos(property, CellProperty.values.length);
        return switch (CellProperty.values[index]) {
            case sunLight -> bot.cell.sunLight;
            case temperature -> bot.cell.temperature;
            case organics -> bot.cell.organics;
            case minerals -> bot.cell.minerals;
            case energy -> bot.cell.energy;
            case humidity -> bot.cell.humidity;
            case water -> bot.cell.water;
        };
    }

    public static int getBotProperty(LivingCell bot, int property) {
        int index = modPos(property, BotProperty.values.length);
        return switch (BotProperty.values[index]) {
            case hp -> bot.hp;
            case energy -> bot.energy;
            case organics -> bot.organics;
            case minerals -> bot.minerals;
            case age -> bot.age;
            case wishedTemperature -> bot.wishedTemperature;
            case temperature -> bot.temperature;
            case heat -> bot.heat;
            case water -> bot.water;
        };
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

    public int getId() {
        return id;
    }

    void setParameter(ParameterName parameter, int value) {
        parameters.set(parameter, value);
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

    public int getEnergyConsumption() {
        int agingConsumption = age > agingStart ? (age - agingStart) / 10 : 0;
        return energyConsumption + agingConsumption + parameters.energyConsumption() + organics / energyConsumptionGrow;
    }

    public int getHp() {
        return hp;
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

    public int mass() {
        return organics + minerals;
    }

    public int getAge() {
        return age;
    }

    public int getWishedTemperature() {
        return wishedTemperature;
    }

    public void setWishedTemperature(int wishedTemperature) {
        this.wishedTemperature = wishedTemperature;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    void changeWater(int diff) {
        water += diff;
        if (water < 0) water = 0;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    protected abstract void initOffspring(GameWorld world, LivingCell offspring);

    protected abstract LivingCell getOffspringInstance();

    @Override
    public void reset() {
        id = 0;
        lastTurnUpdated = 0;
        hp = 0;
        energy = 0;
        organics = 0;
        minerals = 0;
        age = 0;
        wishedTemperature = 0;
        temperature = 0;
        heat = 0;
        isDead = false;
        cell = null;
        dir = null;
    }
}
