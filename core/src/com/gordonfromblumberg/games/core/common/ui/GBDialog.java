package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.gordonfromblumberg.games.core.common.debug.DebugOptions;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

import java.util.function.Consumer;

public class GBDialog extends Dialog {
    private static final Logger log = LogManager.create(GBDialog.class);

    private final TextButton closeButton;
    private Runnable onOpen;
    private Runnable onClose;

    public GBDialog(String title, Skin skin) {
        super(title, skin);

        this.closeButton = new TextButton("x", skin, "close");
        this.closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });
        getTitleTable().add(closeButton).height(getTitleLabel().getHeight());

        if (DebugOptions.DEBUG_UI)
            getTitleTable().debugAll();
    }

    public void toStageCenter() {
        Stage stage = getStage();
        if (stage == null) return;

        float width = stage.getWidth();
        float height = stage.getHeight();
        setPosition(Math.round((width - getWidth()) / 2),
                Math.round((height - getHeight()) / 2));
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        if (onOpen != null) {
            onOpen.run();
        }
        return super.show(stage, action);
    }

    @Override
    public void hide(Action action) {
        if (onClose != null) {
            if (action instanceof SequenceAction seqAct) {
                seqAct.addAction(Actions.run(onClose));
            } else {
                action = Actions.sequence(action, Actions.run(onClose));
            }
        }
        super.hide(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void result(Object object) {
        if (object instanceof Consumer)
            ((Consumer<GBDialog>) object).accept(this);
        else if (object instanceof Runnable)
            ((Runnable) object).run();
    }

    public void setOnOpen(Runnable onOpen) {
        this.onOpen = onOpen;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
}
