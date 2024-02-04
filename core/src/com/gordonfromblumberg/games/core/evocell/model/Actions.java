package com.gordonfromblumberg.games.core.evocell.model;

import com.badlogic.gdx.utils.ObjectMap;

public final class Actions {
    private static final ObjectMap<String, ActionMapping> actionsMap = new ObjectMap<>();

    static {
        actionsMap.put("move", (w, lc) -> lc.move(w.getGrid()));
        actionsMap.put("rotateLeft", (w, lc) -> lc.rotateLeft());
        actionsMap.put("rotateRight", (w, lc) -> lc.rotateRight());
    }

    private Actions() { }
}
