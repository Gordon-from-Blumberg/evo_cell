package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Poolable;
import com.gordonfromblumberg.games.core.common.utils.RandomGen;

public class Gene implements Poolable {
    private static final Pool<Gene> pool = new Pool<>() {
        @Override
        protected Gene newObject() {
            return new Gene();
        }
    };
    private static final Logger log = LogManager.create(Gene.class);

    static final RandomGen RAND = RandomGen.INSTANCE;
    static final int geneValueCount;

    static {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        geneValueCount = configManager.getInteger("dna.geneValueCount");
    }

    private final byte[] values = new byte[geneValueCount];

    private Gene() {}

    public static Gene getInstance() {
        return pool.obtain();
    }

    void setRandom() {
        for (int i = 0; i < geneValueCount; ++i) {
            values[i] = RAND.nextByte();
        }
    }

    void mutate() {
        values[RAND.nextInt(geneValueCount)] = RAND.nextByte();
    }

    void set(Gene other) {
        System.arraycopy(other.values, 0, this.values, 0, geneValueCount);
    }

    public byte getValue(int index) {
        return values[index];
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        for (int i = 0; i < 4; ++i) {
            values[i] = -1;
        }
    }
}
