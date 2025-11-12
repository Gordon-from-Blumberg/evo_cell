package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.utils.ObjectMap;

import java.util.Comparator;

public class ValueMapComparator<T> implements Comparator<T> {
    private ObjectMap<T, Float> valueMap;

    public ValueMapComparator() {
        this(new ObjectMap<>());
    }

    public ValueMapComparator(ObjectMap<T, Float> valueMap) {
        this.valueMap = valueMap;
    }

    @Override
    public int compare(T n1, T n2) {
        float d = valueMap.get(n1) - valueMap.get(n2);
        if (d > 0) return 1;
        else if (d < 0) return -1;
        else return 0;
    }

    public void setValueMap(ObjectMap<T, Float> valueMap) {
        this.valueMap = valueMap;
    }
}
