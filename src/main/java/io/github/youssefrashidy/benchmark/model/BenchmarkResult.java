package io.github.youssefrashidy.benchmark.model;

import java.util.List;

public record BenchmarkResult(
        List<MSTComparison> mstComparisons,
        List<SSSPGeneral> ssspGeneral,
        List<SSSPComparison> ssspComparisons
) {
}
