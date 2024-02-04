package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;

public class DNA implements Poolable {
    private static final Pool<DNA> pool = new Pool<>() {
        @Override
        protected DNA newObject() {
            return new DNA();
        }
    };
    private static final Logger log = LogManager.create(DNA.class);

    public static final int geneCount;
    private static final float mutationChance;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        geneCount = configManager.getInteger("dna.geneCount");
        mutationChance = configManager.getFloat("dna.mutationChance");
    }

    private final Gene[] genes = new Gene[geneCount];

    private DNA() {
        for (int i = 0; i < geneCount; ++i) {
            genes[i] = new Gene();
        }
    }

    public static DNA getInstance() {
        return pool.obtain();
    }

    public void set(DNA original) {
        for (int i = 0; i < geneCount; ++i) {
            this.genes[i].set(original.genes[i]);
        }
    }

    public void set(DNA parent1, DNA parent2) {
        final RandomGen rand = Gene.RAND;
        for (int i = 0; i < geneCount; ++i) {
            DNA parent = rand.nextBool() ? parent1 : parent2;
            this.genes[i].set(parent.genes[i]);
        }
    }

    public void setRandom() {
        for (int i = 0; i < geneCount; ++i) {
            genes[i].setRandom();
        }
    }

    public void mutate() {
        final RandomGen rand = Gene.RAND;
        for (Gene gene : genes) {
            if (rand.nextBool(mutationChance)) {
                gene.mutate();
                log.trace("Gene has mutated");
            }
        }
    }

    public Gene getGene(int index) {
        return genes[index];
    }

    @Override
    public void reset() {
        for (Gene gene : genes) {
            gene.reset();
        }
    }

    @Override
    public void release() {
        pool.free(this);
    }
}
