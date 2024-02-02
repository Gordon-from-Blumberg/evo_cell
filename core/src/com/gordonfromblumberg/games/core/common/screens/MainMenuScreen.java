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

import java.util.function.IntConsumer;

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

        addMinMaxParameter(table, "Light",
                worldParams::setMinLight, 0, 20, worldParams.getMinLight(),
                worldParams::setMaxLight, 12, 50, worldParams.getMaxLight(), 1);

        addMinMaxParameter(table, "Temperature",
                worldParams::setMinTemperature, -30, 25, worldParams.getMinTemperature(),
                worldParams::setMaxTemperature, 20, 50, worldParams.getMaxTemperature(), 1);
        
        return table;
    }

    private void loadDefaults() {
        worldParams.load(AbstractFactory.getInstance().configManager());
    }

    private void addMinMaxParameter(Table table, String name,
                                    IntConsumer setMin, int minMin, int maxMin, int minValue,
                                    IntConsumer setMax, int minMax, int maxMax, int maxValue,
                                    int step) {
        table.row();
        table.add(name)
                .center().colspan(2);

        table.row();
        IntChangeableLabel minField = new IntChangeableLabel(table.getSkin(), setMin);
        minField.setMinValue(minMin);
        minField.setMaxValue(maxMin);
        minField.setFieldWidth(FIELD_WIDTH);
        minField.setFieldDisabled(false);
        minField.setStep(step);
        minField.setValue(minValue);
        table.add(minField)
                .left();
        table.add("Min")
                .left();

        table.row();
        IntChangeableLabel maxField = new IntChangeableLabel(table.getSkin(), setMax);
        maxField.setMinValue(minMax);
        maxField.setMaxValue(maxMax);
        maxField.setFieldWidth(FIELD_WIDTH);
        maxField.setFieldDisabled(false);
        maxField.setStep(step);
        maxField.setValue(maxValue);
        table.add(maxField)
                .left();
        table.add("Max")
                .left();
    }
}
