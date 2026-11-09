
package socialnetwork.service;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.repository.Repository;
import socialnetwork.validation.Validator;

import java.util.Collection;
import java.util.Objects;

/**
 * Manages friendships between existing users.
 */
public class FriendshipService {

    private final Repository<Tuple<Long, Long>, Friendship> repository;
    private final UserService users;
    private final Validator<Friendship> validator;

    /**
     * Creates the friendship service.
     *
     * @param repository the friendship repository
     * @param users the user service
     * @param validator the friendship validator
     */
    public FriendshipService(
            Repository<Tuple<Long, Long>, Friendship> repository,
            UserService users,
            Validator<Friendship> validator) {

        this.repository = Objects.requireNonNull(repository);
        this.users = Objects.requireNonNull(users);
        this.validator = Objects.requireNonNull(validator);
    }

    /**
     * Creates a friendship between two users.
     *
     * @param first the first user
     * @param second the second user
     * @throws IllegalArgumentException if a user is invalid
     */
    public void addFriendship(User first, User second) {

        Objects.requireNonNull(first, "first user");
        Objects.requireNonNull(second, "second user");

        users.getUserOrThrow(first.getId());
        users.getUserOrThrow(second.getId());

        Friendship friendship = new Friendship(
                first.getId(),
                second.getId()
        );

        validator.validate(friendship);
        repository.save(friendship);
    }

    /**
     * Removes an existing friendship.
     *
     * @param first the first user
     * @param second the second user
     * @throws IllegalArgumentException if a user is invalid
     */
    public void removeFriendship(User first, User second) {

        Objects.requireNonNull(first, "first user");
        Objects.requireNonNull(second, "second user");

        users.getUserOrThrow(first.getId());
        users.getUserOrThrow(second.getId());

        Friendship friendship = new Friendship(
                first.getId(),
                second.getId()
        );

        validator.validate(friendship);
        repository.delete(friendship.getId());
    }

    /**
     * Returns all friendships.
     *
     * @return the friendship collection
     */
    public Collection<Friendship> getAllFriendships() {
        return repository.findAll();
    }
}
