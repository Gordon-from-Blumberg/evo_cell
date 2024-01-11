package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;
import com.gordonfromblumberg.games.core.common.utils.Poolable;

public class LivingCell implements Poolable {
    private static final Pool<LivingCell> pool = new Pool<>() {
        @Override
        protected LivingCell newObject() {
            return new LivingCell();
        }
    };

    int energy;
    int age;
    final DNA dna = new DNA();

    private LivingCell() { }

    public static LivingCell getInstance() {
        return pool.obtain();
    }

    @Override
    public void release() {
        pool.free(this);
    }

    @Override
    public void reset() {
        energy = 0;
        age = 0;
        dna.reset();
    }
}
