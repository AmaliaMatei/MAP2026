
package socialnetwork.domain;

/**
 * Represents an entity with a unique identifier.
 *
 * @param <ID> the identifier type
 */
public interface Entity<ID> {

    /**
     * Returns the identifier of the entity.
     *
     * @return the entity identifier
     */
    ID getId();
}
