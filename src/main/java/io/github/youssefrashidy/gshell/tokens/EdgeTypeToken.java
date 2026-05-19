package io.github.youssefrashidy.gshell.tokens;

import io.github.youssefrashidy.graph.GraphType;

public sealed interface EdgeTypeToken extends Token permits DirectedToken, UndirectedToken {
    GraphType graphType();
}
