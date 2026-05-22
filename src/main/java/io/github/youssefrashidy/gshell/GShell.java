package io.github.youssefrashidy.gshell;

import io.github.youssefrashidy.graph.Graph;
import io.github.youssefrashidy.graph.Vertex;
import io.github.youssefrashidy.gshell.nodes.CommandNode;
import io.github.youssefrashidy.gshell.nodes.NewNode;

import java.util.HashMap;
import java.util.Scanner;

public class GShell {
    public static HashMap<String, Graph<String, String>> environmentVars = new HashMap<>();
    public static HashMap<String, HashMap<String, Vertex<String>>> vertexNameMaps = new HashMap<>();
    private final Tokenizer tokenizer = new Tokenizer();
    private final Parser parser = new Parser();


    public void shell() {
        printWelcome();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.print("gshell> ");
                String command = scanner.nextLine().trim();
                if (command.isEmpty()) {
                    continue;
                }
                if (command.equalsIgnoreCase("exit") || command.equalsIgnoreCase("quit")) {
                    break;
                }
                var tokens = tokenizer.tokenize(command);
                var nodes = parser.parse(tokens);
                for (var node : nodes) {
                    switch (node) {
                        case NewNode newNode -> newNode.execute();
                        case CommandNode commandNode -> {
                            try {
                                commandNode.execute();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        default ->
                                throw new IllegalArgumentException("Unsupported node: " + node.getClass().getSimpleName());
                    }
                }
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private void printWelcome() {
        System.out.println("""
            ██████╗ ███████╗██╗  ██╗███████╗██╗     ██╗
            ██╔════╝██╔════╝██║  ██║██╔════╝██║     ██║
            ██║  ███╗███████╗███████║█████╗  ██║     ██║
            ██║   ██║╚════██║██╔══██║██╔══╝  ██║     ██║
            ╚██████╔╝███████║██║  ██║███████╗███████╗███████╗
             ╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝
            """);
        System.out.println("  Graph Shell  —  interactive graph algorithm environment");
        System.out.println("  Type 'help' for available commands, 'exit' or 'quit' to leave.\n");
        System.out.println("  Actually there is no help but you know how things are going");
        System.out.println("  Supported commands:");
        System.out.println("    new <id> = new Graph(Directed|Undirected)");
        System.out.println("    <id>.addVertex(\"v\")          <id>.addEdge(\"u\",\"v\",w)");
        System.out.println("    <id>.primMST()               <id>.kruskalMST()");
        System.out.println("    <id>.dijkstra(\"src\")         <id>.dagShortestPath(\"src\")");
        System.out.println("    <id>.visualizePrimMST()      <id>.visualizeKruskalMST()");
        System.out.println("    <id>.visualizeGraph()\n");
    }
}
