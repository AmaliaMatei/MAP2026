package socialnetwork.exceptions;

/**
 * Exceptie aruncata cand o entitate nu exista.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}