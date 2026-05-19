package io.github.youssefrashidy.gshell.tokens;

public sealed interface Token permits TypeToken, IdentifierToken, NewToken, EdgeTypeToken,
        CommandToken, OpenParToken, CloseParToken, StringToken, DotToken, CommaToken, EqualsToken, NumberToken {
}
