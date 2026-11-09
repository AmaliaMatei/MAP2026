
package socialnetwork.repository;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.validation.Validator;

import java.util.Collection;
import java.util.Optional;

/**
 * Database repository skeleton for friendships.
 * JDBC operations will be implemented in Laboratory 4.
 */
public class PrietenieDbRepository
        implements Repository<Tuple<Long, Long>, Friendship> {

    private final Validator<Friendship> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a friendship database repository skeleton.
     *
     * @param validator the friendship validator
     * @param url the database URL
     * @param username the database username
     * @param password the database password
     * @throws IllegalArgumentException if a required
     *         constructor argument is null or blank
     */
    public PrietenieDbRepository(
            Validator<Friendship> validator,
            String url,
            String username,
            String password) {

        if (validator == null ||
                url == null || url.isBlank() ||
                username == null || username.isBlank() ||
                password == null) {

            throw new IllegalArgumentException(
                    "Invalid friendship database configuration."
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
     * @param entity the friendship to save
     * @throws UnsupportedOperationException always
     */
    @Override
    public void save(Friendship entity) {
        throw new UnsupportedOperationException(
                "DATABASE friendship save is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param id the friendship identifier
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Optional<Friendship> findOne(
            Tuple<Long, Long> id) {

        throw new UnsupportedOperationException(
                "DATABASE friendship findOne is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    public Collection<Friendship> findAll() {
        throw new UnsupportedOperationException(
                "DATABASE friendship findAll is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param id the friendship identifier
     * @throws UnsupportedOperationException always
     */
    @Override
    public void delete(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(
                "DATABASE friendship delete is not implemented."
        );
    }

    /**
     * Not implemented until Laboratory 4.
     *
     * @param entity the updated friendship
     * @throws UnsupportedOperationException always
     */
    @Override
    public void update(Friendship entity) {
        throw new UnsupportedOperationException(
                "DATABASE friendship update is not implemented."
        );
    }
}
