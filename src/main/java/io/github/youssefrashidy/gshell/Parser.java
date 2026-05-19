package io.github.youssefrashidy.gshell;

import java.util.ArrayList;
import java.util.List;

import io.github.youssefrashidy.gshell.tokens.*;
import io.github.youssefrashidy.gshell.nodes.*;


public class Parser {
    int position = 0;
    List<Node> nodes;

    List<Node> parse(List<Token> tokens) {
        nodes = new ArrayList<>();
        position = 0;
        switch (tokens.get(position)) {
            case TypeToken typeToken -> parseCreateGraph(tokens);
            case IdentifierToken identifierToken -> parseCommand(tokens);
            default -> {
                //reset position to 0 for the error message to be accurate
                throw new IllegalArgumentException("Unexpected token %s".formatted(tokens.get(position)));
            }
        }
        return nodes;
    }

    private void parseCreateGraph(List<Token> tokens) {
        TypeToken type = (TypeToken) consume(TypeToken.class, tokens, "Expected graph type");
        IdentifierToken identifierToken = (IdentifierToken) consume(IdentifierToken.class, tokens, "Expected graph name");
        EqualsToken equalsToken = (EqualsToken) consume(EqualsToken.class, tokens, "Expected '='");
        NewToken newToken = (NewToken) consume(NewToken.class, tokens, "Expected 'new'");
        TypeToken createToken = (TypeToken) consume(TypeToken.class, tokens, "Expected 'Graph'");
        OpenParToken openParToken = (OpenParToken) consume(OpenParToken.class, tokens, "Expected '('");
        EdgeTypeToken edgeTypeToken = (EdgeTypeToken) consume(EdgeTypeToken.class, tokens, "Expected 'Directed' or 'Undirected'");
        CloseParToken closeParToken = (CloseParToken) consume(CloseParToken.class, tokens, "Expected ')'");
        // Validate that there are no extra tokens
        if (position != tokens.size()) {
            throw new IllegalArgumentException("Unexpected token %s at position %d".formatted(tokens.get(position), position));
        }
        // Valid create graph statement, create the node and add it to the list of nodes
        if (type.type() != createToken.type()) {
            throw new IllegalArgumentException("Graph type mismatch: expected %s but got %s at position %d".formatted(type.type(), createToken.type(), position));
        }
        NewNode node = new NewNode(type, identifierToken, edgeTypeToken);
        nodes.add(node);
    }

    private void parseCommand(List<Token> tokens) {
        IdentifierToken identifierToken = (IdentifierToken) consume(IdentifierToken.class, tokens, "Expected graph name");
        DotToken dotToken = (DotToken) consume(DotToken.class, tokens, "Expected '.'");
        CommandToken commandToken = (CommandToken) consume(CommandToken.class, tokens, "Expected command");
        OpenParToken openParToken = (OpenParToken) consume(OpenParToken.class, tokens, "Expected '('");
        // Parse the arguments of the command, which can be either a number or a string, and can be separated by commas
        List<Token> args = new ArrayList<>();
        int argsCount = 0;
        while (position < tokens.size() && !(tokens.get(position) instanceof CloseParToken)) {
            if (tokens.get(position) instanceof NumberToken || tokens.get(position) instanceof StringToken) {
                args.add(consume(Token.class, tokens, "Expected argument"));
                argsCount++;
            } else if (tokens.get(position) instanceof CommaToken) {
                consume(CommaToken.class, tokens, "Expected ','");
            } else {
                throw new IllegalArgumentException("Unexpected token %s at position %d".formatted(tokens.get(position), position));
            }
        }

        if (!matchArgs(argsCount, commandToken)) {
            throw new IllegalArgumentException("Invalid number of arguments for command %s at position %d".formatted(commandToken.command(), position));
        }
        CloseParToken closeParToken = (CloseParToken) consume(CloseParToken.class, tokens, "Expected ')'");

        if (position != tokens.size()) {
            throw new IllegalArgumentException("Unexpected token %s at position %d".formatted(tokens.get(position), position));
        }
        CommandNode node = new CommandNode(identifierToken, commandToken, args);
        nodes.add(node);
    }

    private Token consume(Class<? extends Token> cla, List<Token> tokens, String message) {
        if (position >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of input, expected " + message);
        }
        if (cla.isInstance(tokens.get(position))) {
            return tokens.get(position++);
        }
        throw new IllegalArgumentException(message + " at position " + position);
    }

    private boolean matchArgs(int argc, CommandToken token) {
        return switch (token.command()) {
            case PRIM_MST, KRUSKAL_MST, VISUALIZE_PRIM_MST, VISUALIZE_KRUSKAL_MST, VISUALIZE_GRAPH -> argc == 0;
            case DIJKSTRA, DAG_SHORTEST_PATH, VISUALIZE_DIJKSTRA, VISUALIZE_DAG_SHORTEST_PATH -> argc == 1;
            case ADD_VERTEX -> argc == 1;
            case ADD_VERTICES, ADD_ALL_VERTICES -> argc >= 1;
            case ADD_EDGE -> argc == 3;
            case ADD_EDGES -> argc > 0 && argc % 3 == 0;
        };
    }
}
