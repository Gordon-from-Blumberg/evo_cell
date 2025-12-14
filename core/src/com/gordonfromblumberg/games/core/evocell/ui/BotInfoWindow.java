package com.gordonfromblumberg.games.core.evocell.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.gordonfromblumberg.games.core.common.ui.TabbedPane;
import com.gordonfromblumberg.games.core.evocell.model.LivingCell;
import com.gordonfromblumberg.games.core.evocell.model.LivingCellParameters;
import com.gordonfromblumberg.games.core.evocell.model.SimpleLivingCell;

public class BotInfoWindow extends Window {
    private final Label botLabel;
    private final TabbedPane<TextButton> infoPane;
    private final Container<Table> propertiesPane = new Container<>();
    private final ScrollPane parametersPane;
    private final ScrollPane genomePane;

    public BotInfoWindow(String title, Skin skin) {
        super(title, skin);

        columnDefaults(0).maxWidth(500f);
        propertiesPane.align(Align.center);

        parametersPane = new ScrollPane(null, skin);
        parametersPane.setScrollingDisabled(true, false);
        genomePane = new ScrollPane(null, skin);
        genomePane.setScrollingDisabled(true, false);

        botLabel = new Label("", skin);
        add(botLabel).align(Align.center);

        infoPane = new TabbedPane<>();
        row().maxHeight(500f);
        add(infoPane).align(Align.center);

        setPanes(skin);

        addListener(new InputListener() {
            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (Input.Keys.ESCAPE == keycode) {
                    close();
                    return true;
                }
                return false;
            }
        });
    }

    public void show(LivingCell bot, Stage stage) {
        if (bot == null) {
            if (getStage() != null) {
                close();
            }
            return;
        }

        StringBuilder botLabelSb = botLabel.getText();
        botLabelSb.clear();
        botLabelSb.append(bot instanceof SimpleLivingCell ? "Simple" : "Evo")
                .append(" bot #")
                .append(bot.getId());
        botLabel.invalidateHierarchy();

        fillProperties(bot);
        fillParameters(bot);
        fillGenome(bot);

        invalidate();
        pack();
        stage.addActor(this);
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));

    }

    public void close() {
        remove();
    }

    private void setPanes(Skin skin) {
        infoPane.addPane(new TextButton("Properties", skin), propertiesPane);
        infoPane.addPane(new TextButton("Parameters", skin), parametersPane);
        infoPane.addPane(new TextButton("Genome", skin), genomePane);
    }

    private void fillProperties(LivingCell bot) {
        Table table = new Table(getSkin());
        table.columnDefaults(0).align(Align.right);
        table.columnDefaults(1).align(Align.left).padLeft(8f);

        table.add("HP");
        table.add(bot.getHp() + "/" + LivingCell.maxHp);

        table.row();
        table.add("Energy / consumption");
        table.add(bot.getEnergy() + "/" + bot.getEnergyConsumption());

        table.row();
        table.add("Organics");
        table.add(String.valueOf(bot.getOrganics()));

        table.row();
        table.add("Minerals");
        table.add(String.valueOf(bot.getMinerals()));

        table.row();
        table.add("Age");
        table.add(String.valueOf(bot.getAge()));

        table.row();
        table.add("Temperature / wished");
        table.add(bot.getTemperature() + "/" + bot.getWishedTemperature());

        table.row();
        table.add("Heat");
        table.add(String.valueOf(bot.getHeat()));

        table.row();
        table.add("Water");
        table.add(String.valueOf(bot.getWater()));

        table.row();
        table.add("Mass");
        table.add(String.valueOf(bot.mass()));

        table.row();
        table.add("Cell light");
        table.add(String.valueOf(bot.getCell().getSunLight()));

        table.row();
        table.add("Cell organics");
        table.add(String.valueOf(bot.getCell().getOrganics()));

        table.row();
        table.add("Cell minerals");
        table.add(String.valueOf(bot.getCell().getMinerals()));
        propertiesPane.setActor(table);
    }

    private void fillParameters(LivingCell bot) {
        Table table = new Table(getSkin());
        table.columnDefaults(0).align(Align.right);
        table.columnDefaults(1).align(Align.left).padLeft(8f);

        for (LivingCellParameters.ParameterName parameterName : LivingCellParameters.ParameterName.values()) {
            table.add(parameterName.name());
            table.add(String.valueOf(bot.getParameter(parameterName)));
            table.row();
        }
        parametersPane.setActor(table);
    }

    private void fillGenome(LivingCell bot) {
        Table table = new Table(getSkin());
        table.columnDefaults(0).align(Align.right);
        table.columnDefaults(1).align(Align.left).padLeft(8f);


        genomePane.setActor(table);
    }
}
