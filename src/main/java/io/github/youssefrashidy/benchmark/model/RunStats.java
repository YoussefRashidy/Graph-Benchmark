package io.github.youssefrashidy.benchmark.model;

public record RunStats(
        Algorithm algorithm,
        Distribution distribution,
        int v,
        double meanNanos,
        double medianNanos,
        double stdDevNanos,
        double speedUp
) {
}
