package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

public class SimpleLivingCell extends LivingCell {
    private static final Pool<SimpleLivingCell> pool = new Pool<>() {
        @Override
        protected SimpleLivingCell newObject() {
            return new SimpleLivingCell();
        }
    };
    private static final int energyToMove;
    private static final int energyToProduceOffspring;
    private static final int organicsToProduceOffspring;
    private static final float rotateProb;
    private static final float moveProb;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        energyToMove = configManager.getInteger("simpleLivingCell.energyToMove");
        energyToProduceOffspring = offspringProducingCost
                + configManager.getInteger("simpleLivingCell.energyToProduceOffspring");
        organicsToProduceOffspring = configManager.getInteger("simpleLivingCell.organicsToProduceOffspring");
        rotateProb = configManager.getFloat("simpleLivingCell.rotateProb");
        moveProb = configManager.getFloat("simpleLivingCell.moveProb");
    }

    private int producedOffsprings;

    private SimpleLivingCell() { }

    public static SimpleLivingCell getInstance() {
        return pool.obtain();
    }

    @Override
    public void init() {
        super.init();

        wishedTemperature = 17;
        setParameter(LivingCellParameters.ParameterName.chlorophyll, RandomGen.INSTANCE.nextInt(4, 7));
    }

    @Override
    protected void _update(GameWorld world) {
        if (cell.minerals > 0)
            absorbMinerals();

        if (energy >= energyToProduceOffspring) {
            if (producedOffsprings < 2 && organics >= organicsToProduceOffspring) {
                produceOffspring(world);
                ++producedOffsprings;
            } else {
                produceOrganics();
            }
        }

//        if (organics >= 2 * organicsToProduceOffspring
//                && energy < energyToProduceOffspring) {
//            consumeFat();
//        }

        if (energy >= energyToMove + getMoveCost() && RandomGen.INSTANCE.nextBool(moveProb)) {
            Cell forward = getForwardCell(world.getGrid());
            if (forward != null && forward.object == null) {
                move(world.getGrid());
            }
        }
        if (energy >= energyToMove + getRotateCost() && RandomGen.INSTANCE.nextBool(rotateProb)) {
            if (RandomGen.INSTANCE.nextBool())
                rotateLeft();
            else
                rotateRight();
        }
    }

    @Override
    public void produceOffspring(GameWorld world) {
        changeEnergy(-offspringProducingCost);

        Cell targetCell = findCellToProduceOffspring(world.getGrid());
        if (targetCell != null) {
            SimpleLivingCell offspring = getInstance();
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

            offspring.setDir(Direction.random());
            offspring.setTemperature(temperature);
            offspring.setWater(water);
            offspring.init();
            offspring.lastTurnUpdated = world.getTurn();
            world.updateCellStatistic(offspring);
        }
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        super.reset();
        producedOffsprings = 0;
    }
}
