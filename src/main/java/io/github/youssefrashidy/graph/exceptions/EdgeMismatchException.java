package io.github.youssefrashidy.graph.exceptions;

public class EdgeMismatchException extends RuntimeException{
    public EdgeMismatchException() {
    }

    public EdgeMismatchException(String message) {
        super(message);
    }

    public EdgeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public EdgeMismatchException(Throwable cause) {
        super(cause);
    }

    public EdgeMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
