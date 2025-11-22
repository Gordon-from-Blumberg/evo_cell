package com.gordonfromblumberg.games.core.evocell.model;

import com.gordonfromblumberg.games.core.evocell.world.GameWorld;

@FunctionalInterface
public interface ActionMapping {
    void act(GameWorld world, EvoLivingCell livingCell, int parameter);
}
