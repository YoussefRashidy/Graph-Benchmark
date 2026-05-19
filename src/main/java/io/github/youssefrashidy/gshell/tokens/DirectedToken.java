package io.github.youssefrashidy.gshell.tokens;

import io.github.youssefrashidy.graph.GraphType;

public record DirectedToken() implements EdgeTypeToken {
    @Override
    public GraphType graphType() {
        return GraphType.DIRECTED;
    }
}
