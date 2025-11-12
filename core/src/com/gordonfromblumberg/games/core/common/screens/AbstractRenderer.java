package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public abstract class AbstractRenderer implements Renderer {
    private static final Logger log = LogManager.create(AbstractRenderer.class);

    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected boolean centerCamera;

    private final Vector3 temp = new Vector3();

    protected AbstractRenderer() {
        log.info("AbstractRenderer constructor for class " + getClass().getSimpleName());
        this.camera = createCamera();
        this.viewport = createViewport(camera);
    }

    @Override
    public void render(float dt) {
        viewport.apply(centerCamera);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, centerCamera);
    }

    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public void screenToViewport(float x, float y, Vector3 out) {
        viewport.unproject(temp.set(x, y, 0));
        out.set(temp);
    }

    @Override
    public void screenToViewport(Vector3 coords) {
        viewport.unproject(temp.set(coords.x, coords.y, 0));
        coords.set(temp);
    }

    @Override
    public void viewportToScreen(float x, float y, Vector3 out) {
        viewport.project(temp.set(x, y, 0));
        out.set(temp);
    }

    @Override
    public void viewportToScreen(Vector3 coords) {
        viewport.project(temp.set(coords.x, coords.y, 0));
        coords.set(temp);
    }

    protected OrthographicCamera createCamera() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false);
        return camera;
    }

    protected Viewport createViewport(Camera camera) {
        final ConfigManager configManager = AbstractFactory.getInstance().configManager();
        final float worldWidth = configManager.getFloat("worldWidth");
        final float minRatio = configManager.getFloat("minRatio");
        final float maxRatio = configManager.getFloat("maxRatio");
        final float minWorldHeight = worldWidth / maxRatio;
        final float maxWorldHeight = worldWidth / minRatio;
        return new ExtendViewport(worldWidth, minWorldHeight, worldWidth, maxWorldHeight, camera);
    }

    @Override
    public void dispose() {
    }
}
