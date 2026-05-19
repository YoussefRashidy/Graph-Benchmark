package io.github.youssefrashidy.gshell.tokens;

import io.github.youssefrashidy.graph.GraphType;

public record UndirectedToken() implements EdgeTypeToken {
    @Override
    public GraphType graphType() {
        return GraphType.UNDIRECTED;
    }
}
