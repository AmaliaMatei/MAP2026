package socialnetwork.exceptions;

/**
 * Exceptie aruncata pentru date invalide.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}