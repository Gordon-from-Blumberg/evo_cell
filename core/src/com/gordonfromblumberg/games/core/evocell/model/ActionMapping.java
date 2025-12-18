package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

@FunctionalInterface
public interface ActionMapping {
    void act(GameWorld world, EvoBot livingCell, int counter, int parameter);
}
