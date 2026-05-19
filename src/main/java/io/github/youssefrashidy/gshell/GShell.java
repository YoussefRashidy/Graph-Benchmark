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

    }
}
