
package socialnetwork.repository;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.exceptions.EntityNotFoundException;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.validation.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for social network friendships.
 * Uses JDBC and a composite primary key (id1, id2).
 */
public class PrietenieDbRepository
        implements Repository<Tuple<Long, Long>, Friendship> {

    private final Validator<Friendship> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a database repository for friendships.
     *
     * @param validator the friendship validator
     * @param url the PostgreSQL JDBC URL
     * @param username the database username
     * @param password the database password
     * @throws IllegalArgumentException if configuration is invalid
     */
    public PrietenieDbRepository(
            Validator<Friendship> validator,
            String url,
            String username,
            String password) {

        if (validator == null ||
                url == null || url.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
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
     * Opens a PostgreSQL database connection.
     *
     * @return a new JDBC connection
     * @throws SQLException if connection cannot be established
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                url, username, password
        );
    }

    /**
     * Converts a database row into a Friendship object.
     *
     * @param rs the result set positioned on a row
     * @return the mapped friendship
     * @throws SQLException if reading the row fails
     */
    private Friendship mapFriendship(ResultSet rs)
            throws SQLException {

        Long id1 = rs.getLong("id1");
        Long id2 = rs.getLong("id2");

        Timestamp timestamp =
                rs.getTimestamp("friends_from");

        return new Friendship(
                id1,
                id2,
                timestamp.toLocalDateTime()
        );
    }

    /**
     * Validates and normalizes a friendship identifier.
     *
     * @param id the composite friendship identifier
     * @return the normalized identifier
     * @throws IllegalArgumentException if the ID is invalid
     */
    private Tuple<Long, Long> normalizeId(
            Tuple<Long, Long> id) {

        if (id == null ||
                id.getFirst() == null ||
                id.getSecond() == null) {

            throw new IllegalArgumentException(
                    "Friendship ID cannot be null."
            );
        }

        Long first = id.getFirst();
        Long second = id.getSecond();

        if (first <= 0 || second <= 0 ||
                first.equals(second)) {

            throw new IllegalArgumentException(
                    "Invalid friendship user IDs."
            );
        }

        if (first < second) {
            return id;
        }

        return new Tuple<>(second, first);
    }

    /**
     * Converts SQL errors into repository exceptions.
     *
     * @param operation the failed database operation
     * @param e the original SQL exception
     * @return the corresponding repository exception
     */
    private RepositoryException sqlError(
            String operation, SQLException e) {

        String state = e.getSQLState();

        if ((state != null && state.startsWith("08"))
                || "57P01".equals(state)) {

            return new RepositoryException(
                    "Nu se poate realiza conexiunea la PostgreSQL. "
                            + "Verifica daca serverul este pornit.",
                    e
            );
        }

        if ("23505".equals(state)) {
            return new RepositoryException(
                    "Prietenia exista deja in baza de date.",
                    e
            );
        }

        if ("23503".equals(state)) {
            return new RepositoryException(
                    "Prietenia nu poate fi salvata: "
                            + "unul dintre utilizatori nu exista.",
                    e
            );
        }

        if ("23502".equals(state) ||
                "23514".equals(state)) {

            return new RepositoryException(
                    "Datele prieteniei incalca "
                            + "o constrangere a bazei de date.",
                    e
            );
        }

        return new RepositoryException(
                "Eroare PostgreSQL la operatia "
                        + operation + ": " + e.getMessage(),
                e
        );
    }

    /**
     * Saves a validated friendship in PostgreSQL.
     *
     * @param entity the friendship to save
     * @throws IllegalArgumentException if entity is null
     * @throws RepositoryException if inserting fails
     */
    @Override
    public void save(Friendship entity) {

        if (entity == null) {
            throw new IllegalArgumentException(
                    "Friendship cannot be null."
            );
        }

        validator.validate(entity);

        String sql =
                "INSERT INTO friendships " +
                        "(id1, id2, friends_from) " +
                        "VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, entity.getUserId1());
            statement.setLong(2, entity.getUserId2());
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            entity.getFriendshipDate()
                    )
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new RepositoryException(
                        "Prietenia nu a fost salvata."
                );
            }

        } catch (SQLException e) {
            throw sqlError("save friendship", e);
        }
    }

    /**
     * Finds a friendship by its composite identifier.
     *
     * @param id the friendship identifier
     * @return an Optional containing the friendship if found
     * @throws IllegalArgumentException if ID is invalid
     * @throws RepositoryException if querying fails
     */
    @Override
    public Optional<Friendship> findOne(
            Tuple<Long, Long> id) {

        Tuple<Long, Long> normalizedId = normalizeId(id);

        String sql =
                "SELECT id1, id2, friends_from " +
                        "FROM friendships " +
                        "WHERE id1 = ? AND id2 = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, normalizedId.getFirst());
            statement.setLong(2, normalizedId.getSecond());

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapFriendship(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw sqlError("find friendship", e);
        }
    }

    /**
     * Returns all friendships stored in PostgreSQL.
     *
     * @return the collection of stored friendships
     * @throws RepositoryException if querying fails
     */
    @Override
    public Collection<Friendship> findAll() {

        List<Friendship> friendships = new ArrayList<>();

        String sql =
                "SELECT id1, id2, friends_from " +
                        "FROM friendships " +
                        "ORDER BY id1, id2";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                friendships.add(mapFriendship(rs));
            }

            return friendships;

        } catch (SQLException e) {
            throw sqlError("find all friendships", e);
        }
    }

    /**
     * Updates the creation date of an existing friendship.
     *
     * @param entity the friendship with updated information
     * @return null if updated, or the entity if ID is missing
     * @throws IllegalArgumentException if entity is null
     * @throws RepositoryException if updating fails
     */
    @Override
    public Friendship update(Friendship entity) {

        if (entity == null) {
            throw new IllegalArgumentException(
                    "Friendship cannot be null."
            );
        }

        validator.validate(entity);

        String sql =
                "UPDATE friendships " +
                        "SET friends_from = ? " +
                        "WHERE id1 = ? AND id2 = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            entity.getFriendshipDate()
                    )
            );

            statement.setLong(2, entity.getUserId1());
            statement.setLong(3, entity.getUserId2());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return entity;
            }

            return null;

        } catch (SQLException e) {
            throw sqlError("update friendship", e);
        }
    }

    /**
     * Deletes a friendship and returns the deleted entity.
     *
     * @param id the composite friendship identifier
     * @return the deleted friendship
     * @throws IllegalArgumentException if ID is invalid
     * @throws EntityNotFoundException if friendship is missing
     * @throws RepositoryException if deleting fails
     */
    @Override
    public Friendship delete(Tuple<Long, Long> id) {

        Tuple<Long, Long> normalizedId = normalizeId(id);

        String sql =
                "DELETE FROM friendships " +
                        "WHERE id1 = ? AND id2 = ? " +
                        "RETURNING id1, id2, friends_from";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, normalizedId.getFirst());
            statement.setLong(2, normalizedId.getSecond());

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapFriendship(rs);
                }

                throw new EntityNotFoundException(
                        "Prietenia dintre utilizatorii "
                                + normalizedId.getFirst()
                                + " si "
                                + normalizedId.getSecond()
                                + " nu exista."
                );
            }

        } catch (SQLException e) {
            throw sqlError("delete friendship", e);
        }
    }
}
