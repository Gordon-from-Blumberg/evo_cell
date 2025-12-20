package com.gordonfromblumberg.games.core.evocell.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.gordonfromblumberg.games.core.common.ui.TabbedPane;
import com.gordonfromblumberg.games.core.common.ui.VerticalScrollPane;
import com.gordonfromblumberg.games.core.evocell.model.*;

public class BotInfoWindow extends Window {
    private final Label botLabel;
    private final TabbedPane<TextButton> infoPane;
    private final Container<Table> propertiesPane = new Container<>();
    private final VerticalScrollPane parametersPane;
    private final VerticalScrollPane genomePane;
    private final Interpreter interpreter = new Interpreter();

    public BotInfoWindow(String title, Skin skin) {
        super(title, skin);

        propertiesPane.align(Align.center);

        float maxPaneHeight = 500f;
        parametersPane = new VerticalScrollPane(null, skin, maxPaneHeight);
        parametersPane.setScrollingDisabled(true, false);
        genomePane = new VerticalScrollPane(null, skin, maxPaneHeight);
        genomePane.setScrollingDisabled(true, false);

        botLabel = new Label("", skin);
        add(botLabel).align(Align.center);

        infoPane = new TabbedPane<>();
        row();
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

    public void show(Bot bot, Stage stage) {
        if (bot == null) {
            if (getStage() != null) {
                close();
            }
            return;
        }

        StringBuilder botLabelSb = botLabel.getText();
        botLabelSb.clear();
        botLabelSb.append(bot instanceof SimpleBot ? "Simple" : "Evo")
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
//        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));

    }

    public void close() {
        remove();
    }

    private void setPanes(Skin skin) {
        infoPane.addPane(new TextButton("Properties", skin), propertiesPane);
        infoPane.addPane(new TextButton("Parameters", skin), parametersPane);
        infoPane.addPane(new TextButton("Genome", skin), genomePane);
    }

    private void fillProperties(Bot bot) {
        Table table = new Table(getSkin());
        table.columnDefaults(0).align(Align.right);
        table.columnDefaults(1).align(Align.left).padLeft(8f);

        table.add("HP");
        table.add(bot.getHp() + "/" + Bot.maxHp);

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

        table.row();
        table.add("Cell temperature");
        table.add(String.valueOf(bot.getCell().getTemperature()));
        propertiesPane.setActor(table);
    }

    private void fillParameters(Bot bot) {
        Table table = new Table(getSkin());
        table.columnDefaults(0).align(Align.right);
        table.columnDefaults(1).align(Align.left).padLeft(8f);

        for (BotParameters.ParameterName parameterName : BotParameters.ParameterName.values()) {
            table.add(parameterName.name());
            table.add(String.valueOf(bot.getParameter(parameterName)));
            table.row();
        }
        parametersPane.setActor(table);
    }

    private void fillGenome(Bot bot) {
        Table table = new Table(getSkin());
        table.pad(10f);
        table.columnDefaults(0).align(Align.right);
        table.columnDefaults(1).align(Align.right).padLeft(5f);
        table.columnDefaults(2).align(Align.left).padLeft(7f);

        if (bot instanceof EvoBot evoBot) {
            interpreter.print(evoBot, new TableGenomePrinter(table));
        } else {
            table.add("No genome").colspan(3).align(Align.center);
        }

        genomePane.setActor(table);
    }

    private static class TableGenomePrinter implements Interpreter.GenomePrinter {

        private final Table table;
        private Label label;

        private TableGenomePrinter(Table table) {
            this.table = table;
        }

        @Override
        public StringBuilder startRow(String geneValueIndex, String geneValue) {
            table.row();
            table.add(geneValueIndex);
            table.add(geneValue);
            label = new Label("", table.getSkin());
            table.add(label);
            return label.getText();
        }

        @Override
        public void endRow() {
            label.invalidateHierarchy();
        }
    }
}
