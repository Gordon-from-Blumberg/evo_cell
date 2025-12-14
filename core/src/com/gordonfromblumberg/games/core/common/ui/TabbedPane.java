package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;

public class TabbedPane<B extends Button> extends WidgetGroup {
    private final ButtonGroup<B> buttonGroup = new ButtonGroup<>();
    private final HorizontalGroup buttons = new HorizontalGroup().center();
    private final Array<Actor> panes = new Array<>();

    private Actor currentPane;
    private float pads = 5f;
    private float panePad = 3f;

    public TabbedPane() {
        setTransform(false);
        buttons.setTransform(false);
        buttons.space(5f);
        addActor(buttons);
    }

    public void addPane(B button, Actor pane) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (actor instanceof Button btn && btn.isChecked()) {
                    if (currentPane != null) removeActor(currentPane);
                    currentPane = pane;
                    addActor(pane);
                    invalidate();
                }
            }
        });
        panes.add(pane);
        buttonGroup.add(button);
        buttons.addActor(button);
        pane.setY(panePad + getButtonsHeight());
    }

    public void setPanePad(float panePad) {
        this.panePad = panePad;
    }

    @Override
    public void layout() {
        float width = getWidth();
        float height = getHeight();
        float buttonsHeight = buttons.getPrefHeight();
        buttons.setBounds(pads, height - buttonsHeight - pads, width - 2 * pads, buttonsHeight);
        buttons.validate();
        if (currentPane != null) {
            currentPane.setBounds(pads, pads,
                                  width - 2 * pads, height - buttonsHeight - panePad - 2 * panePad);
            if (currentPane instanceof Layout paneL)
                paneL.validate();
        }
    }

    @Override
    public float getPrefWidth() {
        float prefWidth = buttons.getPrefWidth();
        for (Actor pane : panes) {
            prefWidth = Math.max(prefWidth, pane instanceof Layout l ? l.getPrefWidth() : pane.getWidth());
        }
        return prefWidth + 2 * pads;
    }

    @Override
    public float getPrefHeight() {
        float maxPanePrefHeight = 0;
        for (Actor pane : panes) {
            maxPanePrefHeight = Math.max(maxPanePrefHeight, pane instanceof Layout l ? l.getPrefHeight() : pane.getHeight());
        }
        return buttons.getPrefHeight() + panePad + maxPanePrefHeight + 2 * pads;
    }

    private float getButtonsHeight() {
        float height = 0;
        for (Actor child : buttons.getChildren()) {
            height = Math.max(height, child.getHeight());
        }
        return height;
    }
}
