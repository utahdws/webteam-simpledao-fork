package org.simpledao.exceptions;

public class SimpleDAOBaseRuntimeException extends RuntimeException {

    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.
     */
    public SimpleDAOBaseRuntimeException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * @param   message   the detail message.
     */
    public SimpleDAOBaseRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * @param  message the detail message.
     * @param  cause the cause.
     */
    public SimpleDAOBaseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new runtime exception with the specified cause.
     * @param  cause the cause.
     */
    public SimpleDAOBaseRuntimeException(Throwable cause) {
        super(cause);
    }
}