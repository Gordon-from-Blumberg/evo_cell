package com.gordonfromblumberg.games.core.evocell.world;

import com.badlogic.gdx.Preferences;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;

public class WorldParams {
    int width;
    int height;
    int minLight;
    int maxLight;
    int minTemperature;
    int maxTemperature;

    public void load(ConfigManager config) {
        width = config.getIntegerPref("world.width");
        height = config.getIntegerPref("world.height");
        minLight = config.getIntegerPref("world.minLight");
        maxLight = config.getIntegerPref("world.maxLight");
        minTemperature = config.getIntegerPref("world.minTemperature");
        maxTemperature = config.getIntegerPref("world.maxTemperature");
    }

    public void save(Preferences prefs) {
        prefs.putInteger("world.width", width);
        prefs.putInteger("world.height", height);
        prefs.putInteger("world.minLight", minLight);
        prefs.putInteger("world.maxLight", maxLight);
        prefs.putInteger("world.minTemperature", minTemperature);
        prefs.putInteger("world.maxTemperature", maxTemperature);
    }

    public void load(Preferences prefs) {
        width = prefs.getInteger("world.width");
        height = prefs.getInteger("world.height");
        minLight = prefs.getInteger("world.minLight");
        maxLight = prefs.getInteger("world.maxLight");
        minTemperature = prefs.getInteger("world.minTemperature");
        maxTemperature = prefs.getInteger("world.maxTemperature");
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

    public int getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        this.minTemperature = minTemperature;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
}
