package socialnetwork.exceptions;

/**
 * Exceptie aruncata cand o entitate exista deja.
 */
public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message) {
        super(message);
    }
}