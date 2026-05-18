package io.github.youssefrashidy.benchmark;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.benchmark.model.Algorithm;
import io.github.youssefrashidy.benchmark.model.Distribution;
import io.github.youssefrashidy.benchmark.model.SingleRun;
import io.github.youssefrashidy.graph.Graph;
import io.github.youssefrashidy.graph.Vertex;
import io.github.youssefrashidy.graph.exceptions.SingleRunMismatchException;

@Component
public class BenchmarkRunner {
    private static final int WARM_UP = 5;
    private static final int ITERATIONS = 20;

    SingleRun runSingleRun(Algorithm algorithm, Graph<Void, Void> graph, Distribution distribution , int v) {
        return switch (algorithm) {
            case PRIM -> {
                // warm up
                warmUp(algorithm, graph);
                yield new SingleRun(algorithm, distribution, runPrim(graph) , v);
            }
            case KRUSKAL -> {
                warmUp(algorithm, graph);
                yield new SingleRun(algorithm, distribution, runKruskal(graph) , v);
            }
            default -> throw new SingleRunMismatchException("Algorithm " + algorithm + " requires a source vertex.");
        };
    }

    SingleRun runSingleRun(Algorithm algorithm, Graph<Void, Void> graph, Distribution distribution, Vertex<Void> source,int v) {
        return switch (algorithm) {
            case DIJKSTRA -> {
                // warm up
                warmUp(algorithm, graph, source);
                yield new SingleRun(algorithm, distribution, runDijkstra(graph, source),v);
            }
            case DAG_SP -> {
                warmUp(algorithm, graph, source);
                yield new SingleRun(algorithm, distribution, runDAG(graph, source),v);
            }
            default -> throw new SingleRunMismatchException("Algorithm " + algorithm + " does not require a source vertex.");
        };
    }

    void warmUp(Algorithm algorithm, Graph<Void, Void> graph) {
        for (int i = 0; i < WARM_UP; i++) {
            switch (algorithm) {
                case PRIM -> graph.primMST();
                case KRUSKAL -> graph.kruskalMST();
            }
        }
    }

    void warmUp(Algorithm algorithm, Graph<Void, Void> graph, Vertex<Void> source) {
        for (int i = 0; i < WARM_UP; i++) {
            switch (algorithm) {
                case DIJKSTRA -> graph.dijkstra(source);
                case DAG_SP -> graph.dagShortestPath(source);
            }
        }
    }

    long[] runPrim(Graph<Void, Void> graph) {
        long[] times = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            graph.primMST();
            long end = System.nanoTime();
            times[i] = end - start;
        }
        return times;
    }

    long[] runKruskal(Graph<Void, Void> graph) {
        long[] times = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            graph.kruskalMST();
            long end = System.nanoTime();
            times[i] = end - start;
        }
        return times;
    }

    long[] runDijkstra(Graph<Void, Void> graph, Vertex<Void> source) {
        long[] times = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            graph.dijkstra(source);
            long end = System.nanoTime();
            times[i] = end - start;
        }
        return times;
    }

    long[] runDAG(Graph<Void, Void> graph, Vertex<Void> source) {
        long[] times = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            graph.dagShortestPath(source);
            long end = System.nanoTime();
            times[i] = end - start;
        }
        return times;
    }
}
