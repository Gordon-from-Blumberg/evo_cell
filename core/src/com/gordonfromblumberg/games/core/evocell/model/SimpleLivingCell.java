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
    private static final int ENERGY_TO_MOVE;
    private static final float ROTATE_PROB;
    private static final float MOVE_PROB;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        ENERGY_TO_MOVE = configManager.getInteger("simpleLivingCell.energyToMove");
        ROTATE_PROB = configManager.getFloat("simpleLivingCell.rotateProb");
        MOVE_PROB = configManager.getFloat("simpleLivingCell.moveProb");
    }

    public static SimpleLivingCell getInstance() {
        return pool.obtain();
    }

    @Override
    protected void _update(GameWorld world) {
        photosynthesize();

        if (energy > ENERGY_TO_MOVE && RandomGen.INSTANCE.nextBool(MOVE_PROB)) {
            move(world.getGrid());
            if (RandomGen.INSTANCE.nextBool(ROTATE_PROB)) {
                if (RandomGen.INSTANCE.nextBool())
                    rotateLeft();
                else
                    rotateRight();
            }
        }
    }

    @Override
    public void release() {
        pool.free(this);
    }
}
