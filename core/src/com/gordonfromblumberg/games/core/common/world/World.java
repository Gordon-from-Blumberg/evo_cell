package com.gordonfromblumberg.games.core.common.world;

import com.badlogic.gdx.utils.Disposable;
import com.gordonfromblumberg.games.core.common.event.Event;
import com.gordonfromblumberg.games.core.common.event.EventHandler;
import com.gordonfromblumberg.games.core.common.event.EventProcessor;

public class World implements Disposable {
    protected final EventProcessor eventProcessor = EventProcessor.INSTANCE;

    protected float mouseX, mouseY; // current world coordinates of mouse
    protected boolean paused;

    public void initialize() {}

    /**
     * @param delta Time after last frame in seconds
     * @param mouseX World mouse x coordinate
     * @param mouseY World mouse y coordinate
     */
    public void update(float delta, float mouseX, float mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void registerHandler(String type, EventHandler handler) {
        eventProcessor.registerHandler(type, handler);
    }

    public void pushEvent(Event event) {
        eventProcessor.push(event);
    }

    public float getMouseX() {
        return mouseX;
    }

    public float getMouseY() {
        return mouseY;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        this.paused = !this.paused;
    }

    @Override
    public void dispose() {

    }
}
