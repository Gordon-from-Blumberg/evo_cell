package com.gordonfromblumberg.games.core.common.utils;


import com.badlogic.gdx.utils.Array;

public final class CollectionUtils {
    private CollectionUtils() { }

    public static <T> void addNonNull(Array<T> out, T[] array) {
        for (T item : array) {
            if (item != null)
                out.add(item);
        }
    }
}
