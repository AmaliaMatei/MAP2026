
package socialnetwork.validation;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.User;
import socialnetwork.repository.Repository;
import socialnetwork.exceptions.ValidationException;

/**
 * Validates a friendship and checks that both users exist.
 */
public class ExistingUsersFriendshipValidator
        implements Validator<Friendship> {

    private final Repository<Long, User> users;
    private final FriendshipValidator basicValidator;

    /**
     * Creates a validator using the user repository.
     *
     * @param users the repository containing valid users
     */
    public ExistingUsersFriendshipValidator(
            Repository<Long, User> users) {

        if (users == null) {
            throw new IllegalArgumentException(
                    "User repository cannot be null."
            );
        }

        this.users = users;
        this.basicValidator = new FriendshipValidator();
    }

    /**
     * Validates the friendship and its referenced users.
     *
     * @param friendship the friendship to validate
     * @throws ValidationException if the friendship is invalid
     */
    @Override
    public void validate(Friendship friendship) {

        basicValidator.validate(friendship);

        if (!users.findOne(friendship.getUserId1()).isPresent()) {
            throw new ValidationException(
                    "Primul utilizator nu exista."
            );
        }

        if (!users.findOne(friendship.getUserId2()).isPresent()) {
            throw new ValidationException(
                    "Al doilea utilizator nu exista."
            );
        }
    }
}
