
package socialnetwork.factory;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;

import socialnetwork.repository.Repository;
import socialnetwork.repository.InMemoryRepository;
import socialnetwork.repository.UtilizatorFileRepository;
import socialnetwork.repository.PrietenieFileRepository;
import socialnetwork.repository.UtilizatorDbRepository;
import socialnetwork.repository.PrietenieDbRepository;

import socialnetwork.validation.Validator;
import socialnetwork.exceptions.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Creates repositories based on application configuration.
 * Implements the Simple Factory pattern.
 */
public class RepositoryFactory {

    private final Properties properties;
    private final RepositoryType repositoryType;

    /**
     * Loads the application configuration.
     *
     * @throws RepositoryException if configuration
     *         cannot be loaded
     * @throws IllegalArgumentException if persistence
     *         type is missing or invalid
     */
    public RepositoryFactory() {

        properties = new Properties();

        Path configPath = Paths.get(
                "src/main/resources/config.properties"
        );

        try (InputStream input =
                     Files.newInputStream(configPath)) {

            properties.load(input);

        } catch (IOException e) {
            throw new RepositoryException(
                    "Cannot load configuration: " + configPath,
                    e
            );
        }

        String type = getRequiredProperty("repository.type");

        try {
            repositoryType = RepositoryType.valueOf(
                    type.toUpperCase()
            );

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown repository.type: " + type
                            + ". Expected MEMORY, FILE or DATABASE.",
                    e
            );
        }

        if (repositoryType == RepositoryType.FILE) {
            getRequiredProperty("users.file");
            getRequiredProperty("friendships.file");
        }

        if (repositoryType == RepositoryType.DATABASE) {
            getRequiredProperty("db.url");
            getRequiredProperty("db.user");
            getRequiredProperty("db.password", true);
        }
    }

    /**
     * Returns a required configuration value.
     *
     * @param key the property key
     * @return the trimmed property value
     * @throws IllegalArgumentException if missing or blank
     */
    private String getRequiredProperty(String key) {
        return getRequiredProperty(key, false);
    }

    /**
     * Reads a property and optionally allows an empty value.
     *
     * @param key the property key
     * @param allowEmpty whether an empty value is accepted
     * @return the configuration value
     * @throws IllegalArgumentException if invalid
     */
    private String getRequiredProperty(
            String key, boolean allowEmpty) {

        String value = properties.getProperty(key);

        if (value == null) {
            throw new IllegalArgumentException(
                    "Missing configuration property: " + key
            );
        }

        value = value.trim();

        if (!allowEmpty && value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Empty configuration property: " + key
            );
        }

        return value;
    }

    /**
     * Creates the configured user repository lazily.
     *
     * @param validator validation behavior for users
     * @return the configured repository
     * @throws IllegalArgumentException if validator is null
     */
    public Repository<Long, User> createUtilizatorRepository(
            Validator<User> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "User validator cannot be null."
            );
        }

        Map<RepositoryType,
                Supplier<Repository<Long, User>>> creators =
                new EnumMap<>(RepositoryType.class);

        creators.put(
                RepositoryType.MEMORY,
                () -> new InMemoryRepository<>(validator)
        );

        creators.put(
                RepositoryType.FILE,
                () -> new UtilizatorFileRepository(
                        Paths.get(
                                getRequiredProperty("users.file")
                        ),
                        validator
                )
        );

        creators.put(
                RepositoryType.DATABASE,
                () -> new UtilizatorDbRepository(
                        validator,
                        getRequiredProperty("db.url"),
                        getRequiredProperty("db.user"),
                        getRequiredProperty(
                                "db.password", true
                        )
                )
        );

        return creators.get(repositoryType).get();
    }

    /**
     * Creates the configured friendship repository lazily.
     *
     * @param validator validation behavior for friendships
     * @return the configured repository
     * @throws IllegalArgumentException if validator is null
     */
    public Repository<Tuple<Long, Long>, Friendship>
    createPrietenieRepository(
            Validator<Friendship> validator) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "Friendship validator cannot be null."
            );
        }

        Map<RepositoryType,
                Supplier<Repository<Tuple<Long, Long>, Friendship>>>
                creators = new EnumMap<>(RepositoryType.class);

        creators.put(
                RepositoryType.MEMORY,
                () -> new InMemoryRepository<>(validator)
        );

        creators.put(
                RepositoryType.FILE,
                () -> new PrietenieFileRepository(
                        Paths.get(
                                getRequiredProperty("friendships.file")
                        ),
                        validator
                )
        );

        creators.put(
                RepositoryType.DATABASE,
                () -> new PrietenieDbRepository(
                        validator,
                        getRequiredProperty("db.url"),
                        getRequiredProperty("db.user"),
                        getRequiredProperty(
                                "db.password", true
                        )
                )
        );

        return creators.get(repositoryType).get();
    }
}
