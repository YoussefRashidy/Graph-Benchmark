package io.github.youssefrashidy.gshell.nodes;

import io.github.youssefrashidy.graph.Graph;
import io.github.youssefrashidy.gshell.GShell;
import io.github.youssefrashidy.gshell.tokens.EdgeTypeToken;
import io.github.youssefrashidy.gshell.tokens.IdentifierToken;
import io.github.youssefrashidy.gshell.tokens.TypeToken;

import java.util.HashMap;

public record NewNode(TypeToken type, IdentifierToken name, EdgeTypeToken edgeType) implements Node {
    public void execute() {
        if (GShell.environmentVars.containsKey(name.name())) {
            System.out.println("Graph already exists: " + name.name());
            return;
        }

        switch (type.type()) {
            case GRAPH -> {
                Graph<String, String> graph = new Graph<>(edgeType.graphType());
                GShell.environmentVars.put(name.name(), graph);
                GShell.vertexNameMaps.put(name.name(), new HashMap<>());
            }
        }
        System.out.println("Created " + type.type() + " graph: " + name.name());
    }
}
