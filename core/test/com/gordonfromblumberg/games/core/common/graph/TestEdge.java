package com.gordonfromblumberg.games.core.common.graph;

public class TestEdge implements Edge<TestNode> {
    private TestNode node;
    private float weight;

    public TestEdge(TestNode node, float weight) {
        this.node = node;
        this.weight = weight;
    }

    @Override
    public TestNode getNode() {
        return node;
    }

    @Override
    public float getWeight() {
        return weight;
    }
}
