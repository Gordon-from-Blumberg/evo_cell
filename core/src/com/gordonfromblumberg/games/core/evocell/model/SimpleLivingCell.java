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

        setParameter(LivingCellParameters.ParameterName.chlorophyll, RandomGen.INSTANCE.nextInt(4, 7));
    }

    @Override
    protected void _update(GameWorld world) {
        if (cell.minerals > 0)
            eatMinerals(0);

        if (energy >= energyToProduceOffspring) {
            if (producedOffsprings < 2 && organics >= organicsToProduceOffspring
                    && age >= minAgeToReproduce && turnsAfterReproduced >= reproduceDelay) {
                produceOffspring(world, 0);
                if (offspring != null) {
                    initOffspring(world, offspring);
                    offspring.lastTurnUpdated = world.getTurn();
                    world.updateCellStatistic(offspring);
                    offspring = null;
                    ++producedOffsprings;
                }
            } else {
                produceOrganics(0);
            }
        }

        if (energy >= energyToMove + getMoveCost() && RandomGen.INSTANCE.nextBool(moveProb)) {
            Cell forward = getForwardCell(world.getGrid());
            if (forward != null && forward.bot == null) {
                move(world.getGrid(), 0);
            }
        }
        if (energy >= energyToMove + getRotateCost() && RandomGen.INSTANCE.nextBool(rotateProb)) {
            if (RandomGen.INSTANCE.nextBool())
                rotateLeft(0);
            else
                rotateRight(0);
        }
    }

    @Override
    protected void initOffspring(GameWorld world, LivingCell offspring) {
        offspring.init();
    }

    @Override
    protected SimpleLivingCell getOffspringInstance() {
        return getInstance();
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
