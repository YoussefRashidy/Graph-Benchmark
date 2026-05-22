package io.github.youssefrashidy.gshell.nodes;

import io.github.youssefrashidy.graph.Edge;
import io.github.youssefrashidy.graph.Graph;
import io.github.youssefrashidy.graph.Vertex;
import io.github.youssefrashidy.graph.exceptions.EdgeMismatchException;
import io.github.youssefrashidy.gshell.GShell;
import io.github.youssefrashidy.gshell.tokens.CommandToken;
import io.github.youssefrashidy.gshell.tokens.IdentifierToken;
import io.github.youssefrashidy.gshell.tokens.NumberToken;
import io.github.youssefrashidy.gshell.tokens.StringToken;
import io.github.youssefrashidy.gshell.tokens.Token;
import io.github.youssefrashidy.gshell.DotMapper;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public record CommandNode(IdentifierToken identifier, CommandToken command, List<Token> args) implements Node {

    public void execute() throws IOException, InterruptedException {
        Graph<String, String> graph = GShell.environmentVars.get(identifier.name());
        if (graph == null) {
            System.out.println("Graph not found: " + identifier.name());
            return;
        }
        Map<String, Vertex<String>> vertices = GShell.vertexNameMaps.get(identifier.name());
        if (vertices == null) {
            System.out.println("Vertex map not found for graph: " + identifier.name());
            return;
        }

        switch (command.command()) {
            case ADD_VERTEX -> addVertex(graph, vertices, args.getFirst());
            case ADD_VERTICES, ADD_ALL_VERTICES -> {
                for (Token arg : args) {
                    addVertex(graph, vertices, arg);
                }
            }
            case ADD_EDGE -> addEdge(graph, vertices, args, 0);
            case ADD_EDGES -> {
                for (int i = 0; i < args.size(); i += 3) {
                    addEdge(graph, vertices, args, i);
                }
            }
            case PRIM_MST -> System.out.println(formatEdges(graph.primMST(), graph));
            case KRUSKAL_MST -> System.out.println(formatEdges(graph.kruskalMST(), graph));
            case DIJKSTRA -> runDijkstra(graph, vertices, args.getFirst());
            case DAG_SHORTEST_PATH -> runDagShortestPath(graph, vertices, args.getFirst());
            case VISUALIZE_GRAPH -> {
                String dotString = new DotMapper().toDot(graph, identifier.name());
                // create a process and instruct it to load the dot
                String fileName = identifier.name() + "_graph"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) ;
                File dotFile = new File(fileName+".dot");
                Files.writeString(Path.of(dotFile.getAbsolutePath()), dotString);
                Process process = new ProcessBuilder("dot", "-Tpng", dotFile.getAbsolutePath(), "-o", new File(fileName+".png").getAbsolutePath())
                        .redirectErrorStream(true)
                        .start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    String error = new String(process.getInputStream().readAllBytes());
                    throw new RuntimeException("Graphviz failed:\n" + error);
                }
                new ProcessBuilder("cmd", "/c", "start", fileName+".png").start();

            }
            case VISUALIZE_PRIM_MST -> {
                String dotString = new DotMapper().toDotMst(graph, identifier.name(), graph.primMST());
                // create a process and instruct it to load the dot
                String fileName = identifier.name() + "_prim"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) ;

                File dotFile = new File(fileName+".dot");
                Files.writeString(Path.of(dotFile.getAbsolutePath()), dotString);
                Process process = new ProcessBuilder("dot", "-Tpng", dotFile.getAbsolutePath(), "-o", new File(fileName+".png").getAbsolutePath())
                        .redirectErrorStream(true)
                        .start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    String error = new String(process.getInputStream().readAllBytes());
                    throw new RuntimeException("Graphviz failed:\n" + error);
                }
                new ProcessBuilder("cmd", "/c", "start", fileName+".png").start();

            }
            case VISUALIZE_KRUSKAL_MST -> {
                String dotString = new DotMapper().toDotMst(graph, identifier.name(), graph.kruskalMST());
                String fileName = identifier.name() + "_kruskal"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) ;
                // create a process and instruct it to load the dot
                File dotFile = new File(fileName+".dot");
                Files.writeString(Path.of(dotFile.getAbsolutePath()), dotString);
                Process process = new ProcessBuilder("dot", "-Tpng", dotFile.getAbsolutePath(), "-o", new File(fileName+".png").getAbsolutePath())
                        .redirectErrorStream(true)
                        .start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    String error = new String(process.getInputStream().readAllBytes());
                    throw new RuntimeException("Graphviz failed:\n" + error);
                }
                new ProcessBuilder("cmd", "/c", "start", fileName+".png").start();
            }

            case VISUALIZE_DAG_SHORTEST_PATH, VISUALIZE_DIJKSTRA -> System.out.println("Coming soon");
        }
    }

    private void addVertex(Graph<String, String> graph, Map<String, Vertex<String>> vertices, Token token) {
        String name = tokenToString(token);
        if (vertices.containsKey(name)) {
            System.out.println("Vertex already exists: " + name);
            return;
        }
        Vertex<String> vertex = graph.addVertex(name);
        vertices.put(name, vertex);
    }

    private void addEdge(Graph<String, String> graph, Map<String, Vertex<String>> vertices, List<Token> args, int index) {
        String uName = tokenToString(args.get(index));
        String vName = tokenToString(args.get(index + 1));
        int weight = toInt(args.get(index + 2), "edge weight");
        Vertex<String> u = vertices.get(uName);
        Vertex<String> v = vertices.get(vName);
        if (u == null || v == null) {
            System.out.println("Vertex not found for edge: " + uName + " -> " + vName);
            return;
        }
        try {
            graph.addEdge(u, v, null, weight);
        } catch (EdgeMismatchException ex) {
            graph.addDirectedEdge(u, v, null, weight);
        }
    }

    private void runDijkstra(Graph<String, String> graph, Map<String, Vertex<String>> vertices, Token token) {
        String name = tokenToString(token);
        Vertex<String> source = vertices.get(name);
        if (source == null) {
            System.out.println("Vertex not found: " + name);
            return;
        }
        System.out.println(formatDistances(graph.dijkstra(source), graph));
    }

    private void runDagShortestPath(Graph<String, String> graph, Map<String, Vertex<String>> vertices, Token token) {
        String name = tokenToString(token);
        Vertex<String> source = vertices.get(name);
        if (source == null) {
            System.out.println("Vertex not found: " + name);
            return;
        }
        System.out.println(formatDistances(graph.dagShortestPath(source), graph));
    }

    private String formatDistances(IntIntHashMap distances, Graph<String, String> graph) {

        StringBuilder builder = new StringBuilder("{");
        distances.forEachKeyValue((id, distance) -> {
            String name = graph.getVertex(id).getData();
            if (builder.length() > 1) {
                builder.append(", ");
            }
            builder.append(name).append("=").append(distance);
        });
        builder.append("}");
        return builder.toString();
    }

    private String formatEdges(List<Edge<String>> edges, Graph<String, String> graph) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < edges.size(); i++) {
            Edge<String> edge = edges.get(i);
            String uName = graph.getVertex(edge.getU()).getData();
            String vName = graph.getVertex(edge.getV()).getData();
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(uName).append("-").append(vName).append("(").append(edge.getWeight()).append(")");
        }
        builder.append("]");
        return builder.toString();
    }

    private int toInt(Token token, String label) {
        return switch (token) {
            case NumberToken number -> number.value();
            case StringToken text -> {
                try {
                    yield Integer.parseInt(text.value());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Expected " + label + " as a number, got: " + text.value());
                }
            }
            default -> throw new IllegalArgumentException("Expected " + label + " as a number");
        };
    }

    private String tokenToString(Token token) {
        return switch (token) {
            case StringToken text -> text.value();
            case NumberToken number -> Integer.toString(number.value());
            default -> throw new IllegalArgumentException("Expected string or number for vertex data");
        };
    }
}

