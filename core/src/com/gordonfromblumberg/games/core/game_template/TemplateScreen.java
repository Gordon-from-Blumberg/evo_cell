package com.gordonfromblumberg.games.core.game_template;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.world.*;

public class TemplateScreen extends WorldScreen<TemplateWorld> {
    private static final Logger log = LogManager.create(TemplateScreen.class);

    public TemplateScreen(SpriteBatch batch) {
        super(batch, new TemplateWorld());
    }

    @Override
    protected void initialize() {
        super.initialize();
        log.info("World screen init for " + getClass().getSimpleName());

        ((TemplateRenderer) worldRenderer).initialize();

        uiRenderer.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!event.isHandled()) {
                    x = Gdx.input.getX();
                    y = Gdx.input.getY();
                    worldRenderer.screenToViewport(x, y, viewCoords3);
                    worldRenderer.viewToWorld(viewCoords3.x, viewCoords3.y, worldCoords3);
                    ((TemplateRenderer) worldRenderer).click(worldCoords3.x, worldCoords3.y);
                    world.click(Input.Buttons.LEFT, worldCoords3.x, worldCoords3.y);
                }
            }
        });

        uiRenderer.addListener(new ClickListener(Input.Buttons.RIGHT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!event.isHandled()) {
                    x = Gdx.input.getX();
                    y = Gdx.input.getY();
//                screenToViewport(x, y, viewCoords3);
//                renderer.screenToWorld(worldCoords3.set(viewCoords3));
//                renderer.click(Input.Buttons.LEFT, viewCoords3.x, viewCoords3.y);
//                screenToWorld(x, y, worldCoords3);
//                gameWorld.click(Input.Buttons.RIGHT, worldCoords3.x, worldCoords3.y);
                }
            }
        });
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

    @Override
    protected void createWorldRenderer() {
        log.info("GameScreen.createWorldRenderer");

        worldRenderer = new TemplateRenderer(world, batch);
    }

    @Override
    protected void createUiRenderer() {
        log.info("WorldScreen.createUiRenderer for " + getClass().getSimpleName());

        uiRenderer = new TemplateUIRenderer(batch, world, this::getViewCoords3);

        final ConfigManager configManager = AbstractFactory.getInstance().configManager();

        final float minZoom = configManager.getFloat("minZoom");
        final float maxZoom = configManager.getFloat("maxZoom");
        final OrthographicCamera camera = worldRenderer.getCamera();
        uiRenderer.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (amountY > 0)
                    camera.zoom *= 1.25f;
                else if (amountY < 0)
                    camera.zoom /= 1.25f;
                if (camera.zoom < minZoom)
                    camera.zoom = minZoom;
                if (camera.zoom > maxZoom)
                    camera.zoom = maxZoom;
                return true;
            }
        });

        uiRenderer.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE) {
                    world.pause();
                    return true;
                }
                return false;
            }
        });
    }

    public Vector3 getViewCoords3() {
        return viewCoords3;
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        uiRenderer.dispose();
    }
}
