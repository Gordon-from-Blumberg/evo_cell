package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.Pool;

public class SimpleLivingCell extends LivingCell {
    private static final Pool<SimpleLivingCell> pool = new Pool<>() {
        @Override
        protected SimpleLivingCell newObject() {
            return new SimpleLivingCell();
        }
    };

    public static SimpleLivingCell getInstance() {
        return pool.obtain();
    }

    @Override
    public void release() {
        pool.free(this);
    }
}
