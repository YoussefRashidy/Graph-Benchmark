package io.github.youssefrashidy.benchmark.model;

public record MSTComparison(SingleRun prim, SingleRun kruskal) {
    public MSTComparison {
        assert prim.distribution() == kruskal.distribution();
    }
}
