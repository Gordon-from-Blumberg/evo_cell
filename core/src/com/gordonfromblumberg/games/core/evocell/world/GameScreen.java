package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gordonfromblumberg.games.core.common.world.WorldScreen;

public class GameScreen extends WorldScreen<GameWorld> {

    public GameScreen(SpriteBatch batch, WorldParams worldParams) {
        this(batch, new GameWorld(worldParams));
    }

    protected GameScreen(SpriteBatch batch, GameWorld world) {
        super(batch, world);
    }

    @Override
    protected void createWorldRenderer() {
        super.createWorldRenderer();

        worldRenderer = new GameWorldRenderer(world, batch);
    }
}
