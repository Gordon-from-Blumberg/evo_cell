package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.assets.AssetManager;

public class Assets {
    private static AssetManager assetManager;

    public static void setAssetManager(AssetManager assetManager) {
        Assets.assetManager = assetManager;
    }

    public static AssetManager manager() {
        return assetManager;
    }

    public static <T> T get(String name, Class<T> type) {
        return assetManager.get(name, type);
    }
}
