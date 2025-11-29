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
    byte activeGeneIndex;
    Object contextObject;

    private EvoLivingCell() { }

    public static EvoLivingCell getInstance() {
        return pool.obtain();
    }

    public void setRandomDna() {
        dna.setRandom();
    }

    public void setGene(int geneIndex, int... geneValues) {
        dna.getGene(geneIndex).set(geneValues);
    }

    @Override
    protected void _update(GameWorld world) {
        final Interpreter interpreter = world.interpreter();
        interpreter.run(world, this);
    }

    @Override
    protected void initOffspring(GameWorld world, LivingCell offspring) {
        final EvoLivingCell child = (EvoLivingCell) offspring;
        child.dna.set(this.dna);
        child.dna.mutate();

        world.interpreter().runEmbryo(child);
    }

    @Override
    protected EvoLivingCell getOffspringInstance() {
        return getInstance();
    }

    @Override
    public void reset() {
        super.reset();

        dna.reset();
        activeGeneIndex = 0;
        contextObject = null;
    }

    @Override
    public void release() {
        pool.free(this);
    }
}
