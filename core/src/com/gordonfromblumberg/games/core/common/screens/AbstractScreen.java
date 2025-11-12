package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

public abstract class AbstractScreen implements Screen {
    private static final Logger log = LogManager.create(AbstractScreen.class);
    private static final float MAX_DELTA = 1.0f / 30;

    protected static int screenWidth;
    protected static int screenHeight;

    protected SpriteBatch batch;
    protected Color color = Color.BLACK;

    protected UIRenderer uiRenderer;

    protected boolean initialized;

    protected AbstractScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    protected void initialize() {
        log.info("AbstractScreen.initialize for " + getClass().getSimpleName());

        uiRenderer = createUiRenderer();

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        uiRenderer.addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (keycode == Input.Keys.F11) {
                    if (Gdx.graphics.isFullscreen()) {
                        Gdx.graphics.setWindowedMode(screenWidth, screenHeight);
                    } else {
                        screenWidth = Gdx.graphics.getWidth();
                        screenHeight = Gdx.graphics.getHeight();
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    }

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void show() {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        uiRenderer.setAsInputProcessor();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, MAX_DELTA);

        Gdx.gl.glClearColor(color.r, color.g, color.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        renderWorld(delta);

        uiRenderer.render(delta);
    }

    protected void renderWorld(float delta) {}

    @Override
    public void resize(int width, int height) {
        uiRenderer.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    protected void update(float delta) {
    }

    protected UIRenderer createUiRenderer() {
        log.info("AbstractScreen.createUiRenderer for " + getClass().getSimpleName());

        return new UIRenderer(batch);
    }

    @Override
    public void dispose() {
        uiRenderer.dispose();
    }
}
