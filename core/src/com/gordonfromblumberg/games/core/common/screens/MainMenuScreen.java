package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;
import com.gordonfromblumberg.games.core.common.ui.IntChangeableLabel;
import com.gordonfromblumberg.games.core.common.ui.UIUtils;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.evocell.world.GameScreen;
import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class MainMenuScreen extends AbstractScreen {
    private static final Logger log = LogManager.create(MainMenuScreen.class);
    // UI constants
    private static final float FIELD_WIDTH = 50f;

    private final WorldParams worldParams = new WorldParams();
    TextButton textButton;

    public MainMenuScreen(SpriteBatch batch) {
        super(batch);

        color = Color.FOREST;

        log.debug("Local storage path = " + Gdx.files.getLocalStoragePath());
        log.debug("External storage path = " + Gdx.files.getExternalStoragePath());
    }

    @Override
    protected void update(float delta) {
    }

    @Override
    protected void createUiRenderer() {
        super.createUiRenderer();

        loadDefaults();
        final Skin uiSkin = assets.get("ui/uiskin.json", Skin.class);
        final Table rootTable = uiRenderer.rootTable;

        rootTable.add(createWorldParamsTable(uiSkin));

        rootTable.row();
        textButton = new TextButton("PLAY", uiSkin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ConfigManager config = AbstractFactory.getInstance().configManager();
                worldParams.save(config.getConfigPreferences());
                config.flushPreferences();
                Main.getInstance().setScreen(new GameScreen(batch, worldParams));
            }
        });
        rootTable.add(textButton).space(5f);
    }

    private Table createWorldParamsTable(Skin skin) {
        Table table = UIUtils.createTable(skin);
        table.add("World size")
                .center().colspan(2);

        table.row();
        IntChangeableLabel widthField = new IntChangeableLabel(skin, worldParams::setWidth);
        widthField.setMinValue(100);
        widthField.setMaxValue(500);
        widthField.setFieldWidth(FIELD_WIDTH);
        widthField.setFieldDisabled(false);
        widthField.setStep(10);
        widthField.setValue(worldParams.getWidth());
        table.add(widthField)
                .left();
        table.add("Width")
                .left();

        table.row();
        IntChangeableLabel heightField = new IntChangeableLabel(skin, worldParams::setHeight);
        heightField.setMinValue(100);
        heightField.setMaxValue(500);
        heightField.setFieldWidth(FIELD_WIDTH);
        heightField.setFieldDisabled(false);
        heightField.setStep(10);
        heightField.setValue(worldParams.getHeight());
        table.add(heightField)
                .left();
        table.add("Height")
                .left();

        table.row();
        table.add("Light")
                .center().colspan(2);

        table.row();
        IntChangeableLabel minLightField = new IntChangeableLabel(skin, worldParams::setMinLight);
        minLightField.setMinValue(0);
        minLightField.setMaxValue(20);
        minLightField.setFieldWidth(FIELD_WIDTH);
        minLightField.setFieldDisabled(false);
        minLightField.setStep(1);
        minLightField.setValue(worldParams.getMinLight());
        table.add(minLightField)
                .left();
        table.add("Min")
                .left();

        table.row();
        IntChangeableLabel maxLightField = new IntChangeableLabel(skin, worldParams::setMaxLight);
        maxLightField.setMinValue(12);
        maxLightField.setMaxValue(50);
        maxLightField.setFieldWidth(FIELD_WIDTH);
        maxLightField.setFieldDisabled(false);
        maxLightField.setStep(1);
        maxLightField.setValue(worldParams.getMaxLight());
        table.add(maxLightField)
                .left();
        table.add("Max")
                .left();
        
        return table;
    }

    private void loadDefaults() {
        worldParams.load(AbstractFactory.getInstance().configManager());
    }
}
