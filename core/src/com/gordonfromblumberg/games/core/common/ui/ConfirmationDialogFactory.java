package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ConfirmationDialogFactory {
    private final Skin skin;
    private String title;
    private String text;

    public ConfirmationDialogFactory(Skin skin) {
        this.skin = skin;
    }

    public ConfirmationDialogFactory title(String title) {
        this.title = title;
        return this;
    }

    public ConfirmationDialogFactory text(String text) {
        this.text = text;
        return this;
    }

    public GBDialog create(Runnable okHandler) {
        return new DialogBuilder(title, skin)
                .text(text)
                .ok(okHandler)
                .cancel(null)
                .build();
    }
}
