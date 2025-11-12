package com.gordonfromblumberg.games.core.common.graph;

import com.badlogic.gdx.utils.Array;

public interface Graph<T extends Node> {
    void next(T node, Array<Edge<T>> out);
    void prev(T node, Array<Edge<T>> out);
}
