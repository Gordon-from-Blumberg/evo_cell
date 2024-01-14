package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.Preferences;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public class WorldParams {
    int width;
    int height;

    public void load(ConfigManager config) {
        width = config.getInteger("world.width");
        height = config.getInteger("world.height");
    }

    public void save(Preferences prefs) {
        prefs.putInteger("world.width", width);
        prefs.putInteger("world.height", height);
    }

    public void load(Preferences prefs) {
        width = prefs.getInteger("world.width");
        height = prefs.getInteger("world.height");
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
