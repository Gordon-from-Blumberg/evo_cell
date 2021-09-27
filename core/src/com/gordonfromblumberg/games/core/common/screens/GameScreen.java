package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.model.GameWorld;

public class GameScreen extends AbstractScreen {
    private static final String LABEL = "Mouse on ";

    TextureRegion background;
    private GameWorld gameWorld;

    private final Vector3 coords = new Vector3();

    protected GameScreen(SpriteBatch batch) {
        super(batch);

        gameWorld = new GameWorld();
    }

    @Override
    public void initialize() {
        super.initialize();

        background = Main.getInstance().assets()
                .get("image/texture_pack.atlas", TextureAtlas.class)
                .findRegion("background");

        gameWorld.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());

        stage.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                camera.zoom += amountY * 0.25;
                if (camera.zoom <= 0)
                    camera.zoom = 0.25f;
                return true;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        uiViewport.update(width, height, true);
    }

    @Override
    protected void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            camera.translate(-10, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            camera.translate(10, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            camera.translate(0, 10);
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            camera.translate(0, -10);

        super.update(delta);            // apply camera moving and update batch projection matrix
        gameWorld.update(delta);        // update game state
    }

    @Override
    protected void renderWorld(float delta) {
        batch.draw(background, 0, 0);
        gameWorld.render(batch);
    }

    @Override
    protected void renderUi() {
        super.renderUi();
    }

//    @Override
//    protected void createWorldViewport(float worldWidth, float minWorldHeight, float maxWorldHeight) {
//        camera = new OrthographicCamera();
//        camera.setToOrtho(false);
//
//        float worldSize = AbstractFactory.getInstance().configManager().getFloat("game.size");
//        viewport = new ExtendViewport(worldSize, worldSize, camera);
//        viewport.update(Gdx.graphics.getHeight(), Gdx.graphics.getHeight(), true);
//    }

    @Override
    public void dispose() {
        gameWorld.dispose();

        super.dispose();
    }

    @Override
    protected void createUI() {
        super.createUI();

        final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);


    }
}
