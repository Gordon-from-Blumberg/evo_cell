package com.gordonfromblumberg.games.core.common.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.ObjectMap;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class WindowManager {
    private static final Logger log = LogManager.create(WindowManager.class);

    private final Stage stage;
    private float duration = 0.3f;
    private String defaultWindow;

    private final ObjectMap<String, Dialog> windows = new ObjectMap<>();
    private Dialog previous;
    private Dialog current;
    private boolean showPrevOnClose;

    public WindowManager(Stage stage) {
        this(stage, "default");
    }

    public WindowManager(Stage stage, String defaultWindow) {
        this.stage = stage;
        this.defaultWindow = defaultWindow;
    }

    public void register(String name, Dialog window) {
        log.debug("Register window of type " + window.getClass().getSimpleName() + " for name " + name);
        windows.put(name, window);
        if (window instanceof GBDialog gbDialog) {
            gbDialog.setOnClose(resetCurrent(gbDialog));
        }
    }

    public void open(String name, boolean showPrevOnClose) {
        Dialog window = windows.get(name);
        if (window == null || window == current) return;

        log.debug("Open window " + name + " with showPrevOnClose=" + showPrevOnClose);
        this.showPrevOnClose = showPrevOnClose;
        previous = current;
        if (current != null) {
            closeCurrent(window);
        } else {
            open(window);
        }
    }

    public void toggle(String name) {
        Dialog window = windows.get(name);
        if (window == null) return;

        log.debug("Toggle window " + name);
        if (window == current) {
            close();
        } else {
            open(name, false);
        }
    }

    /**
     * Closes current window and opens previous if {@link #showPrevOnClose} == true
     */
    public void close() {
        if (current == null) return;

        log.debug("Close current window of type " + current.getClass().getSimpleName() + " with name " + current.getName());
        closeCurrent(showPrevOnClose ? previous : null);
    }

    /**
     * Invoke {@link #close()} if current is not null, open default window otherwise
     */
    public void escape() {
        log.debug("Escape");
        if (current != null) {
            close();
        } else {
            open(defaultWindow, false);
        }
    }

    public boolean isOpened() {
        return current != null;
    }

    public void setDefaultWindow(String defaultWindow) {
        this.defaultWindow = defaultWindow;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    private void open(Dialog window) {
        current = window;
        if (window != null) {
            window.getColor().a = 0;
            window.show(stage, sequence(run(() -> toStageCenter(window)), fadeIn()));
        }
    }

    private Runnable resetCurrent(GBDialog dialog) {
        return () -> {
            if (current == dialog) {
                current = null;
            }
        };
    }

    private void closeCurrent(Dialog open) {
        log.debug("Close current " + current.getName());
        current.hide(sequence(fadeOut(),
                run(() -> open(open))));
    }

    private void toStageCenter(Dialog window) {
        window.setPosition(Math.round((stage.getWidth() - window.getWidth()) / 2),
                Math.round((stage.getHeight() - window.getHeight()) / 2));
    }

    private Action fadeOut() {
        return Actions.fadeOut(duration, Interpolation.fade);
    }

    private Action fadeIn() {
        return Actions.fadeIn(duration, Interpolation.fade);
    }
}
