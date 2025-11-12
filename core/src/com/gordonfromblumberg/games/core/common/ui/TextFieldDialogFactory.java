package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import java.util.function.Consumer;

public class TextFieldDialogFactory {
    private final Skin skin;
    private String title;
    private String text;

    public TextFieldDialogFactory(Skin skin) {
        this.skin = skin;
    }

    public TextFieldDialogFactory title(String title) {
        this.title = title;
        return this;
    }

    public TextFieldDialogFactory text(String text) {
        this.text = text;
        return this;
    }

    public GBDialog create(String initialValue, Consumer<String> inputHandler) {
        final String textFieldName = this + inputHandler.toString();
        return new DialogBuilder(title, skin)
                .text(text)
                .textField(initialValue, inputHandler, text, textFieldName, tf -> tf.setWidth(100f))
                .ok((Consumer<GBDialog>) dialog -> {
                    TextField field = dialog.getContentTable().findActor(textFieldName);
                    inputHandler.accept(field.getText());
                })
                .cancel(null)
                .build();
    }
}
