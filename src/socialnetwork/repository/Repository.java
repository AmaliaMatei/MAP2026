
package socialnetwork.repository;

import socialnetwork.domain.Entity;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Defines the contract for data repositories.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Saves a new entity.
     *
     * @param entity the entity to save
     * @throws IllegalArgumentException if entity or ID is null
     * @throws socialnetwork.exceptions.ValidationException
     *         if the entity is invalid
     * @throws socialnetwork.exceptions.DuplicateEntityException
     *         if the identifier already exists
     */
    void save(E entity);

    /**
     * Finds an entity by its identifier.
     *
     * @param id the entity identifier
     * @return an Optional containing the entity,
     *         or an empty Optional if not found
     * @throws IllegalArgumentException if id is null
     */
    Optional<E> findOne(ID id);

    /**
     * Returns all stored entities.
     *
     * @return an unmodifiable collection of entities
     */
    Collection<E> findAll();

    /**
     * Filters entities without changing the repository.
     *
     * @param filter predicate defining which
     *               entities are included
     * @return matching entities as a list
     * @throws NullPointerException if filter is null
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
     * @throws IllegalArgumentException if id is null
     * @throws socialnetwork.exceptions.EntityNotFoundException
     *         if the entity does not exist
     */
    void delete(ID id);

    /**
     * Updates an existing entity.
     *
     * @param entity the entity with updated information
     * @throws IllegalArgumentException if entity or ID is null
     * @throws socialnetwork.exceptions.ValidationException
     *         if the entity is invalid
     * @throws socialnetwork.exceptions.EntityNotFoundException
     *         if the entity does not exist
     */
    void update(E entity);
}
