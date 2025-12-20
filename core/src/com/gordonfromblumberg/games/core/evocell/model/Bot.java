package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.math.MathUtils;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.evocell.model.BotParameters.ParameterName;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

import static com.gordonfromblumberg.games.core.common.utils.MathHelper.modPos;

public abstract class Bot implements Poolable {
    private static final Logger log = LogManager.create(Bot.class);

    public static final int maxHp;

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
    static final int minAgeToReproduce = 15;
    static final int reproduceDelay = 7;

    protected static int nextId = 1;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        maxHp = configManager.getInteger("bot.maxHp");
        energyConsumption = configManager.getInteger("bot.energyConsumption");
        energyConsumptionGrow = configManager.getInteger("bot.energyConsumptionGrow");
        maxEnergy = configManager.getInteger("bot.maxEnergy");
        rotateCost = configManager.getInteger("bot.rotateCost");
        rotateCostGrow = configManager.getInteger("bot.rotateCostGrow");
        moveCost = configManager.getInteger("bot.moveCost");
        moveCostGrow = configManager.getInteger("bot.moveCostGrow");
        regenerateCost = configManager.getInteger("bot.regenerateCost");
        increaseParameterCost = configManager.getInteger("bot.increaseParameterCost");
        offspringProducingCost = configManager.getInteger("bot.offspringProducingCost");
        agingStart = configManager.getInteger("bot.agingStart");
        maxAge = configManager.getInteger("bot.maxAge");
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
    int turnsAfterReproduced;
    boolean isDead;
    Cell cell;
    Direction dir;
    Bot offspring;
    final BotParameters parameters = new BotParameters();

    public void init() {
        id = nextId++;
        hp = maxHp;
        wishedTemperature = 17;
    }

    public void update(GameWorld world) {
        if (lastTurnUpdated == world.getTurn())
            return;

        if (++age == maxAge) {
//            log.debug("Cell #" + id + " dies from aging");
            die();
            return;
        }

        ++turnsAfterReproduced;

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
            int energyDiff = (int) (sunLight * (0.5f + 0.2f * chlorophyll));
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
        if (age < minAgeToReproduce || turnsAfterReproduced < reproduceDelay) return;
        changeEnergy(-offspringProducingCost);
        if (counter > 0) return;

        Cell targetCell = findCellToProduceOffspring(world.getGrid());
        if (targetCell != null) {
            turnsAfterReproduced = 0;
            Bot offspring = getOffspringInstance();
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

            this.offspring = offspring;
        }
    }

    void die() {
        if (!isDead) {
            cell.energy += Math.max(energy, 0);
            cell.organics += Math.max(organics, 0);
            cell.minerals += Math.max(minerals, 0);
            cell.bot = null;
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
            if (target != null && target.bot == null) {
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
        if (cell.bot != null) {
            throw new IllegalStateException("Cell must be empty");
        }
        if (this.cell != null) {
            this.cell.bot = null;
        }
        this.cell = cell;
        cell.bot = this;
    }

    public Cell getForwardCell(CellGrid grid) {
        return grid.getCell(cell, dir);
    }

    public void eatOrganics(int counter) {
        int cost = 1 + 4 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn) {
            int organicsToEat = Math.min(cell.getOrganics(), 3 + parameters.get(ParameterName.bigMouth) / 2);
            int energyToAbsorb = Math.min(Math.min(cell.energy, organicsToEat), 5);
            cell.changeOrganics(-organicsToEat);
            cell.changeEnergy(-energyToAbsorb);
            organics += organicsToEat;
            energy += energyToAbsorb;
            if (cell.minerals > 10) {
                --cell.minerals;
                ++minerals;
            }
            if (cell.water > 10) {
                --cell.water;
                ++water;
            }
        }
    }

    public void produceOrganics(int counter) {
        int cost = 5 + 4 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn) {
            int energyDiff = Math.min(energy, 25);
            changeEnergy(-energyDiff);
            organics += energyDiff / 25;
        }
    }

