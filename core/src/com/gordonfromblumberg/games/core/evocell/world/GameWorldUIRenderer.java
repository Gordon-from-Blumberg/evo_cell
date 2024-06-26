package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.ui.UpdatableLabel;
import com.gordonfromblumberg.games.core.common.world.WorldUIRenderer;
import com.gordonfromblumberg.games.core.evocell.model.Cell;
import com.gordonfromblumberg.games.core.evocell.model.LivingCell;
import com.gordonfromblumberg.games.core.evocell.utils.ECUIUtils;

import java.util.function.Function;
import java.util.function.Supplier;

public class GameWorldUIRenderer extends WorldUIRenderer<GameWorld> {

    private final RenderParams renderParams;

    public GameWorldUIRenderer(SpriteBatch batch, GameWorld world, RenderParams renderParams, Supplier<Vector3> viewCoords) {
        super(batch, world, viewCoords);

        this.renderParams = renderParams;

        final AssetManager assets = Main.getInstance().assets();
        final Skin skin = assets.get("ui/uiskin.json", Skin.class);
        stage.addActor(createSelectedCellWindow(skin));
        stage.addActor(createWorldStatisticWindow(skin));
        stage.addActor(createRenderParamsWindow(skin));
        stage.addActor(createWorldParamsWindow(skin));
    }

    private Window createSelectedCellWindow(Skin skin) {
        final Window window = new Window("Cell", skin);
        window.setY(220f);
        window.setWidth(250f);
        window.setHeight(250f);
        window.defaults().align(Align.right).spaceRight(2f);

        window.add("Parameter");
        window.add("World").width(60f);
        window.add("Cell").width(70f);

        window.row();
        window.add("Coords / id");
        window.add(createCellInfo(skin, cell -> cell.getX() + ", " + cell.getY()));
        window.add(createLivCellInfo(skin, LivingCell::getId));

        window.row();
        window.add("Light / age");
        window.add(createCellInfo(skin, Cell::getSunLight));
        window.add(createLivCellInfo(skin, LivingCell::getAge));

        window.row();
        window.add("Energy");
        window.add(createCellInfo(skin, Cell::getEnergy));
        window.add(createLivCellInfo(skin, LivingCell::getEnergy));

        window.row();
        window.add("Organics");
        window.add(createCellInfo(skin, Cell::getOrganics));
        window.add(createLivCellInfo(skin, LivingCell::getOrganics));

        window.row();
        window.add("Minerals");
        window.add(createCellInfo(skin, Cell::getMinerals));
        window.add(createLivCellInfo(skin, LivingCell::getMinerals));

        window.row();
        window.add("Temperature");
        window.add(createCellInfo(skin, Cell::getTemperature));
        window.add(createLivCellInfo(skin, lc -> lc.getTemperature() + " / " + lc.getWishedTemperature()));

        window.row();
        window.add("Water");
        window.add(createCellInfo(skin, Cell::getWater));
        window.add(createLivCellInfo(skin, LivingCell::getWater));
        return window;
    }

    private Window createWorldStatisticWindow(Skin skin) {
        final Window window = new Window("World statistic", skin);
        window.setWidth(300f);
        window.setHeight(200f);
        window.defaults().align(Align.right).spaceRight(2f);

        window.add("Turn");
        window.add(new UpdatableLabel(skin, world::getTurn));

        window.row().padTop(10f);
        window.add("Resource");
        window.add("Total in world");
        window.add("Total in cells");

        window.row();
        window.add("Energy");
        window.add(new UpdatableLabel(skin, () -> world.statistic.worldEnergy));
        window.add(new UpdatableLabel(skin, () -> world.statistic.totalCellEnergy));

        window.row();
        window.add("Organics");
        window.add(new UpdatableLabel(skin, () -> world.statistic.worldOrganics));
        window.add(new UpdatableLabel(skin, () -> world.statistic.totalCellOrganics));

        window.row();
        window.add("Minerals");
        window.add(new UpdatableLabel(skin, () -> world.statistic.worldMinerals));
        window.add(new UpdatableLabel(skin, () -> world.statistic.totalCellMinerals));

        window.row().padTop(10f);
        window.add("Cells");
        window.add(new UpdatableLabel(skin, () -> world.statistic.cellCount));
        return window;
    }

    private Window createRenderParamsWindow(Skin skin) {
        final Window window = new Window("Render params", skin);
        window.setX(viewport.getWorldWidth() - window.getX());
        window.defaults().left();

        CheckBox lightCheckBox = new CheckBox("Light", skin);
        lightCheckBox.setChecked(renderParams.renderLight);
        lightCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                renderParams.renderLight = ((CheckBox) event.getListenerActor()).isChecked();
            }
        });
        window.add(lightCheckBox);

        window.row();
        CheckBox mineralsCheckBox = new CheckBox("Minerals", skin);
        mineralsCheckBox.setChecked(renderParams.renderMinerals);
        mineralsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                renderParams.renderMinerals = ((CheckBox) event.getListenerActor()).isChecked();
            }
        });
        window.add(mineralsCheckBox);

        window.row();
        CheckBox temperatureCheckBox = new CheckBox("Temperature", skin);
        temperatureCheckBox.setChecked(renderParams.renderTemperature);
        temperatureCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                renderParams.renderTemperature = ((CheckBox) event.getListenerActor()).isChecked();
            }
        });
        window.add(temperatureCheckBox);
        return window;
    }

    private Window createWorldParamsWindow(Skin skin) {
        Window window = new Window("World params", skin);
        window.setX(viewport.getWorldWidth() - window.getX());
        window.setY(300f);
        window.setHeight(200f);
        window.defaults().right();

        ECUIUtils.addLightField(window, world.params);
        ECUIUtils.addTemperatureField(window, world.params);

        return window;
    }

    private UpdatableLabel createCellInfo(Skin skin, Function<Cell, Object> getter) {
        return new UpdatableLabel(skin, () -> world.selectedCell == null ? "-" : getter.apply(world.selectedCell));
    }

    private UpdatableLabel createLivCellInfo(Skin skin, Function<LivingCell, Object> getter) {
        return new UpdatableLabel(skin, () -> world.selectedCell == null || world.selectedCell.getObject() == null
                ? "-" : getter.apply(world.selectedCell.getObject()));
    }
}
