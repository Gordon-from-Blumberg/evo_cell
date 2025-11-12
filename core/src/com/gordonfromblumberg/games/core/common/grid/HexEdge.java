package com.gordonfromblumberg.games.core.common.grid;

import com.gordonfromblumberg.games.core.common.graph.Edge;

class HexEdge implements Edge<Hex> {
    final Hex hex;
    float weight;

    HexEdge(Hex hex) {
        this(hex, 1f);
    }

    HexEdge(Hex hex, float weight) {
        this.hex = hex;
        this.weight = weight;
    }

    @Override
    public Hex getNode() {
        return hex;
    }

    @Override
    public float getWeight() {
        return weight;
    }
}
