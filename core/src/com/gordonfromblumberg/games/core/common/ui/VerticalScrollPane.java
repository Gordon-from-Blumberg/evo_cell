package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class VerticalScrollPane extends ScrollPane {
    private float maxHeight;

    public VerticalScrollPane(Actor actor, Skin skin, float maxHeight) {
        super(actor, skin);

        setScrollingDisabled(true, false);
        this.maxHeight = maxHeight;
    }

    public VerticalScrollPane(Actor actor, Skin skin) {
        this(actor, skin, 0);
    }

    @Override
    public float getPrefHeight() {
        return maxHeight > 0 ? Math.min(maxHeight, super.getPrefHeight()) : super.getPrefHeight();
    }

    @Override
    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        this.maxHeight = maxHeight;
    }
}
