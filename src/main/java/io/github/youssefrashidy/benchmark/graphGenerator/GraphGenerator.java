package io.github.youssefrashidy.benchmark.graphGenerator;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.annotations.Inject;
import io.github.youssefrashidy.graph.Graph;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.Random;
import java.util.stream.IntStream;

@Component
public class GraphGenerator {
    Random rng = new Random(1234);
    GraphBuilder graphBuilder;

    @Inject
    public GraphGenerator(GraphBuilder graphBuilder) {
        this.graphBuilder = graphBuilder;
    }

    public Graph<Void, Void> generateSparseGraph(int v, int maxWeight) {
        // create an array of size v shuffle it
        int[] vertices = IntStream.range(0, v).toArray();
        IntObjectHashMap<FastList<Integer>> edges = new IntObjectHashMap<>();
        shuffle(vertices);
        for (int i = 1; i < v; i++) {
            int j = rng.nextInt(0, i);
            edges.getIfAbsent(vertices[i], FastList::newList).add(vertices[j]);
        }
        long extraEdges = 5L * v - (v - 1);
        long edgeCount = 0;
        while (edgeCount < extraEdges) {
            int i = rng.nextInt(v);
            int j = rng.nextInt(v);
            edges.getIfAbsent(vertices[i], FastList::newList).add(vertices[j]);
            edgeCount++;
        }
        // build graph object
        return graphBuilder.buildUndirectedGraphObject(vertices, edges, maxWeight);
    }

    public Graph<Void, Void> generateDenseGraph(int v, int maxWeight) {
        // create an array of size v shuffle it
        int[] vertices = IntStream.range(0, v).toArray();
        IntObjectHashMap<FastList<Integer>> edges = new IntObjectHashMap<>();
        shuffle(vertices);
        for (int i = 1; i < v; i++) {
            int j = rng.nextInt(0, i - 1);
            edges.getIfAbsent(vertices[i], FastList::newList).add(vertices[j]);
        }
        long extraEdges = (long) (0.25 * ((long) v * (v - 1) / 2)) - (v - 1);
        long edgeCount = 0;
        while (edgeCount < extraEdges) {
            int i = rng.nextInt(v);
            int j = rng.nextInt(v);
            edges.getIfAbsent(vertices[i], FastList::newList).add(vertices[j]);
            edgeCount++;
        }
        // build graph object
        return graphBuilder.buildUndirectedGraphObject(vertices, edges, maxWeight);
    }

    public Graph<Void, Void> generateCompleteGraph(int v, int maxWeight) {
        int[] vertices = IntStream.range(0, v).toArray();
        IntObjectHashMap<FastList<Integer>> edges = new IntObjectHashMap<>();
        for (int i = 0; i < v; i++) {
            for (int j = i + 1; j < v; j++) {
                edges.getIfAbsent(i, FastList::newList).add(j);
            }
        }
        return graphBuilder.buildUndirectedGraphObject(vertices, edges, maxWeight);
    }

    public Graph<Void, Void> generateDAG(int v, int maxWeight) {
        int[] vertices = IntStream.range(0, v).toArray();
        IntObjectHashMap<FastList<Integer>> edges = new IntObjectHashMap<>();
        shuffle(vertices);
        for (int i = 0; i < v - 1; i++) {
            edges.getIfAbsent(vertices[i], FastList::newList).add(vertices[i + 1]);
        }
        long extraEdges = 5L * v - (v - 1);
        long edgeCount = 0;
        while (edgeCount < extraEdges) {
            int i = rng.nextInt(v);
            int j = rng.nextInt(v);
            if (j == i) continue;
            if (j > i) {
                int temp = i;
                i = j;
                j = temp;
            }
            edges.getIfAbsent(vertices[i], FastList::newList).add(vertices[j]);
            edgeCount++;
        }

        return graphBuilder.buildDirectedGraphObject(vertices, edges, maxWeight);
    }

    private void shuffle(int[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            int j = rng.nextInt(i + 1);
            int temp = array[j];
            array[j] = array[i];
            array[i] = temp;
        }
    }


}
