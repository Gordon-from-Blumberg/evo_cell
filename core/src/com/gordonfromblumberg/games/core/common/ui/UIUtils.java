package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.debug.DebugOptions;

public class UIUtils {
    private UIUtils() {}

    public static TextButton textButton(String text, Skin skin, Runnable onClickListener, ButtonConfig config) {
        final TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!button.isDisabled())
                    onClickListener.run();
            }
        });
        
        if (config != null) {
            if (config.disableEvents != null) {
                for (String disableEvent : config.disableEvents) {
                    config.gameWorld.registerHandler(disableEvent, e -> {
                        button.setDisabled(true);
                        return false;
                    });
                }
            }

            if (config.enableEvents != null) {
                for (String enableEvent : config.enableEvents) {
                    config.gameWorld.registerHandler(enableEvent, e -> {
                        button.setDisabled(false);
                        return false;
                    });
                }
            }
            
            if (config.showEvents != null) {
                for (String showEvent : config.showEvents) {
                    config.gameWorld.registerHandler(showEvent, e -> {
                        button.setVisible(true);
                        return false;
                    });
                }
            }

            if (config.hideEvents != null) {
                for (String hideEvent : config.hideEvents) {
                    config.gameWorld.registerHandler(hideEvent, e -> {
                        button.setVisible(false);
                        return false;
                    });
                }
            }
        }

        return button;
    }

    public static Table createTable() {
        return createTable(null);
    }

    public static Table createTable(Skin skin) {
        Table table = new Table(skin);

        if (DebugOptions.DEBUG_UI) {
            table.debugAll();
        }

        return table;
    }
}
