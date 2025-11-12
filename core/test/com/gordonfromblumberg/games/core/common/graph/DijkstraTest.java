package com.gordonfromblumberg.games.core.common.graph;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DijkstraTest {
    private final TestGraph graph = new TestGraph(6);
    private final Dijkstra<TestNode> dijkstra = new Dijkstra<>();

    @BeforeEach
    void init() {
        graph.addNextNode(0, 1, 5f);
        graph.addNextNode(0, 2, 2f);
        graph.addNextNode(0, 3, 4f);

        graph.addNextNode(1, 4, 10f);

        graph.addNextNode(2, 1, 2f);
        graph.addNextNode(2, 5, 3f);

        graph.addNextNode(3, 0, 5f);
        graph.addNextNode(3, 5, 5f);

        graph.addNextNode(4, 3, 7f);

        graph.addNextNode(5, 4, 3f);
    }

    @Test
    void testInit() {
        for (int i = 0; i < 6; ++i) {
            Assertions.assertNotNull(graph.get(i));
        }
    }

    @Test
    void testFindPath() {
        Array<TestNode> path = new Array<>();
        dijkstra.findPath(graph, graph.get(0), graph.get(3), path);
        Assertions.assertEquals(2, path.size);
        Assertions.assertSame(graph.get(0), path.get(0));
        Assertions.assertSame(graph.get(3), path.get(1));

        path.clear();
        dijkstra.findPath(graph, graph.get(0), graph.get(4), path);
        for (TestNode node : path) {
            System.out.println("path step: " + node);
        }
        Assertions.assertEquals(4, path.size);
        Assertions.assertSame(graph.get(0), path.get(0));
        Assertions.assertSame(graph.get(2), path.get(1));
        Assertions.assertSame(graph.get(5), path.get(2));
        Assertions.assertSame(graph.get(4), path.get(3));
    }

    @Test
    void testMark() {
        ObjectMap<TestNode, Float> marks = new ObjectMap<>();
        dijkstra.markNodes(graph, graph.get(0), marks);

        float[] expected = new float[] {0f, 4f, 2f, 4f, 8f, 5f};
        for (int i = 0; i < 6; ++i) {
            Assertions.assertEquals(expected[i], marks.get(graph.get(i)).floatValue());
        }
    }
}
