package com.gordonfromblumberg.games.core.common.graph;

public interface Edge<T extends Node> {
    T getNode();

    default float getWeight() {
        return 1f;
    }
}
