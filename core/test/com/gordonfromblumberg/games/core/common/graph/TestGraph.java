package com.gordonfromblumberg.games.core.common.graph;

import com.badlogic.gdx.utils.Array;

public class TestGraph implements Graph<TestNode> {
    private final TestNode[] nodes;

    public TestGraph(int nodeCount) {
        this.nodes = new TestNode[nodeCount];
        for (int i = 0; i < nodeCount; ++i) {
            this.nodes[i] = new TestNode(i);
        }
    }

    @Override
    public void next(TestNode node, Array<Edge<TestNode>> out) {
        out.addAll(node.nextEdges);
    }

    @Override
    public void prev(TestNode node, Array<Edge<TestNode>> out) {
        out.addAll(node.prevEdges);
    }

    TestNode get(int i) {
        return nodes[i];
    }

    void addNextNode(int nodeIdx, int nextIdx, float weight) {
        nodes[nodeIdx].addNextNode(nodes[nextIdx], weight);
    }
}