    public void digestOrganics(int counter) {
        int cost = 1 + 5 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn && organics > 0) {
            --organics;
            energy += (int) (25 * (0.8f + 0.1f * parameters.get(ParameterName.organicsDigestion)));
            heat += organics + counter * organics / 2;
        }
    }

    public void transformMineralsToOrganics(int counter) {
        int cost = 3 + 7 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn && minerals > 0) {
            --minerals;
            ++organics;
        }
    }

    public void bite(CellGrid grid, int counter) {
        int cost = 10 + 15 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn) {
            Cell forwardCell = getForwardCell(grid);
            if (forwardCell != null) {
                Bot target = forwardCell.bot;
                if (target != null) {
                    int dmg = 5;
                    float massRatio = ((float) mass()) / target.mass();
                    if (massRatio > 1) {
                        dmg += (int) ((massRatio - 1) / 0.3f);
                    } else {
                        dmg -= (int) ((1 - massRatio) / 0.2f);
                    }
                    dmg = MathUtils.clamp(dmg, 1, 15);
                    target.hp -= dmg;
                    int organicsDiff = Math.min(1 + organics / (20 - parameters.get(ParameterName.bigMouth)),
                                                target.organics);
                    target.organics -= organicsDiff;
                    organics += organicsDiff;
                    if (dmg > 3 && target.minerals > 10) {
                        --target.minerals;
                        ++minerals;
                    }
                    if (dmg > 3 && target.water > 10) {
                        --target.water;
                        ++water;
                    }

                    if (target.hp <= 0 || target.organics <= 0) {
                        target.die();
                        target.release();
                        if (target == offspring) offspring = null;
                    }
                }
            }
        }
    }

    public void eatMinerals(int counter) {
        int cost = 1 + 4 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn) {
            int mineralsToEat = Math.min(cell.getMinerals(), 2 + parameters.get(ParameterName.bigMouth) / 2);
            int energyToAbsorb = Math.min(Math.min(cell.energy, mineralsToEat), 5);
            cell.changeMinerals(-mineralsToEat);
            cell.changeEnergy(-energyToAbsorb);
            minerals += mineralsToEat;
            energy += energyToAbsorb;
            if (cell.organics > 10) {
                --cell.organics;
                ++organics;
            }
            if (cell.water > 10) {
                --cell.water;
                ++water;
            }
        }
    }

    public void chemosynthesis(int counter) {
        int cost = 1 + 5 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn && minerals > 0) {
            --minerals;
            energy += (int) (20 * (0.8f + 0.15f * parameters.get(ParameterName.chemosynthesis)));
            heat += (organics + counter * organics / 2) / 2;
        }
    }

    public void transformOrganicsToMinerals(int counter) {
        int cost = 2 + 5 * counter;
        changeEnergy(-cost);
        if (energy > 0 && counter < actionLimitPerTurn) {
            --organics;
            ++minerals;
        }
    }

    public void increaseParameter(int parameter, int counter) {
        parameter = modPos(parameter, parameters.count());
        if (!parameters.canIncrease(parameter))
            return;
        int cost = parameters.getIncreaseCost(parameter) + increaseParameterCost;
        changeEnergy(-cost);
        if (energy > 0 && counter < 1) {
            parameters.increase(parameter);
        }
    }

    public void increaseParameterEmbryo(int parameter) {
        parameter = modPos(parameter, parameters.count());
        if (!parameters.canIncrease(parameter))
            return;
        int cost = Math.max(1, parameters.getIncreaseCost(parameter) / 2);
        changeEnergy(-cost);
        if (energy > 0) {
            parameters.increase(parameter);
        }
    }

    public void decreaseParameter(int parameter, int counter) {
        parameter = modPos(parameter, parameters.count());
        if (!parameters.canDecrease(parameter))
            return;
        int cost = parameters.getDecreaseCost(parameter) + increaseParameterCost / 2;
        changeEnergy(-cost);
        if (energy > 0 && counter < 1) {
            parameters.decrease(parameter);
        }
    }

    public void decreaseParameterEmbryo(int parameter) {
        parameter = modPos(parameter, parameters.count());
        if (!parameters.canDecrease(parameter))
            return;
        int cost = Math.max(1, parameters.getDecreaseCost(parameter) / 2);
        changeEnergy(-cost);
        if (energy > 0) {
            parameters.decrease(parameter);
        }
    }

    public void regenerate(int counter) {
        int cost = 2 * regenerateCost * (counter + 1);
        changeEnergy(-cost);
        if (hp < maxHp && energy > 0 && counter < actionLimitPerTurn) {
            ++hp;
        }
    }

    public static int getCellProperty(Bot bot, int property) {
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

    public static int getBotProperty(Bot bot, int property) {
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
            case turnsAfterReproduced -> bot.turnsAfterReproduced;
        };
    }

    public static int div(int n1, int n2) {
        return n2 != 0 ? n1 / n2
                : n1 > 0 ? Integer.MAX_VALUE
                : n1 < 0 ? Integer.MIN_VALUE : 0;
    }

    protected Cell findCellToProduceOffspring(CellGrid grid) {
        Cell result = grid.getCell(cell, dir);
        if (result != null && result.bot == null)
            return result;
        result = grid.getCell(cell, dir.prev());
        if (result != null && result.bot == null)
            return result;
        result = grid.getCell(cell, dir.next());
        if (result != null && result.bot == null)
            return result;
        result = grid.getCell(cell, dir.opposite());
        return result != null && result.bot == null ? result : null;
    }

    private void checkHp() {
        if (hp <= 0) {
//            log.debug("Cell #" + id + " dies with HP " + hp);
            die();
        }
    }

    private void checkEnergy() {
        if (energy <= 0 || energy >= maxEnergy) {
//            log.debug("Cell #" + id + " dies with energy " + energy);
            die();
        }
    }

    public int getId() {
        return id;
    }

    public int getParameter(ParameterName parameter) {
        return parameters.get(parameter);
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
//            log.debug("Cell #" + id + " dies with organics " + organics);
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

    public int getHeat() {
        return heat;
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

    protected abstract void initOffspring(GameWorld world, Bot offspring);

    protected abstract Bot getOffspringInstance();

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
        turnsAfterReproduced = 0;
        isDead = false;
        cell = null;
        dir = null;
        offspring = null;
        parameters.reset();
    }
}
