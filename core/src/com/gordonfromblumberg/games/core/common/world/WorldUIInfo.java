package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public interface WorldUIInfo<T extends World> {
    SpriteBatch getBatch();
    T getWorld();
    void worldToView(Vector3 coords);
    void worldToScreen(Vector3 coords);
}
