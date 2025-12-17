package com.gordonfromblumberg.games.core.evocell.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.gordonfromblumberg.games.core.common.ui.IntChangeableLabel;
import com.gordonfromblumberg.games.core.evocell.world.WorldParams;

import java.util.function.IntConsumer;

public class ECUIUtils {
    public static final float FIELD_WIDTH = 50f;

    public static void addLightField(Table table, WorldParams worldParams) {
        addMinMaxParameter(table, "Light",
                worldParams::setMinLight, 0, 15, worldParams.getMinLight(),
                worldParams::setMaxLight, 10, 25, worldParams.getMaxLight(), 1);
    }

    public static void addTemperatureField(Table table, WorldParams worldParams) {
        addMinMaxParameter(table, "Temperature",
                worldParams::setMinTemperature, -30, 25, worldParams.getMinTemperature(),
                worldParams::setMaxTemperature, 20, 50, worldParams.getMaxTemperature(), 1);
    }

    private static void addMinMaxParameter(Table table, String name,
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
