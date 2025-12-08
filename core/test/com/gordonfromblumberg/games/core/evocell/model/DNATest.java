package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.factory.TestFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DNATest {

    @BeforeAll
    static void init() {
        TestFactory.init();
        ConfigManager config = AbstractFactory.getInstance().configManager();
        config.setInteger("dna.minGeneCount", 4);
        config.setInteger("dna.maxGeneCount", 10);
        config.setFloat("dna.mutationChance", 0.02f);
        config.setFloat("dna.geneCountChangeChance", 0.1f);
        config.setFloat("dna.geneDuplicateChance", 0.6f);
        config.setInteger("dna.geneValueCount", 16);
    }

    @Test
    void setWith0() {
        DNA dna1 = DNA.getInstance();
        DNA dna2 = DNA.getInstance();

        dna2.getGene(0).set(1, 2, 3, 4, 5, 6, 7, 8);
        dna2.getGene(2).set(8, 7, 6, 5, 4, 3, 2, 1);

        Assertions.assertEquals(4, dna1.genes.size);
        Assertions.assertEquals(4, dna2.genes.size);

        dna1.set(dna2);

        Assertions.assertEquals(4, dna1.genes.size);
        Assertions.assertEquals(4, dna2.genes.size);

        for (int i = 0; i < 8; ++i) {
            Assertions.assertEquals(i + 1, dna2.getGene(0).getValue(i));
            Assertions.assertEquals(8 - i, dna2.getGene(2).getValue(i));
        }
    }

    @Test
    void setWithPlus1() {
        DNA dna1 = DNA.getInstance();
        DNA dna2 = DNA.getInstance();

        dna2.getGene(0).set(1, 2, 3, 4, 5, 6, 7, 8);
        dna2.getGene(2).set(8, 7, 6, 5, 4, 3, 2, 1);
        dna2.genes.add(Gene.getInstance());

        Assertions.assertEquals(4, dna1.genes.size);
        Assertions.assertEquals(5, dna2.genes.size);

        dna1.set(dna2);

        Assertions.assertEquals(5, dna1.genes.size);
        Assertions.assertEquals(5, dna2.genes.size);

        for (int i = 0; i < 8; ++i) {
            Assertions.assertEquals(i + 1, dna2.getGene(0).getValue(i));
            Assertions.assertEquals(8 - i, dna2.getGene(2).getValue(i));
        }
    }

    @Test
    void setWithPlus3() {
        DNA dna1 = DNA.getInstance();
        DNA dna2 = DNA.getInstance();

        dna2.getGene(0).set(1, 2, 3, 4, 5, 6, 7, 8);
        dna2.getGene(2).set(8, 7, 6, 5, 4, 3, 2, 1);
        dna2.genes.add(Gene.getInstance());
        dna2.genes.add(Gene.getInstance());
        dna2.genes.add(Gene.getInstance());

        Assertions.assertEquals(4, dna1.genes.size);
        Assertions.assertEquals(7, dna2.genes.size);

        dna1.set(dna2);

        Assertions.assertEquals(7, dna1.genes.size);
        Assertions.assertEquals(7, dna2.genes.size);

        for (int i = 0; i < 8; ++i) {
            Assertions.assertEquals(i + 1, dna2.getGene(0).getValue(i));
            Assertions.assertEquals(8 - i, dna2.getGene(2).getValue(i));
        }
    }

    @Test
    void setWithMinus1() {
        DNA dna1 = DNA.getInstance();
        DNA dna2 = DNA.getInstance();

        dna2.getGene(0).set(1, 2, 3, 4, 5, 6, 7, 8);
        dna2.getGene(2).set(8, 7, 6, 5, 4, 3, 2, 1);
        dna1.genes.add(Gene.getInstance());

        Assertions.assertEquals(5, dna1.genes.size);
        Assertions.assertEquals(4, dna2.genes.size);

        dna1.set(dna2);

        Assertions.assertEquals(4, dna1.genes.size);
        Assertions.assertEquals(4, dna2.genes.size);

        for (int i = 0; i < 8; ++i) {
            Assertions.assertEquals(i + 1, dna2.getGene(0).getValue(i));
            Assertions.assertEquals(8 - i, dna2.getGene(2).getValue(i));
        }
    }

    @Test
    void setWithMinus3() {
        DNA dna1 = DNA.getInstance();
        DNA dna2 = DNA.getInstance();

        dna2.getGene(0).set(1, 2, 3, 4, 5, 6, 7, 8);
        dna2.getGene(2).set(8, 7, 6, 5, 4, 3, 2, 1);
        dna1.genes.add(Gene.getInstance());
        dna1.genes.add(Gene.getInstance());
        dna1.genes.add(Gene.getInstance());

        Assertions.assertEquals(7, dna1.genes.size);
        Assertions.assertEquals(4, dna2.genes.size);

        dna1.set(dna2);

        Assertions.assertEquals(4, dna1.genes.size);
        Assertions.assertEquals(4, dna2.genes.size);

        for (int i = 0; i < 8; ++i) {
            Assertions.assertEquals(i + 1, dna2.getGene(0).getValue(i));
            Assertions.assertEquals(8 - i, dna2.getGene(2).getValue(i));
        }
    }
}
