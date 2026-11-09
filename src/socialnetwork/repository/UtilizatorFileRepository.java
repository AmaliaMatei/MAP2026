
package socialnetwork.repository;

import socialnetwork.domain.User;
import socialnetwork.validation.Validator;

import java.nio.file.Path;

/**
 * File repository for social network users.
 * Converts user entities to and from text lines.
 */
public class UtilizatorFileRepository
        extends AbstractFileRepository<Long, User> {

    /**
     * Creates a user file repository and loads existing data.
     *
     * @param filePath the users file path
     * @param validator the user validator
     */
    public UtilizatorFileRepository(
            Path filePath,
            Validator<User> validator) {

        super(filePath, validator);

        loadData();
    }

    /**
     * Creates a user from a semicolon-separated line.
     *
     * @param line the input line
     * @return the extracted user
     * @throws IllegalArgumentException if the format is invalid
     */
    @Override
    protected User extractEntity(String line) {

        String[] fields = line.split(";", -1);

        if (fields.length != 6) {
            throw new IllegalArgumentException(
                    "Expected 6 fields, found "
                            + fields.length
            );
        }

        try {

            Long id = Long.parseLong(fields[0].trim());

            String firstName = fields[1].trim();
            String lastName = fields[2].trim();
            String email = fields[3].trim();

            Integer age = Integer.parseInt(
                    fields[4].trim()
            );

            String city = fields[5].trim();

            return new User(
                    id,
                    firstName,
                    lastName,
                    email,
                    age,
                    city
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid numeric value: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Converts a user into a semicolon-separated line.
     *
     * @param user the user to convert
     * @return the text representation
     */
    @Override
    protected String createEntityAsString(User user) {

        return user.getId() + ";"
                + user.getFirstName() + ";"
                + user.getLastName() + ";"
                + user.getEmail() + ";"
                + user.getAge() + ";"
                + user.getCity();
    }
}
