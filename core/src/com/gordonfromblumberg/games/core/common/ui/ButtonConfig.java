package com.gordonfromblumberg.games.core.common.ui;

import com.gordonfromblumberg.games.core.common.event.EventHandler;

import java.util.function.BiConsumer;

public class ButtonConfig {
    final BiConsumer<String, EventHandler> eventHandlerRegistrar;
    final String[] disableEvents;
    final String[] enableEvents;
    final String[] showEvents;
    final String[] hideEvents;

    public ButtonConfig(BiConsumer<String, EventHandler> eventHandlerRegistrar, String[] disableEvents, String[] enableEvents,
                        String[] showEvents, String[] hideEvents) {
        this.eventHandlerRegistrar = eventHandlerRegistrar;
        this.disableEvents = disableEvents;
        this.enableEvents = enableEvents;
        this.showEvents = showEvents;
        this.hideEvents = hideEvents;
    }

    public static ButtonConfig toggleDisable(BiConsumer<String, EventHandler> eventHandlerRegistrar,
                                             String[] disableEvents, String[] enableEvents) {
        return new ButtonConfig(eventHandlerRegistrar, disableEvents, enableEvents, null, null);
    }

    public static ButtonConfig toggleDisable(BiConsumer<String, EventHandler> eventHandlerRegistrar,
                                             String disableEvent, String enableEvent) {
        return new ButtonConfig(eventHandlerRegistrar, new String[] { disableEvent }, new String[] { enableEvent },
                null, null);
    }
}
