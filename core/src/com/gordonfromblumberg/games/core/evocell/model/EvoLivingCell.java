package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

public class EvoLivingCell extends LivingCell {
    private static final Pool<EvoLivingCell> pool = new Pool<>() {
        @Override
        protected EvoLivingCell newObject() {
            return new EvoLivingCell();
        }
    };

    final DNA dna = DNA.getInstance();

    private EvoLivingCell() { }

    public static EvoLivingCell getInstance() {
        return pool.obtain();
    }

    public void setRandomDna() {
        dna.setRandom();
    }

    @Override
    protected void _update(GameWorld world) {

    }

    @Override
    public void produceOffspring(GameWorld world) {

    }

    @Override
    public void reset() {
        super.reset();

        dna.reset();
    }

    @Override
    public void release() {
        pool.free(this);
    }
}
