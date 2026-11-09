
package socialnetwork;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;

import socialnetwork.repository.Repository;
import socialnetwork.factory.RepositoryFactory;

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
 * Configures dependencies and starts the social network.
 */
public class Main {

    /**
     * Starts the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {

        try {

            RepositoryFactory factory =
                    new RepositoryFactory();

            // Existing validation is preserved.
            // The additional rule is defined as a lambda.
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
                                    + "contine ;, taburi sau randuri noi."
                    );
                }
            };

            // Composing validators using andThen.
            Validator<User> userValidator =
                    new UserValidator()
                            .andThen(additionalRule);

            // Load users before friendships.
            Repository<Long, User> userRepository =
                    factory.createUtilizatorRepository(
                            userValidator
                    );

            Validator<Friendship> friendshipValidator =
                    new ExistingUsersFriendshipValidator(
                            userRepository
                    );

            Repository<Tuple<Long, Long>, Friendship>
                    friendshipRepository =
                    factory.createPrietenieRepository(
                            friendshipValidator
                    );

            UserService users = new UserService(
                    userRepository,
                    friendshipRepository,
                    userValidator
            );

            FriendshipService friendships =
                    new FriendshipService(
                            friendshipRepository,
                            users,
                            friendshipValidator
                    );

            CommunityService communities =
                    new CommunityService(
                            userRepository,
                            friendshipRepository
                    );

            ConsoleUI ui = new ConsoleUI(
                    users,
                    friendships,
                    communities
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
