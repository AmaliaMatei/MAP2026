
package socialnetwork.repository;

import socialnetwork.domain.User;
import socialnetwork.exceptions.EntityNotFoundException;
import socialnetwork.exceptions.RepositoryException;
import socialnetwork.validation.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL repository for social network users.
 * Uses JDBC and database-generated identifiers.
 */
public class UtilizatorDbRepository
        implements Repository<Long, User> {

    private final Validator<User> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Creates a database repository for users.
     *
     * @param validator the user validator
     * @param url the PostgreSQL JDBC URL
     * @param username the database username
     * @param password the database password
     * @throws IllegalArgumentException if configuration is invalid
     */
    public UtilizatorDbRepository(
            Validator<User> validator,
            String url,
            String username,
            String password) {

        if (validator == null ||
                url == null || url.trim().isEmpty() ||
                username == null || username.trim().isEmpty() ||
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
     * Converts a SQL row into a User object.
     *
     * @param rs the result set positioned on a row
     * @return the mapped user
     * @throws SQLException if reading the row fails
     */
    private User mapUser(ResultSet rs) throws SQLException {

        Long id = rs.getLong("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");

        int ageValue = rs.getInt("age");
        Integer age = rs.wasNull() ? null : ageValue;

        String city = rs.getString("city");

        return new User(
                id,
                firstName,
                lastName,
                email,
                age,
                city
        );
    }

    /**
     * Converts SQL errors into repository exceptions.
     *
     * @param operation the failed operation
     * @param e the original SQL exception
     * @return a repository exception with a specific message
     */
    private RepositoryException sqlError(
            String operation, SQLException e) {

        String state = e.getSQLState();

        if (state != null && state.startsWith("08")) {
            return new RepositoryException(
                    "Nu se poate realiza conexiunea la PostgreSQL. "
                            + "Verifica daca serverul este pornit.",
                    e
            );
        }

        if ("23505".equals(state)) {
            return new RepositoryException(
                    "Operatia nu poate fi efectuata: "
                            + "exista deja o inregistrare cu acea cheie.",
                    e
            );
        }

        if ("23503".equals(state)) {
            return new RepositoryException(
                    "Operatia incalca o legatura "
                            + "dintre tabelele bazei de date.",
                    e
            );
        }

        if ("23502".equals(state) ||
                "23514".equals(state)) {
            return new RepositoryException(
                    "Datele incalca o constrangere "
                            + "a bazei de date.",
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
     * Saves a validated user and assigns the generated ID.
     *
     * @param entity the user to save
     * @throws IllegalArgumentException if user is null
     *         or already has an identifier
     * @throws RepositoryException if inserting fails
     */
    @Override
    public void save(User entity) {

        if (entity == null) {
            throw new IllegalArgumentException(
                    "User cannot be null."
            );
        }

        if (entity.getId() != null) {
            throw new IllegalArgumentException(
                    "A new database user must have a null ID."
            );
        }

        validator.validate(entity);

        String sql =
                "INSERT INTO users " +
                        "(first_name, last_name, email, age, city) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getEmail());

            if (entity.getAge() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, entity.getAge());
            }

            statement.setString(5, entity.getCity());

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                throw new RepositoryException(
                        "Utilizatorul nu a fost salvat."
                );
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new RepositoryException(
                            "PostgreSQL nu a returnat ID-ul generat."
                    );
                }

                entity.setId(keys.getLong("id"));
            }

        } catch (SQLException e) {
            throw sqlError("save user", e);
        }
    }

    /**
     * Finds a user by its identifier.
     *
     * @param id the user identifier
     * @return an Optional containing the user if found
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if querying fails
     */
    @Override
    public Optional<User> findOne(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null."
            );
        }

        String sql =
                "SELECT id, first_name, last_name, " +
                        "email, age, city " +
                        "FROM users WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw sqlError("find user", e);
        }
    }

    /**
     * Returns all users stored in PostgreSQL.
     *
     * @return the collection of stored users
     * @throws RepositoryException if querying fails
     */
    @Override
    public Collection<User> findAll() {

        List<User> users = new ArrayList<>();

        String sql =
                "SELECT id, first_name, last_name, " +
                        "email, age, city " +
                        "FROM users ORDER BY id";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

            return users;

        } catch (SQLException e) {
            throw sqlError("find all users", e);
        }
    }

    /**
     * Updates a validated user in PostgreSQL.
     *
     * @param entity the user with updated information
     * @return null if updated, or the entity if ID is missing
     * @throws IllegalArgumentException if user or ID is null
     * @throws RepositoryException if updating fails
     */
    @Override
    public User update(User entity) {

        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "User and user ID cannot be null."
            );
        }

        validator.validate(entity);

        String sql =
                "UPDATE users SET " +
                        "first_name = ?, " +
                        "last_name = ?, " +
                        "email = ?, " +
                        "age = ?, " +
                        "city = ? " +
                        "WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setString(3, entity.getEmail());

            if (entity.getAge() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, entity.getAge());
            }

            statement.setString(5, entity.getCity());
            statement.setLong(6, entity.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return entity;
            }

            return null;

        } catch (SQLException e) {
            throw sqlError("update user", e);
        }
    }

    /**
     * Deletes a user and returns the deleted entity.
     *
     * @param id the identifier of the user to delete
     * @return the deleted user
     * @throws IllegalArgumentException if id is null
     * @throws EntityNotFoundException if user does not exist
     * @throws RepositoryException if deleting fails
     */
    @Override
    public User delete(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null."
            );
        }

        String sql =
                "DELETE FROM users WHERE id = ? " +
                        "RETURNING id, first_name, last_name, " +
                        "email, age, city";

        try (Connection connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }

                throw new EntityNotFoundException(
                        "Utilizatorul cu ID " + id
                                + " nu exista."
                );
            }

        } catch (SQLException e) {
            throw sqlError("delete user", e);
        }
    }
}
