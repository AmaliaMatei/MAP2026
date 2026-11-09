
package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.validation.Validator;
import socialnetwork.exceptions.DuplicateEntityException;
import socialnetwork.exceptions.EntityNotFoundException;

import java.util.*;

/**
 * Generic in-memory repository implementation.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>>
        implements Repository<ID, E> {

    protected final Map<ID, E> entities;
    protected final Validator<E> validator;

    /**
     * Creates an empty repository.
     *
     * @param validator the entity validator
     * @throws IllegalArgumentException if validator is null
     */
    public InMemoryRepository(Validator<E> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "Validator cannot be null."
            );
        }

        this.entities = new HashMap<>();
        this.validator = validator;
    }

    /**
     * Saves a validated entity.
     *
     * @param entity the entity to save
     * @throws IllegalArgumentException if entity or ID is null
     * @throws DuplicateEntityException if the ID exists
     */
    @Override
    public void save(E entity) {

        checkEntity(entity);
        validator.validate(entity);

        ID id = entity.getId();

        if (entities.containsKey(id)) {
            throw new DuplicateEntityException(
                    "Entity with ID " + id + " already exists."
            );
        }

        entities.put(id, entity);
    }

    /**
     * Finds an entity by ID.
     *
     * @param id the identifier
     * @return the entity, if present
     * @throws IllegalArgumentException if ID is null
     */
    @Override
    public Optional<E> findOne(ID id) {

        checkId(id);

        return Optional.ofNullable(entities.get(id));
    }

    /**
     * Returns all stored entities.
     *
     * @return an unmodifiable collection
     */
    @Override
    public Collection<E> findAll() {

        return Collections.unmodifiableCollection(
                entities.values()
        );
    }

    /**
     * Deletes an entity.
     *
     * @param id the entity identifier
     * @throws IllegalArgumentException if ID is null
     * @throws EntityNotFoundException if the entity is missing
     */
    @Override
    public void delete(ID id) {

        checkId(id);

        if (!entities.containsKey(id)) {
            throw new EntityNotFoundException(
                    "Entity with ID " + id + " does not exist."
            );
        }

        entities.remove(id);
    }

    /**
     * Updates an existing entity.
     *
     * @param entity the updated entity
     * @throws IllegalArgumentException if entity or ID is null
     * @throws EntityNotFoundException if the entity is missing
     */
    @Override
    public void update(E entity) {

        checkEntity(entity);
        validator.validate(entity);

        ID id = entity.getId();

        if (!entities.containsKey(id)) {
            throw new EntityNotFoundException(
                    "Entity with ID " + id + " does not exist."
            );
        }

        entities.put(id, entity);
    }

    /**
     * Checks whether an identifier is valid.
     *
     * @param id the identifier
     * @throws IllegalArgumentException if ID is null
     */
    private void checkId(ID id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID cannot be null."
            );
        }
    }

    /**
     * Checks whether an entity has a valid identifier.
     *
     * @param entity the entity to check
     * @throws IllegalArgumentException if entity or ID is null
     */
    private void checkEntity(E entity) {

        if (entity == null) {
            throw new IllegalArgumentException(
                    "Entity cannot be null."
            );
        }

        checkId(entity.getId());
    }
}
