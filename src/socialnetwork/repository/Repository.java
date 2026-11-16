
package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.exceptions.RepositoryException;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Defines the contract for data repositories.
 *
 * @param <ID> the entity identifier type
 * @param <E> the entity type
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Saves a new entity.
     *
     * @param entity the entity to save
     * @throws IllegalArgumentException if entity is null
     * @throws RepositoryException if persistence fails
     */
    void save(E entity);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the entity identifier
     * @return an Optional containing the entity,
     *         or empty if not found
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if retrieval fails
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all stored entities.
     *
     * @return a collection of entities
     * @throws RepositoryException if retrieval fails
     */
    Collection<E> findAll();

    /**
     * Filters entities using a predicate.
     *
     * @param filter the filtering predicate
     * @return the matching entities
     * @throws NullPointerException if filter is null
     * @throws RepositoryException if retrieval fails
     */
    default List<E> findAll(Predicate<E> filter) {
        return findAll()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the entity identifier
     * @return the deleted entity
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if deletion fails
     */
    E delete(ID id);

    /**
     * Updates an existing entity.
     *
     * @param entity the entity with updated data
     * @return null if the update succeeds,
     *         or the entity if its ID does not exist
     * @throws IllegalArgumentException if entity
     *         or its identifier is null
     * @throws RepositoryException if updating fails
     */
    E update(E entity);
}
