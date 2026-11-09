
package socialnetwork.repository;

import socialnetwork.domain.Entity;
import socialnetwork.validation.Validator;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.exceptions.ValidationException;
import socialnetwork.exceptions.DuplicateEntityException;

import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Stream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic repository that stores entities in a text file.
 * Implements the Template Method pattern.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>>
        extends InMemoryRepository<ID, E> {

    private final Path filePath;

    /**
     * Creates a file repository.
     *
     * @param filePath the file location
     * @param validator the entity validator
     * @throws IllegalArgumentException if filePath is null
     */
    protected AbstractFileRepository(
            Path filePath,
            Validator<E> validator) {

        super(validator);

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null."
            );
        }

        this.filePath = filePath;
    }

    /**
     * Converts a text line into an entity.
     *
     * @param line the text line
     * @return the extracted entity
     * @throws IllegalArgumentException if the line is invalid
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into a text line.
     *
     * @param entity the entity to convert
     * @return the serialized text representation
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Loads entities from the file.
     * Invalid lines are reported and ignored.
     *
     * @throws RepositoryException if reading fails
     */
    protected final void loadData() {

        if (!Files.exists(filePath)) {
            return;
        }

        try (Stream<String> stream = Files.lines(
                filePath, StandardCharsets.UTF_8)) {

            List<String> lines =
                    stream.collect(Collectors.toList());

            IntStream.range(0, lines.size())
                    .forEach(index -> {
                        try {
                            super.save(
                                    extractEntity(lines.get(index))
                            );
                        } catch (
                                IllegalArgumentException |
                                ValidationException |
                                DuplicateEntityException e) {

                            reportInvalidLine(index + 1, e);
                        }
                    });

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot read file: " + filePath,
                    e
            );
        }
    }

    /**
     * Reports an invalid input line.
     *
     * @param number the line number
     * @param cause the reason
     */
    private void reportInvalidLine(
            int number,
            RuntimeException cause) {

        System.err.println(
                "Invalid line " + number
                        + " in " + filePath
                        + ": " + cause.getMessage()
        );
    }

    /**
     * Writes all entities to the file using UTF-8.
     * The original file is replaced after a successful write.
     *
     * @throws RepositoryException if writing fails
     */
    private void writeToFile() {

        Path temporaryFile = null;

        try {

            Path absolutePath = filePath.toAbsolutePath();
            Path parent = absolutePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            temporaryFile = Files.createTempFile(
                    parent,
                    "repository-",
                    ".tmp"
            );

            List<String> lines = entities.values()
                    .stream()
                    .map(this::createEntityAsString)
                    .collect(Collectors.toList());

            Files.write(
                    temporaryFile,
                    lines,
                    StandardCharsets.UTF_8
            );

            try {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot write file: " + filePath,
                    e
            );

        } finally {

            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException e) {
                    System.err.println(
                            "Cannot remove temporary file: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Saves an entity and updates the file.
     *
     * @param entity the entity to save
     * @throws RepositoryException if writing fails
     */
    @Override
    public void save(E entity) {

        Map<ID, E> backup =
                new HashMap<ID, E>(entities);

        super.save(entity);

        try {
            writeToFile();

        } catch (RepositoryException e) {
            entities.clear();
            entities.putAll(backup);
            throw e;
        }
    }

    /**
     * Deletes an entity and updates the file.
     *
     * @param id the identifier of the entity
     * @throws RepositoryException if writing fails
     */
    @Override
    public void delete(ID id) {

        Map<ID, E> backup =
                new HashMap<ID, E>(entities);

        super.delete(id);

        try {
            writeToFile();

        } catch (RepositoryException e) {
            entities.clear();
            entities.putAll(backup);
            throw e;
        }
    }

    /**
     * Updates an entity and rewrites the file.
     *
     * @param entity the updated entity
     * @throws RepositoryException if writing fails
     */
    @Override
    public void update(E entity) {

        Map<ID, E> backup =
                new HashMap<ID, E>(entities);

        super.update(entity);

        try {
            writeToFile();

        } catch (RepositoryException e) {
            entities.clear();
            entities.putAll(backup);
            throw e;
        }
    }
}
