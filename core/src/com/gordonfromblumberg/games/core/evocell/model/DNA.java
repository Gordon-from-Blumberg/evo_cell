package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;

import java.util.Iterator;

public class DNA implements Poolable {
    private static final Pool<DNA> pool = new Pool<>() {
        @Override
        protected DNA newObject() {
            return new DNA();
        }
    };
    private static final Logger log = LogManager.create(DNA.class);

    public static final int minGeneCount;
    public static final int maxGeneCount;
    private static final float mutationChance;
    private static final float geneCountChangeChance;
    private static final float geneDuplicateChance;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        minGeneCount = configManager.getInteger("dna.minGeneCount");
        maxGeneCount = configManager.getInteger("dna.maxGeneCount");
        mutationChance = configManager.getFloat("dna.mutationChance");
        geneCountChangeChance = configManager.getFloat("dna.geneCountChangeChance");
        geneDuplicateChance = configManager.getFloat("dna.geneDuplicateChance");
    }

    final Array<Gene> genes = new Array<>();

    private DNA() {
        for (int i = 0; i < minGeneCount; ++i) {
            genes.add(Gene.getInstance());
        }
    }

    public static DNA getInstance() {
        return pool.obtain();
    }

    public void set(DNA original) {
        set(original.genes);
    }

    @SuppressWarnings("unchecked")
    public void set(DNA parent1, DNA parent2) {
        final RandomGen rand = Gene.RAND;
        final Array<Gene> originalGenes = Pools.obtain(Array.class),
                parentGenes1 = parent1.genes,
                parentGenes2 = parent2.genes;
        final int maxParentGeneCount = Math.max(parentGenes1.size, parentGenes2.size);
        for (int i = 0; i < maxParentGeneCount; ++i) {
            Array<Gene> parentGenes = rand.nextBool() ? parentGenes1 : parentGenes2;
            if (i < parentGenes.size) {
                originalGenes.add(parentGenes.get(i));
            }
        }
        set(originalGenes);
        originalGenes.clear();
        Pools.free(originalGenes);
    }

    private void set(Array<Gene> originalGenes) {
        final Array<Gene> thisGenes = this.genes;
        int countToAdd = originalGenes.size - thisGenes.size;
        while (--countToAdd > -1) {
            thisGenes.add(Gene.getInstance());
        }
        for (int i = 0, n = Math.min(originalGenes.size, thisGenes.size); i < n; ++i) {
            thisGenes.get(i).set(originalGenes.get(i));
        }
        int countToRemove = thisGenes.size - originalGenes.size;
        while (--countToRemove > -1) {
            thisGenes.removeIndex(thisGenes.size - 1).release();
        }
    }

    public void setRandom() {
        for (Gene gene : genes) {
            gene.setRandom();
        }
    }

    @SuppressWarnings("unchecked")
    public void mutate() {
        final RandomGen rand = Gene.RAND;
        float mutationChance = DNA.mutationChance;
        Iterator<Gene> geneIterator = genes.iterator();
        final Array<Gene> genesToAdd = Pools.obtain(Array.class);
        while (geneIterator.hasNext()) {
            Gene gene = geneIterator.next();
            if (rand.nextBool(mutationChance)) {
                float mutation = rand.nextFloat();
                if (mutation < 0.01f) {
                    gene.mutate();
                } else if (mutation < geneCountChangeChance) {
                    if (genes.size == maxGeneCount || genes.size > minGeneCount && !rand.nextBool(geneDuplicateChance)) {
                        geneIterator.remove();
                        gene.release();
                    } else {
                        Gene duplicate = Gene.getInstance();
                        duplicate.set(gene);
                        duplicate.mutate();
                        genesToAdd.add(duplicate);
                    }
                } else {
                    gene.mutate();
                }
                mutationChance /= 2;
            }
        }
        genes.addAll(genesToAdd);
        genesToAdd.clear();
        Pools.free(genesToAdd);
    }

    public Gene getGene(int index) {
        return genes.get(index);
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
