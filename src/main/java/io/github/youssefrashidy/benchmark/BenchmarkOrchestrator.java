package io.github.youssefrashidy.benchmark;

import io.github.youssefrashidy.annotations.Component;
import io.github.youssefrashidy.annotations.Inject;
import io.github.youssefrashidy.benchmark.graphGenerator.GraphGenerator;

@Component
public class BenchmarkOrchestrator {
    BenchmarkRunner runner;
    GraphGenerator generator;

    @Inject
    public BenchmarkOrchestrator(BenchmarkRunner runner, GraphGenerator generator) {
        this.runner = runner;
        this.generator = generator;
    }
}
