package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

import static com.gordonfromblumberg.games.core.common.utils.MathHelper.modPos;

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

    public void setActiveGeneIndex(int index) {
        this.activeGeneIndex = (byte) modPos(index, dna.genes.size);
    }

    public void setGene(int geneIndex, int... geneValues) {
        dna.getGene(geneIndex).set(geneValues);
    }

    @Override
    protected void _update(GameWorld world) {
        final Interpreter interpreter = world.interpreter();
        interpreter.run(world, this);
        if (offspring != null) {
            if (offspring.energy > 0 && offspring.organics > 0) {
                initOffspring(world, offspring);
                offspring.lastTurnUpdated = world.getTurn();
                world.updateCellStatistic(offspring);
            } else {
                offspring.die();
                offspring.release();
            }
            offspring = null;
        }
    }

    @Override
    protected void initOffspring(GameWorld world, LivingCell offspring) {
        final EvoLivingCell child = (EvoLivingCell) offspring;
        child.init();
        child.dna.set(this.dna);
        child.dna.mutate();

        world.interpreter().runEmbryo(world, child);
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
