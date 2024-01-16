package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.common.utils.Poolable;

public abstract class LivingCell implements Poolable {
    int energy;
    int organics;
    int minerals;
    int age;

    @Override
    public void reset() {
        energy = 0;
        organics = 0;
        minerals = 0;
        age = 0;
    }
}
