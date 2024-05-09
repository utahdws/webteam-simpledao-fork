package org.simpledao;

/**
 * <p>A simple exception used by the SimpleDAO framework 
 */
public class SimpleDAOException extends RuntimeException
{
    public SimpleDAOException()
    {
        super();
    }

    public SimpleDAOException(String message)
    {
        super(message);
    }

    public SimpleDAOException(Throwable cause)
    {
        super(cause);
    }

    public SimpleDAOException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
