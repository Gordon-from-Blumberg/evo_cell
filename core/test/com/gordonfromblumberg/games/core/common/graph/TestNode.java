package com.gordonfromblumberg.games.core.common.graph;


import com.badlogic.gdx.utils.Array;

public class TestNode implements Node {
    final int id;
    final Array<TestEdge> nextEdges = new Array<>();
    final Array<TestEdge> prevEdges = new Array<>();

    TestNode(int id) {
        this.id = id;
    }

    public void addNextNode(TestNode node, float weight) {
        nextEdges.add(new TestEdge(node, weight));
        node.prevEdges.add(new TestEdge(this, weight));
    }

    @Override
    public String toString() {
        return "TestNode#" + id;
    }
}
