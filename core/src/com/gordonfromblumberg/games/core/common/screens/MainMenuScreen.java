package com.gordonfromblumberg.games.core.common.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import com.gordonfromblumberg.games.core.evocell.world.GameScreen;
import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

public class MainMenuScreen extends AbstractScreen {
    private static final Logger log = LogManager.create(MainMenuScreen.class);
    private static final String LAST_USED_PARAMS_KEY = "last-used-params";
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

        rootTable.add(createSizeTable(uiSkin));

        rootTable.row();
        textButton = new TextButton("PLAY", uiSkin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
//                Preferences prefs = Gdx.app.getPreferences(LAST_USED_PARAMS_KEY);
//                worldParams.save(prefs);
//                prefs.putBoolean("exists", true);
//                prefs.flush();
                Main.getInstance().setScreen(new GameScreen(batch, worldParams));
            }
        });
        rootTable.add(textButton).space(5f);
    }

    private Table createSizeTable(Skin skin) {
        Table table = UIUtils.createTable(skin);
        table.add(new Label("World size", skin))
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
        IntChangeableLabel heightField = new IntChangeableLabel(skin, worldParams::setWidth);
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
        
        return table;
    }

    private void loadDefaults() {
        Preferences lastUsedPrefs = Gdx.app.getPreferences(LAST_USED_PARAMS_KEY);
        if (lastUsedPrefs.getBoolean("exists")) {
            log.debug("Load config from preferences");
            worldParams.load(lastUsedPrefs);
        } else {
            log.debug("Load config from config");
            worldParams.load(AbstractFactory.getInstance().configManager());
        }
    }
}
