package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.gordonfromblumberg.games.core.common.utils.FloatConsumer;

public class FloatChangeableLabel extends HorizontalGroup implements Disableable {
    private final Button prev;
    private final Button next;
    private final FloatField valueField;

    private float step = 1;
    private ChangeFunction changeFunction = ChangeFunction.linear;

    public FloatChangeableLabel(Skin skin, FloatConsumer onChangeListener) {
        this.prev = new TextButton("<", skin);
        this.next = new TextButton(">", skin);
        this.valueField = FloatField.builder()
                .skin(skin)
                .value(0)
                .handler(onChangeListener)
                .build();
//        this.valueField.setDisabled(true);

        addClickListeners();
        addActor(prev);
        addActor(valueField);
        addActor(next);
    }

    public void setMinValue(float minValue) {
        valueField.minValue = minValue;
    }

    public void setMaxValue(float maxValue) {
        valueField.maxValue = maxValue;
    }

    public void setValue(float value) {
        valueField.setValue(value);
    }

    public void setStep(float step) {
        this.step = step;
    }

    public void linear() {
        changeFunction = ChangeFunction.linear;
    }

    public void geometric() {
        changeFunction = ChangeFunction.geometric;
    }

    public void setFieldWidth(float width) {
        valueField.setWidth(width);
    }

    public void setFieldDisabled(boolean disabled) {
        valueField.setDisabled(disabled);
    }

    @Override
    public void setDisabled(boolean isDisabled) {
        prev.setDisabled(isDisabled);
        valueField.setDisabled(isDisabled);
        next.setDisabled(isDisabled);
    }

    @Override
    public boolean isDisabled() {
        return valueField.isDisabled();
    }

    private void addClickListeners() {
        ClickListener clickListener = new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (valueField.isDisabled())
                    return;

                float v = valueField.getValue();
                switch (changeFunction) {
                    case linear:
                        if (event.getListenerActor() == prev) {
                            v -= step;
                        } else if (event.getListenerActor() == next) {
                            v += step;
                        }
                        break;
                    case geometric:
                        if (event.getListenerActor() == prev) {
                            v /= step;
                        } else if (event.getListenerActor() == next) {
                            v *= step;
                        }
                        break;
                }

                try {
                    valueField.setValue(v);
                } catch (IllegalArgumentException e) {
                    //do nothing
                }
            }
        };
        prev.addListener(clickListener);
        next.addListener(clickListener);
    }
}
