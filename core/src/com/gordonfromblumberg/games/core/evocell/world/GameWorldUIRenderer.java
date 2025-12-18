package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.gordonfromblumberg.games.core.common.ui.UpdatableLabel;
import com.gordonfromblumberg.games.core.common.utils.Assets;
import com.gordonfromblumberg.games.core.common.world.WorldUIInfo;
import com.gordonfromblumberg.games.core.common.world.WorldUIRenderer;
import com.gordonfromblumberg.games.core.evocell.model.Bot;
import com.gordonfromblumberg.games.core.evocell.model.Cell;
import com.gordonfromblumberg.games.core.evocell.ui.BotInfoWindow;
import com.gordonfromblumberg.games.core.evocell.utils.ECUIUtils;

import java.util.function.Consumer;
import java.util.function.Function;

public class GameWorldUIRenderer extends WorldUIRenderer<GameWorld> {

    private final RenderParams renderParams;
    private final BotInfoWindow botInfoWindow;

    public GameWorldUIRenderer(WorldUIInfo<GameWorld> worldInfo, RenderParams renderParams) {
        super(worldInfo);

        this.renderParams = renderParams;

        final AssetManager assets = Assets.manager();
        final Skin skin = assets.get("ui/uiskin.json", Skin.class);
        botInfoWindow = createBotInfoWindow(skin);
        stage.addActor(createSelectedCellWindow(skin));
        stage.addActor(createWorldStatisticWindow(skin));
        stage.addActor(createRenderParamsWindow(skin));
        stage.addActor(createWorldParamsWindow(skin));

        stage.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getTarget() == stage.getRoot()) {
                    botInfoWindow.show(world.selectedCell.getBot(), stage);
                    if (world.selectedCell.getBot() != null) {
                        world.pause();
                    }
                }
            }
        });
    }

    @Override
    public void render(float dt) {
        super.render(dt);
    }

    private BotInfoWindow createBotInfoWindow(Skin skin) {
        return new BotInfoWindow("Bot info", skin);
    }

    private Window createSelectedCellWindow(Skin skin) {
        final Window window = new Window("Cell", skin);
        window.setY(220f);
        window.setWidth(250f);
        window.setHeight(250f);
        window.defaults().align(Align.right).spaceRight(2f);

        window.add("Parameter");
        window.add("World").width(60f);
        window.add("Bot").width(70f);

        window.row();
        window.add("Coords / id");
        window.add(createCellInfo(skin, cell -> cell.getX() + ", " + cell.getY()));
        window.add(createLivCellInfo(skin, Bot::getId));

        window.row();
        window.add("HP");
        window.add(createCellInfo(skin, cell -> '-'));
        window.add(createLivCellInfo(skin, Bot::getHp));

        window.row();
        window.add("Light / age");
        window.add(createCellInfo(skin, Cell::getSunLight));
        window.add(createLivCellInfo(skin, Bot::getAge));

        window.row();
        window.add("Energy / cons");
        window.add(createCellInfo(skin, Cell::getEnergy));
        window.add(createLivCellInfo(skin, lc -> lc.getEnergy() + " / " + lc.getEnergyConsumption()));

        window.row();
        window.add("Organics");
        window.add(createCellInfo(skin, Cell::getOrganics));
        window.add(createLivCellInfo(skin, Bot::getOrganics));

        window.row();
        window.add("Minerals");
        window.add(createCellInfo(skin, Cell::getMinerals));
        window.add(createLivCellInfo(skin, Bot::getMinerals));

        window.row();
        window.add("Temperature");
        window.add(createCellInfo(skin, Cell::getTemperature));
        window.add(createLivCellInfo(skin, lc -> lc.getTemperature() + " / " + lc.getWishedTemperature()));

        window.row();
        window.add("Water");
        window.add(createCellInfo(skin, Cell::getWater));
        window.add(createLivCellInfo(skin, Bot::getWater));
        return window;
    }

    private Window createWorldStatisticWindow(Skin skin) {
        final Window window = new Window("World statistic", skin);
        window.setWidth(300f);
        window.setHeight(200f);
        window.defaults().align(Align.right).spaceRight(2f);

        window.add("Turn");
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.getTurn()))));

        window.row().padTop(10f);
        window.add("Resource");
        window.add("Total in world");
        window.add("Total in bots");

        window.row();
        window.add("Energy");
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.worldEnergy))));
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.totalCellEnergy))));

        window.row();
        window.add("Organics");
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.worldOrganics))));
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.totalCellOrganics))));

        window.row();
        window.add("Minerals");
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.worldMinerals))));
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.totalCellMinerals))));

        window.row().padTop(10f);
        window.add("Bots");
        window.add(new UpdatableLabel(skin, withClear(sb -> sb.append(world.statistic.cellCount))));
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
        return new UpdatableLabel(skin, withClear(sb -> {
            if (world.selectedCell == null)
                sb.append('-');
            else
                sb.append(getter.apply(world.selectedCell));
        }));
    }

    private UpdatableLabel createLivCellInfo(Skin skin, Function<Bot, Object> getter) {
        return new UpdatableLabel(skin, withClear(sb -> {
            if (world.selectedCell == null || world.selectedCell.getBot() == null)
                sb.append('-');
            else
                sb.append(getter.apply(world.selectedCell.getBot()));
        }));
    }

    private Consumer<StringBuilder> withClear(Consumer<StringBuilder> printer) {
        return sb -> {
            sb.clear();
            printer.accept(sb);
        };
    }
}
