package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.gordonfromblumberg.games.core.common.debug.DebugOptions;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.screens.AbstractScreen;

public abstract class WorldScreen<T extends World> extends AbstractScreen {
    private static final Logger log = LogManager.create(WorldScreen.class);

    protected final T world;
    protected WorldRenderer<T> worldRenderer;

    protected final Vector3 viewCoords3 = new Vector3();
    protected final Vector3 worldCoords3 = new Vector3();

    protected WorldScreen(SpriteBatch batch, T world) {
        super(batch);
        log.info("World screen constructor for " + getClass().getSimpleName());
        this.world = world;
    }

    @Override
    protected void initialize() {
        world.initialize();
        // create world renderer before ui
        createWorldRenderer();
        super.initialize();
        log.info("World screen init for " + getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void update(float delta) {
        super.update(delta);
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        worldRenderer.screenToViewport(x, y, viewCoords3);
        worldRenderer.viewToWorld(viewCoords3.x, viewCoords3.y, worldCoords3);
        world.update(delta, worldCoords3.x, worldCoords3.y);        // update game state

        if (DebugOptions.DEBUG) {
            WorldCameraParams worldCameraParams = ((WorldUIRenderer<T>) uiRenderer).getWorldCameraParams();
            worldCameraParams.position.set(worldRenderer.getCamera().position);
            worldCameraParams.zoom = worldRenderer.getCamera().zoom;
        }
    }

    @Override
    protected void renderWorld(float delta) {
        worldRenderer.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.resize(width, height);
        uiRenderer.resize(width, height);
    }

    protected abstract void createWorldRenderer();

    @Override
    protected WorldUIRenderer<T> createUiRenderer() {
        log.info("WorldScreen.createUiRenderer for " + getClass().getSimpleName());

        return new WorldUIRenderer<>(getInfo());
    }

    protected WorldUIInfo<T> getInfo() {
        return new WorldUIInfo<>() {
            @Override
            public SpriteBatch getBatch() {
                return batch;
            }

            @Override
            public T getWorld() {
                return world;
            }

            @Override
            public void worldToView(Vector3 coords) {
                worldRenderer.worldToView(coords);
            }

            @Override
            public void worldToScreen(Vector3 coords) {
                worldRenderer.worldToScreen(coords);
            }
        };
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        uiRenderer.dispose();
    }
}
