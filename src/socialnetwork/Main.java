
package socialnetwork;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;

import socialnetwork.repository.Repository;

import socialnetwork.factory.RepositoryFactory;
import socialnetwork.factory.RepositoryType;

import socialnetwork.validation.UserValidator;
import socialnetwork.validation.Validator;
import socialnetwork.validation.ExistingUsersFriendshipValidator;

import socialnetwork.service.UserService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.CommunityService;

import socialnetwork.ui.ConsoleUI;

import socialnetwork.exceptions.ValidationException;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Configures application dependencies and starts
 * the social network application.
 *
 * Supports MEMORY, FILE and DATABASE persistence.
 */
public class Main {

    /**
     * Starts the social network application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        try {

            // Create the repository factory.
            RepositoryFactory factory =
                    new RepositoryFactory();

            // Default user validation.
            Validator<User> userValidator =
                    new UserValidator();

            // Additional validation for FILE storage only.
            // Semicolons and line breaks may corrupt
            // the text file format.
            if (factory.getRepositoryType()
                    == RepositoryType.FILE) {

                Validator<User> additionalRule = user -> {

                    Objects.requireNonNull(user);

                    boolean invalid = Stream.of(
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getEmail(),
                                    user.getCity()
                            )
                            .filter(Objects::nonNull)
                            .anyMatch(field ->
                                    field.contains(";")
                                            || field.contains("\t")
                                            || field.contains("\n")
                                            || field.contains("\r")
                            );

                    if (invalid) {
                        throw new ValidationException(
                                "Campurile utilizatorului nu pot "
                                        + "contine ;, taburi "
                                        + "sau randuri noi."
                        );
                    }
                };

                userValidator =
                        userValidator.andThen(additionalRule);
            }

            // Create the user repository.
            Repository<Long, User> userRepository =
                    factory.createUtilizatorRepository(
                            userValidator
                    );

            // Create the friendship validator.
            // It verifies that both users exist.
            Validator<Friendship> friendshipValidator =
                    new ExistingUsersFriendshipValidator(
                            userRepository
                    );

            // Create the friendship repository.
            Repository<Tuple<Long, Long>, Friendship>
                    friendshipRepository =
                    factory.createPrietenieRepository(
                            friendshipValidator
                    );

            // Create the user service.
            // RepositoryType determines ID generation.
            UserService users = new UserService(
                    userRepository,
                    friendshipRepository,
                    userValidator,
                    factory.getRepositoryType()
            );

            // Create the friendship service.
            FriendshipService friendships =
                    new FriendshipService(
                            friendshipRepository,
                            users,
                            friendshipValidator
                    );

            // Create the community service.
            CommunityService communities =
                    new CommunityService(
                            userRepository,
                            friendshipRepository
                    );

            // Create and start the console interface.
            ConsoleUI ui = new ConsoleUI(
                    users,
                    friendships,
                    communities
            );

            System.out.println(
                    "Aplicatia foloseste repository: "
                            + factory.getRepositoryType()
            );

            ui.run();

        } catch (RuntimeException e) {

            System.err.println(
                    "Aplicatia nu poate porni: "
                            + e.getMessage()
            );
        }
    }
}
