
package socialnetwork.repository;

import socialnetwork.domain.User;
import socialnetwork.validation.Validator;

import java.util.Collection;
import java.util.Optional;

/**
 * Database repository skeleton for users.
 * JDBC operations will be implemented in Laboratory 4.
 */
public class UtilizatorDbRepository
        implements Repository<Long, User> {

    private final Validator<User> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a database repository skeleton.
     *
     * @param validator the user validator
     * @param url the database connection URL
     * @param username the database username
     * @param password the database password
     * @throws IllegalArgumentException if a required
     *         constructor argument is null or blank
     */
    public UtilizatorDbRepository(
            Validator<User> validator,
            String url,
            String username,
            String password) {

        if (validator == null ||
                url == null || url.isBlank() ||
                username == null || username.isBlank() ||
                password == null) {

            throw new IllegalArgumentException(
                    "Invalid user database configuration."
            );
        }

        this.validator = validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param entity the user to save
     * @throws UnsupportedOperationException always
     */
    @Override
    public void save(User entity) {
        throw new UnsupportedOperationException(
                "DATABASE user save is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param id the user identifier
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Optional<User> findOne(Long id) {
        throw new UnsupportedOperationException(
                "DATABASE user findOne is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Collection<User> findAll() {
        throw new UnsupportedOperationException(
                "DATABASE user findAll is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param id the user identifier
     * @throws UnsupportedOperationException always
     */
    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException(
                "DATABASE user delete is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param entity the updated user
     * @throws UnsupportedOperationException always
     */
    @Override
    public void update(User entity) {
        throw new UnsupportedOperationException(
                "DATABASE user update is not implemented."
        );
    }
}
