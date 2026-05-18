package io.github.youssefrashidy.benchmark;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.annotations.Inject;
import io.github.youssefrashidy.benchmark.graphGenerator.GraphGenerator;
import io.github.youssefrashidy.benchmark.model.*;
import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.List;

@Component
public class BenchmarkOrchestrator {
    BenchmarkRunner runner;
    GraphGenerator generator;
    private static final int[] SIZES = {1000, 2500, 5000, 10000};
    private static final int GRAPHS_PER_SIZE = 10;
    private static final int MAX_WEIGHT = 1000;
    private static final int COMPLETE_MAX_V = 5000; // cap complete graph 50M Edge is too much


    @Inject
    public BenchmarkOrchestrator(BenchmarkRunner runner, GraphGenerator generator) {
        this.runner = runner;
        this.generator = generator;
    }

    public BenchmarkResult runAll() {
        System.out.println("Running MST benchmarks...");
        List<MSTComparison> mst = compareMST();
        System.gc();

        System.out.println("Running SSSP general benchmarks...");
        List<SSSPGeneral> sssp = runSSSPGeneral();
        System.gc();

        System.out.println("Running SSSP DAG comparison...");
        List<SSSPComparison> dag = compareSSSP();
        System.gc();

        return new BenchmarkResult(mst, sssp, dag);
    }

    public List<MSTComparison> compareMST() {
        List<MSTComparison> list = FastList.newList();
        for (int size : SIZES) {
            for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                var sparseGraph = generator.generateSparseGraph(size, MAX_WEIGHT);
                list.add(
                        new MSTComparison(
                                runner.runSingleRun(Algorithm.PRIM, sparseGraph, Distribution.SPARSE, size),
                                runner.runSingleRun(Algorithm.KRUSKAL, sparseGraph, Distribution.SPARSE, size)
                        )
                );
                System.gc();
            }

            for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                var sparseGraph = generator.generateDenseGraph(size, MAX_WEIGHT);
                list.add(
                        new MSTComparison(
                                runner.runSingleRun(Algorithm.PRIM, sparseGraph, Distribution.DENSE, size),
                                runner.runSingleRun(Algorithm.KRUSKAL, sparseGraph, Distribution.DENSE, size)
                        )
                );
                System.gc();
            }

            if (size <= COMPLETE_MAX_V) {
                for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                    var sparseGraph = generator.generateCompleteGraph(size, MAX_WEIGHT);
                    list.add(
                            new MSTComparison(
                                    runner.runSingleRun(Algorithm.PRIM, sparseGraph, Distribution.COMPLETE, size),
                                    runner.runSingleRun(Algorithm.KRUSKAL, sparseGraph, Distribution.COMPLETE, size)
                            )
                    );
                    System.gc();
                }
            }
        }
        return list;
    }

    List<SSSPGeneral> runSSSPGeneral() {
        List<SSSPGeneral> list = FastList.newList();
        for (int size : SIZES) {
            for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                var sparseGraph = generator.generateSparseGraph(size, MAX_WEIGHT);
                // extract source
                var source = sparseGraph.getVertex(0);
                list.add(
                        new SSSPGeneral(
                                runner.runSingleRun(Algorithm.DIJKSTRA, sparseGraph, Distribution.SPARSE, source, size)
                        )
                );
                System.gc();
            }

            for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                var denseGraph = generator.generateDenseGraph(size, MAX_WEIGHT);
                var source = denseGraph.getVertex(0);
                list.add(
                        new SSSPGeneral(
                                runner.runSingleRun(Algorithm.DIJKSTRA, denseGraph, Distribution.DENSE, source, size)
                        )
                );
                System.gc();
            }

            for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                var dagGraph = generator.generateDAG(size, MAX_WEIGHT);
                var source = dagGraph.getVertex(dagGraph.topologicalSort().pop());

                list.add(
                        new SSSPGeneral(
                                runner.runSingleRun(Algorithm.DIJKSTRA, dagGraph, Distribution.DAG, source, size)
                        )
                );
                System.gc();
            }

            if (size <= COMPLETE_MAX_V) {
                for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                    var completeGraph = generator.generateCompleteGraph(size, MAX_WEIGHT);
                    var source = completeGraph.getVertex(0);

                    list.add(
                            new SSSPGeneral(
                                    runner.runSingleRun(Algorithm.DIJKSTRA, completeGraph, Distribution.COMPLETE, source, size)
                            )
                    );
                    System.gc();
                }
            }
        }
        return list;
    }

    List<SSSPComparison> compareSSSP() {
        List<SSSPComparison> list = FastList.newList();
        for (int size : SIZES) {
            for (int i = 0; i < GRAPHS_PER_SIZE; i++) {
                var dagGraph = generator.generateDAG(size, MAX_WEIGHT);
                var source = dagGraph.getVertex(dagGraph.topologicalSort().pop());
                list.add(
                        new SSSPComparison(
                                runner.runSingleRun(Algorithm.DIJKSTRA, dagGraph, Distribution.DAG, source, size),
                                runner.runSingleRun(Algorithm.DAG_SP, dagGraph, Distribution.DAG, source, size)
                        )
                );
                System.gc();
            }
        }
        return list;
    }
}
