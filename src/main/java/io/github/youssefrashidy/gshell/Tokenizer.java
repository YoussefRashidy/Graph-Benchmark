package io.github.youssefrashidy.gshell;

import io.github.youssefrashidy.gshell.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tokenizer {

    Map<Character, Token> singleCharTokens = Map.of(
            '.', new DotToken(),
            '(', new OpenParToken(),
            ')', new CloseParToken(),
            ',', new CommaToken(),
            '=', new EqualsToken()
    );

    List<Token> tokenize(String context) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean inString = false;
        for (char c : context.toCharArray()) {
            if (c == '"') inString = !inString;
            if (!inString) {
                if (!isDelimiterToken(c)) {
                    token.append(c);
                } else {
                    if (!token.isEmpty()) {
                        tokens.add(createToken(token.toString()));
                        token.setLength(0);
                    }
                    if (c != ' ' && c != '\t') tokens.add(singleCharTokens.get(c));
                }
            } else token.append(c);
        }
        // Flush trailing token when input does not end with a delimiter.
        if (!token.isEmpty()) {
            tokens.add(createToken(token.toString()));
        }
        return tokens;
    }

    boolean isDelimiterToken(char c) {
        return c == '.' ||
                c == '(' ||
                c == ')' ||
                c == ',' ||
                c == '=' ||
                c == ' ' ||
                c == '\t';
    }

    private Token createToken(String token) {
        return switch (token) {
            case "Graph" -> new TypeToken(TypeName.GRAPH);
            case "new" -> new NewToken();
            case "Directed" -> new DirectedToken();
            case "Undirected" -> new UndirectedToken();
            case "addVertex" -> new CommandToken(CommandType.ADD_VERTEX);
            case "addVertices" -> new CommandToken(CommandType.ADD_VERTICES);
            case "addAllVertices", "add_all_vertices" -> new CommandToken(CommandType.ADD_ALL_VERTICES);
            case "addEdge" -> new CommandToken(CommandType.ADD_EDGE);
            case "addEdges" -> new CommandToken(CommandType.ADD_EDGES);
            case "primMST" -> new CommandToken(CommandType.PRIM_MST);
            case "kruskalMST" -> new CommandToken(CommandType.KRUSKAL_MST);
            case "dijkstra" -> new CommandToken(CommandType.DIJKSTRA);
            case "dagShortestPath" -> new CommandToken(CommandType.DAG_SHORTEST_PATH);
            case "visualizePrimMST" -> new CommandToken(CommandType.VISUALIZE_PRIM_MST);
            case "visualizeKruskalMST" -> new CommandToken(CommandType.VISUALIZE_KRUSKAL_MST);
            case "visualizeDijkstra" -> new CommandToken(CommandType.VISUALIZE_DIJKSTRA);
            case "visualizeDagShortestPath" -> new CommandToken(CommandType.VISUALIZE_DAG_SHORTEST_PATH);
            case "visualizeGraph" -> new CommandToken(CommandType.VISUALIZE_GRAPH);
            default -> {
                try {
                    Integer.parseInt(token);
                    yield new NumberToken(Integer.parseInt(token));
                } catch (RuntimeException _) {
                }
                if (token.startsWith("\"") && token.endsWith("\"")) {
                    yield new StringToken(token.substring(1, token.length() - 1));
                }
                yield new IdentifierToken(token);
            }
        };
    }
}
