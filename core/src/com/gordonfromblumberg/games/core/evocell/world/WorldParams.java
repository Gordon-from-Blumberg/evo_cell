package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.Preferences;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public class WorldParams {
    int width;
    int height;
    int minLight;
    int maxLight;

    public void load(ConfigManager config) {
        width = config.getInteger("world.width");
        height = config.getInteger("world.height");
        minLight = config.getInteger("world.minLight");
        maxLight = config.getInteger("world.maxLight");
    }

    public void save(Preferences prefs) {
        prefs.putInteger("world.width", width);
        prefs.putInteger("world.height", height);
        prefs.putInteger("world.minLight", minLight);
        prefs.putInteger("world.maxLight", maxLight);
    }

    public void load(Preferences prefs) {
        width = prefs.getInteger("world.width");
        height = prefs.getInteger("world.height");
        minLight = prefs.getInteger("world.minLight");
        maxLight = prefs.getInteger("world.maxLight");
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

    public int getMinLight() {
        return minLight;
    }

    public void setMinLight(int minLight) {
        this.minLight = minLight;
    }

    public int getMaxLight() {
        return maxLight;
    }

    public void setMaxLight(int maxLight) {
        this.maxLight = maxLight;
    }
}
