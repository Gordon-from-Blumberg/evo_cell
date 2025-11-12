package com.gordonfromblumberg.games.core.common.control;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.IntMap;
import com.gordonfromblumberg.games.core.common.log.LogManager;
import com.gordonfromblumberg.games.core.common.log.Logger;

public class KeyBindingsListener extends InputListener {
    private static final Logger log = LogManager.create(KeyBindingsListener.class);
    private static final int SHIFT_BIT = 1;
    private static final int CTRL_BIT = 1 << 1;
    private static final int ALT_BIT = 1 << 2;

    private final IntMap<KeyHandler> downHandlers = new IntMap<>();
    private final IntMap<KeyHandler> upHandlers = new IntMap<>();

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        return handle(downHandlers, keycode);
    }

    @Override
    public boolean keyUp(InputEvent event, int keycode) {
        return handle(upHandlers, keycode);
    }

    /**
     * Binds handler for the specified keycode for key up event
     * @param keycode Keycode from {@link Input.Keys}
     * @param name Binding name
     * @param handler Handler
     */
    public void bind(int keycode, String name, Runnable handler) {
        log.debug("Bind " + Input.Keys.toString(keycode) + " up to " + name);
        int key = keycode << 3;
        KeyHandler keyHandler = upHandlers.get(key);
        if (keyHandler == null) {
            keyHandler = new KeyHandler();
            upHandlers.put(key, keyHandler);
        }
        keyHandler.set(name, handler);
    }

    private boolean handle(IntMap<KeyHandler> handlerMap, int keycode) {
        int key = key(keycode);
        KeyHandler handler = handlerMap.get(key);
        if (handler == null) return false;

        handler.handler.run();
        return true;
    }

    private static int key(int keycode) {
        int key = keycode << 3;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
            key |= SHIFT_BIT;
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))
            key |= CTRL_BIT;
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT))
            key |= ALT_BIT;
        return key;
    }

    private static class KeyHandler {
        private String name;
        private Runnable handler;

        void set(String name, Runnable handler) {
            this.name = name;
            this.handler = handler;
        }
    }
}
