package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.function.Consumer;

public class UpdatableLabel extends Label {
    private final Consumer<StringBuilder> textUpdater;

    public UpdatableLabel(Skin skin, Consumer<StringBuilder> textUpdater) {
        super(null, skin);

        this.textUpdater = textUpdater;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        textUpdater.accept(getText());
        invalidateHierarchy();
    }

    public UpdatableLabel center() {
        setAlignment(Align.center);
        return this;
    }
}
