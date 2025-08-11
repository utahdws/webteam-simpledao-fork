package org.simpledao.exceptions;

public class ReflectionUtilsRuntimeException extends RuntimeException {

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.
     */
    public ReflectionUtilsRuntimeException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * @param   message   the detail message.
     */
    public ReflectionUtilsRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * @param  message the detail message.
     * @param  cause the cause.
     */
    public ReflectionUtilsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause.
     * @param  cause the cause.
     */
    public ReflectionUtilsRuntimeException(Throwable cause) {
        super(cause);
    }
}