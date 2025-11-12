package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class ButtonListWindow extends GBDialog {
    public ButtonListWindow(String title, Skin skin) {
        super(title, skin);

        float padY = 5f;
        float padX = 20f;
        getButtonTable().defaults().pad(padY, padX, padY, padX).expandX().fillX();
        getButtonTable().padBottom(15f).padTop(10f);
    }

    @Override
    public Dialog button(Button button, Object object) {
        getButtonTable().row();
        return super.button(button, object);
    }
}
