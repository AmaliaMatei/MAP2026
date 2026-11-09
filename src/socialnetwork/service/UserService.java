
package socialnetwork.service;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.repository.Repository;
import socialnetwork.validation.Validator;
import socialnetwork.exceptions.DuplicateEntityException;
import socialnetwork.exceptions.EntityNotFoundException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages user operations in the social network.
 */
public class UserService {

    private final Repository<Long, User> users;
    private final Repository<Tuple<Long, Long>, Friendship> friendships;
    private final Validator<User> validator;

    /**
     * Creates the user service.
     *
     * @param users the user repository
     * @param friendships the friendship repository
     * @param validator the user validator
     */
    public UserService(
            Repository<Long, User> users,
            Repository<Tuple<Long, Long>, Friendship> friendships,
            Validator<User> validator) {

        this.users = Objects.requireNonNull(users);
        this.friendships = Objects.requireNonNull(friendships);
        this.validator = Objects.requireNonNull(validator);
    }

    /**
     * Generates the next available user identifier.
     *
     * @return the next identifier
     */
    public Long nextId() {
        return users.findAll()
                .stream()
                .map(User::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    /**
     * Adds a new user to the social network.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address
     * @param age the user's age
     * @param city the user's city
     * @return the created user
     * @throws DuplicateEntityException if email already exists
     */
    public User addUser(
            String firstName,
            String lastName,
            String email,
            Integer age,
            String city) {

        User user = new User(
                nextId(),
                firstName,
                lastName,
                email,
                age,
                city
        );

        validator.validate(user);
        validateFileFields(user);

        boolean emailExists = users.findAll()
                .stream()
                .anyMatch(existing ->
                        existing.getEmail() != null
                                && existing.getEmail()
                                .equalsIgnoreCase(email.trim())
                );

        if (emailExists) {
            throw new DuplicateEntityException(
                    "Exista deja un utilizator cu acest email."
            );
        }

        users.save(user);
        return user;
    }

    /**
     * Rejects characters that would break the text file format.
     *
     * @param user the user to check
     * @throws IllegalArgumentException if a field is unsafe
     */
    private void validateFileFields(User user) {

        String[] fields = {
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getCity()
        };

        boolean invalid = Arrays.stream(fields)
                .filter(Objects::nonNull)
                .anyMatch(field ->
                        field.contains(";")
                                || field.contains("\n")
                                || field.contains("\r")
                );

        if (invalid) {
            throw new IllegalArgumentException(
                    "Campurile nu pot contine ; sau randuri noi."
            );
        }
    }

    /**
     * Finds a user by ID or throws an exception.
     *
     * @param id the user identifier
     * @return the existing user
     * @throws EntityNotFoundException if user does not exist
     */
    public User getUserOrThrow(Long id) {
        return users.findOne(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Utilizatorul nu exista."
                        )
                );
    }

    /**
     * Returns all stored users.
     *
     * @return the user collection
     */
    public Collection<User> getAllUsers() {
        return users.findAll();
    }

    /**
     * Counts users with a given full name.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @return the number of matching users
     */
    public long countByName(
            String firstName,
            String lastName) {

        return users.findAll()
                .stream()
                .filter(user ->
                        user.getFirstName()
                                .equalsIgnoreCase(firstName.trim())
                                && user.getLastName()
                                .equalsIgnoreCase(lastName.trim())
                )
                .count();
    }

    /**
     * Finds a user by name and optional email.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email optional email for disambiguation
     * @return the matching user
     * @throws EntityNotFoundException if no match exists
     * @throws IllegalArgumentException if the name is ambiguous
     */
    public User findByName(
            String firstName,
            String lastName,
            String email) {

        List<User> matches = users.findAll()
                .stream()
                .filter(user ->
                        user.getFirstName()
                                .equalsIgnoreCase(firstName.trim())
                                && user.getLastName()
                                .equalsIgnoreCase(lastName.trim())
                )
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            throw new EntityNotFoundException(
                    "Nu exista persoana "
                            + firstName + " " + lastName
            );
        }

        if (email != null && !email.trim().isEmpty()) {
            return matches.stream()
                    .filter(user ->
                            user.getEmail() != null
                                    && user.getEmail()
                                    .equalsIgnoreCase(email.trim())
                    )
                    .findFirst()
                    .orElseThrow(() ->
                            new EntityNotFoundException(
                                    "Emailul nu corespunde persoanei."
                            )
                    );
        }

        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                    "Exista mai multe persoane cu acest nume. "
                            + "Introdu si emailul."
            );
        }

        return matches.get(0);
    }

    /**
     * Returns the friends of an existing user.
     *
     * @param id the user identifier
     * @return distinct friends sorted by name and ID
     * @throws EntityNotFoundException if user is missing
     */
    public List<User> getFriendsOfUser(Long id) {

        getUserOrThrow(id);

        return friendships.findAll()
                .stream()
                .filter(friendship ->
                        friendship.getUserId1().equals(id)
                                || friendship.getUserId2().equals(id)
                )
                .flatMap(friendship ->
                        Stream.of(
                                friendship.getUserId1(),
                                friendship.getUserId2()
                        )
                )
                .filter(otherId -> !otherId.equals(id))
                .distinct()
                .map(this::getUserOrThrow)
                .sorted(
                        Comparator.comparing(User::getLastName)
                                .thenComparing(User::getFirstName)
                                .thenComparing(User::getId)
                )
                .collect(Collectors.toList());
    }

    /**
     * Removes a user and all associated friendships.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email optional email for disambiguation
     * @throws EntityNotFoundException if user is missing
     */
    public void removeUser(
            String firstName,
            String lastName,
            String email) {

        User user = findByName(
                firstName,
                lastName,
                email
        );

        List<Tuple<Long, Long>> friendshipIds =
                friendships.findAll()
                        .stream()
                        .filter(friendship ->
                                friendship.getUserId1()
                                        .equals(user.getId())
                                        || friendship.getUserId2()
                                        .equals(user.getId())
                        )
                        .map(Friendship::getId)
                        .collect(Collectors.toList());

        friendshipIds.forEach(friendships::delete);

        users.delete(user.getId());
    }
}
