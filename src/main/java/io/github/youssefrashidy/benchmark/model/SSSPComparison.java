package io.github.youssefrashidy.benchmark.model;

public record SSSPComparison(SingleRun dijkstra, SingleRun dag) {
    public SSSPComparison {
        assert dijkstra.distribution() == dag.distribution();
    }
}
