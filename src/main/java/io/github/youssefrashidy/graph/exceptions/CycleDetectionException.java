package io.github.youssefrashidy.graph.exceptions;

public class CycleDetectionException extends RuntimeException {
    public CycleDetectionException() {
    }

    public CycleDetectionException(String message) {
        super(message);
    }

    public CycleDetectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CycleDetectionException(Throwable cause) {
        super(cause);
    }

    public CycleDetectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
