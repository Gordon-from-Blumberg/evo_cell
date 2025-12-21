package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

import static com.gordonfromblumberg.games.core.common.utils.MathHelper.modPos;

public class EvoBot extends Bot {
    private static final Pool<EvoBot> pool = new Pool<>() {
        @Override
        protected EvoBot newObject() {
            return new EvoBot();
        }
    };

    final DNA dna = DNA.getInstance();
    byte activeGeneIndex;
    Object contextObject;

    private EvoBot() { }

    public static EvoBot getInstance() {
        return pool.obtain();
    }

    public void setRandomDna() {
        dna.setRandom();
    }

    public byte getActiveGeneIndex() {
        return activeGeneIndex;
    }

    public void setActiveGeneIndex(int index) {
        this.activeGeneIndex = (byte) modPos(index, dna.genes.size);
    }

    public void setGene(int geneIndex, int... geneValues) {
        dna.getGene(geneIndex).set(geneValues);
    }

    @Override
    protected void _update(GameWorld world) {
        contextObject = getForwardCell(world.getGrid());
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
    protected void initOffspring(GameWorld world, Bot offspring) {
        final EvoBot child = (EvoBot) offspring;
        child.init();
        child.dna.set(this.dna);
        child.dna.mutate();
        child.setActiveGeneIndex(1);
        world.interpreter().runEmbryo(world, child);
    }

    @Override
    protected EvoBot getOffspringInstance() {
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
