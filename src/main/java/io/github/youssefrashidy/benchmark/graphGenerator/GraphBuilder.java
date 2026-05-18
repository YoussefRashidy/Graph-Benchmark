package io.github.youssefrashidy.benchmark.graphGenerator;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.graph.Graph;
import io.github.youssefrashidy.graph.GraphType;
import io.github.youssefrashidy.graph.Vertex;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Arrays;
import java.util.Random;

@Component
public class GraphBuilder {
    Random rng = new Random(1234);
    int baseWeight = 1;


    Graph<Void, Void> buildUndirectedGraphObject(int[] vertices, IntObjectHashMap<FastList<Integer>> edges, int maxWeight) {
        Graph<Void, Void> graph = new Graph<>(GraphType.UNDIRECTED);
        IntObjectHashMap<Vertex<Void>> indexToVertex = IntObjectHashMap.newMap();
        Arrays.stream(vertices).forEach(vertex -> {
            var v = graph.addVertex(null);
            indexToVertex.put(vertex, v);
        });
        edges.forEachKey(key -> {
            edges.get(key).forEach(key2 -> {
                graph.addEdge(indexToVertex.get(key), indexToVertex.get(key2), null, rng.nextInt(baseWeight, maxWeight + 1));
            });
        });
        return graph;
    }

    Graph<Void, Void> buildDirectedGraphObject(int[] vertices, IntObjectHashMap<FastList<Integer>> edges, int maxWeight) {
        Graph<Void, Void> graph = new Graph<>(GraphType.DIRECTED);
        IntObjectHashMap<Vertex<Void>> indexToVertex = IntObjectHashMap.newMap();
        Arrays.stream(vertices).forEach(vertex -> {
            var v = graph.addVertex(null);
            indexToVertex.put(vertex, v);
        });
        edges.forEachKey(key -> {
            edges.get(key).forEach(key2 -> {
                graph.addDirectedEdge(indexToVertex.get(key), indexToVertex.get(key2), null, rng.nextInt(baseWeight, maxWeight + 1));
            });
        });
        return graph;
    }
}
