package io.github.youssefrashidy.graph;

import io.github.youssefrashidy.benchmark.graphGenerator.GraphBuilder;
import io.github.youssefrashidy.benchmark.graphGenerator.GraphGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphGeneratorTest {
    GraphBuilder builder;
    GraphGenerator generator;

    @BeforeEach
    void initialize() {
        builder = new GraphBuilder();
        generator = new GraphGenerator(builder);
    }

    @Test
    void generateCompleteGraph() {
        int v = 100;
        var completeGraph = generator.generateCompleteGraph(v, 1000);
        long expectedEdges = ((long) v * (v - 1)) / 2;
        assertEquals(v, completeGraph.verticesMap.keysView().size());
        assertEquals(expectedEdges, completeGraph.getEdgeCount());
        completeGraph.verticesMap.keysView().forEach(vertex -> {
            completeGraph.verticesMap.keysView().forEach(destination -> {
                if (destination == vertex)
                    assertFalse(completeGraph.adjacencyList.get(vertex).anySatisfy(edge -> edge.v == destination));
                else
                    assertTrue(completeGraph.adjacencyList.get(vertex).anySatisfy(edge -> edge.v == destination));
            });
        });
    }

    @Test
    void generateDenseGraph() {
        int v = 100;
        int maxWeight = 1000;
        var denseGraph = generator.generateDenseGraph(v, 1000);
        long expectedEdges = (long) (v * (v - 1) / 8);

        assertEquals(v, denseGraph.verticesMap.size());
        assertEquals(expectedEdges, denseGraph.getEdgeCount());

        denseGraph.edgeList.forEach(edge -> {
            assertTrue(edge.getWeight() >= 1, "Weight below lower bound");
            assertTrue(edge.weight <= maxWeight, "Weight above upper bound");
        });

        assertEquals(
                v - 1,
                denseGraph.primMST().size(),
                "Dense graph should be connected"
        );

        assertDoesNotThrow(denseGraph::kruskalMST);
        assertDoesNotThrow(() -> denseGraph.dijkstra(denseGraph.getVertex(0)));
    }

    @Test
    void generateSparseGraph() {
        int v = 100;
        int maxWeight = 1000;
        var sparseGraph = generator.generateSparseGraph(v, 1000);
        long expectedEdges = (long) 5 * v;

        assertEquals(v, sparseGraph.verticesMap.size());
        assertEquals(expectedEdges, sparseGraph.getEdgeCount());

        sparseGraph.edgeList.forEach(edge -> {
            assertTrue(edge.getWeight() >= 1, "Weight below lower bound");
            assertTrue(edge.weight <= maxWeight, "Weight above upper bound");
        });

        assertEquals(
                v - 1,
                sparseGraph.primMST().size(),
                "Sparse graph should be connected"
        );

        assertDoesNotThrow(sparseGraph::kruskalMST);
        assertDoesNotThrow(() -> sparseGraph.dijkstra(sparseGraph.getVertex(0)));
    }

    @Test
    void generateDAG() {
        int v = 100;
        int maxWeight = 1000;

        var dag = generator.generateDAG(v, maxWeight);

        // 1. vertex count
        assertEquals(v, dag.verticesMap.size());

        // 2. edge count (exact because construction is deterministic)
        long expectedEdges = 5L * v;

        assertEquals(expectedEdges, dag.getEdgeCount());

        // 3. weight bounds
        dag.edgeList.forEach(edge -> {
            assertTrue(edge.getWeight() >= 1, "Weight below lower bound");
            assertTrue(edge.getWeight() <= maxWeight, "Weight above upper bound");
        });

        // 4. DAG property (cycle detection via topo sort)
        assertDoesNotThrow(dag::topologicalSort);

        // 5. shortest path must run safely
        assertDoesNotThrow(() -> dag.dagShortestPath(dag.getVertex(0)));

        var vertices = dag.verticesMap.keysView().toList();

        for (int i = 0; i < v - 1; i++) {
            int u = vertices.get(i);
            int next = vertices.get(i + 1);

            boolean exists = dag.adjacencyList
                    .get(u)
                    .stream()
                    .anyMatch(e -> e.v == next);

            assertTrue(exists, "Missing backbone edge " + u + " -> " + next);
        }
    }

}