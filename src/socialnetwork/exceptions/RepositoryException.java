
package socialnetwork.exceptions;

/**
 * Represents an unchecked exception caused by
 * a repository persistence operation.
 */
public class RepositoryException extends RuntimeException {

    /**
     * Creates a repository exception with a message.
     *
     * @param message the error description
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Creates a repository exception and preserves
     * the original cause.
     *
     * @param message the error description
     * @param cause the original exception
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
