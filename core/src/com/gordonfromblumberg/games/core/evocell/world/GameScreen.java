package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.ui.ZoomByScrollListener;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.WorldScreen;

public class GameScreen extends WorldScreen<GameWorld> {
    private final RenderParams renderParams = new RenderParams();

    public GameScreen(SpriteBatch batch, WorldParams worldParams) {
        this(batch, new GameWorld(worldParams));
    }

    protected GameScreen(SpriteBatch batch, GameWorld world) {
        super(batch, world);
    }

    @Override
    protected void initialize() {
        super.initialize();

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        uiRenderer.addListener(new ZoomByScrollListener(worldRenderer.getCamera(), 1.2f,
                configManager.getFloat("minZoom"), configManager.getFloat("maxZoom")));
    }

    @Override
    protected void createWorldRenderer() {
        super.createWorldRenderer();

        worldRenderer = new GameWorldRenderer(world, renderParams);
    }

    @Override
    protected void createUiRenderer() {
        uiRenderer = new GameWorldUIRenderer(batch, world, renderParams, this::getViewCoords3);
        addPauseListener();
    }
}
