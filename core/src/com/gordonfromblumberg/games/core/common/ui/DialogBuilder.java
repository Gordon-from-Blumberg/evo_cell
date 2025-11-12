package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import java.util.function.Consumer;

public class DialogBuilder {
    private final GBDialog dialog;

    public DialogBuilder(String title, Skin skin) {
        this.dialog = new GBDialog(title, skin);
    }

    public DialogBuilder text(String text) {
        dialog.text(text);
        return this;
    }

    public DialogBuilder fillContent(Consumer<Table> fillContent) {
        fillContent.accept(dialog.getContentTable());
        return this;
    }

    // todo use config object instead of lambda
    public DialogBuilder textField(String initialValue, Consumer<String> onAccept, String message, String fieldName,
                                   Consumer<TextField> configure) {
        if (dialog.getContentTable().hasChildren())
            dialog.getContentTable().row();

        TextField textField = new TextField(initialValue, dialog.getSkin());
        if (message != null) textField.setMessageText(message);
        if (fieldName != null) textField.setName(fieldName);
        dialog.getContentTable().add(textField);
        if (configure != null) configure.accept(textField);
        textField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                TextField field = (TextField) event.getListenerActor();
                if (!field.isDisabled() && keycode == Input.Keys.ENTER) {
                    onAccept.accept(field.getText());
                    dialog.hide();
                    return true;
                }
                return false;
            }
        });

        return this;
    }

    public DialogBuilder button(String title, Object handler) {
        dialog.button(title, handler);
        return this;
    }

    public DialogBuilder ok(Object handler) {
        return button("Ok", handler);
    }

    public DialogBuilder cancel(Object handler) {
        return button("Cancel", handler);
    }

    public GBDialog build() {
        return dialog;
    }
}
