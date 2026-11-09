
package socialnetwork.repository;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.validation.Validator;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * File repository for friendships.
 * Converts friendship entities to and from text lines.
 */
public class PrietenieFileRepository
        extends AbstractFileRepository<
        Tuple<Long, Long>, Friendship> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd'T'HH:mm"
            );

    /**
     * Creates a friendship repository and loads existing data.
     *
     * @param filePath the friendships file path
     * @param validator the friendship validator
     */
    public PrietenieFileRepository(
            Path filePath,
            Validator<Friendship> validator) {

        super(filePath, validator);

        loadData();
    }

    /**
     * Extracts a friendship from a text line.
     *
     * @param line the input line
     * @return the extracted friendship
     * @throws IllegalArgumentException if the format is invalid
     */
    @Override
    protected Friendship extractEntity(String line) {

        String[] fields = line.split(";", -1);

        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "Expected 3 fields, found "
                            + fields.length
            );
        }

        try {

            Long userId1 = Long.parseLong(
                    fields[0].trim()
            );

            Long userId2 = Long.parseLong(
                    fields[1].trim()
            );

            LocalDateTime date = LocalDateTime.parse(
                    fields[2].trim(),
                    FORMATTER
            );

            return new Friendship(
                    userId1,
                    userId2,
                    date
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid user ID: " + e.getMessage(),
                    e
            );

        } catch (DateTimeParseException e) {

            throw new IllegalArgumentException(
                    "Invalid friendship date: "
                            + e.getParsedString(),
                    e
            );
        }
    }

    /**
     * Converts a friendship into a text line.
     *
     * @param friendship the friendship to convert
     * @return the text representation
     */
    @Override
    protected String createEntityAsString(
            Friendship friendship) {

        return friendship.getUserId1() + ";"
                + friendship.getUserId2() + ";"
                + friendship.getFriendshipDate().format(
                FORMATTER
        );
    }
}
