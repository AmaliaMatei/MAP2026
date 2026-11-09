
package socialnetwork.ui;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;

import socialnetwork.service.UserService;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.CommunityService;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides an interactive console interface
 * for the social network application.
 */
public class ConsoleUI {

    private final UserService users;
    private final FriendshipService friendships;
    private final CommunityService communities;

    private final Scanner scanner;
    private final Map<String, Runnable> actions;

    /**
     * Creates the console interface.
     *
     * @param users the user service
     * @param friendships the friendship service
     * @param communities the community service
     */
    public ConsoleUI(
            UserService users,
            FriendshipService friendships,
            CommunityService communities) {

        this.users = Objects.requireNonNull(users);
        this.friendships =
                Objects.requireNonNull(friendships);
        this.communities =
                Objects.requireNonNull(communities);

        this.scanner = new Scanner(System.in);
        this.actions = new HashMap<>();

        configureActions();
    }

    /**
     * Registers menu operations using method references.
     */
    private void configureActions() {

        actions.put("1", this::addUser);
        actions.put("2", this::removeUser);
        actions.put("3", this::showUsers);

        actions.put("4", this::addFriendship);
        actions.put("5", this::removeFriendship);
        actions.put("6", this::showFriendships);

        actions.put("7", () ->
                System.out.println(
                        "Numar comunitati: "
                                + communities.getNumberOfCommunities()
                )
        );

        actions.put(
                "8",
                this::showMostSociableCommunity
        );
    }

    /**
     * Reads a line from the console.
     *
     * @param message displayed prompt
     * @return the trimmed input
     */
    private String read(String message) {

        System.out.print(message);
        return scanner.nextLine().trim();
    }

    /**
     * Chooses an existing user by name and,
     * when necessary, by email.
     *
     * @param message displayed message
     * @return the selected user
     */
    private User chooseUser(String message) {

        System.out.println(message);

        String firstName = read("Prenume: ");
        String lastName = read("Nume: ");

        String email = null;

        if (users.countByName(firstName, lastName) > 1) {
            email = read(
                    "Exista mai multe persoane. Email: "
            );
        }

        return users.findByName(
                firstName,
                lastName,
                email
        );
    }

    /**
     * Displays the available menu options.
     */
    private void showMenu() {

        Stream.of(
                "",
                "RETEA DE SOCIALIZARE",
                "1. Adauga utilizator",
                "2. Sterge utilizator",
                "3. Afiseaza utilizatori",
                "4. Adauga prietenie",
                "5. Sterge prietenie",
                "6. Afiseaza prietenii",
                "7. Numar comunitati",
                "8. Cea mai sociabila comunitate",
                "0. Iesire"
        ).forEach(System.out::println);
    }

    /**
     * Adds a new user.
     */
    private void addUser() {

        String firstName = read("Prenume: ");
        String lastName = read("Nume: ");
        String email = read("Email: ");

        int age = Integer.parseInt(
                read("Varsta: ")
        );

        String city = read("Oras: ");

        users.addUser(
                firstName,
                lastName,
                email,
                age,
                city
        );

        System.out.println(
                "Utilizator adaugat!"
        );
    }

    /**
     * Deletes an existing user.
     */
    private void removeUser() {

        User user = chooseUser(
                "Persoana de sters:"
        );

        users.removeUser(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );

        System.out.println(
                "Utilizator sters!"
        );
    }

    /**
     * Displays users sorted by identifier.
     */
    private void showUsers() {

        Collection<User> allUsers =
                users.getAllUsers();

        if (allUsers.isEmpty()) {

            System.out.println(
                    "Nu exista utilizatori."
            );
            return;
        }

        allUsers.stream()
                .sorted(
                        Comparator.comparing(User::getId)
                )
                .forEach(System.out::println);
    }

    /**
     * Creates a friendship.
     */
    private void addFriendship() {

        User first = chooseUser(
                "Prima persoana:"
        );

        User second = chooseUser(
                "A doua persoana:"
        );

        friendships.addFriendship(
                first,
                second
        );

        System.out.println(
                "Prietenie adaugata!"
        );
    }

    /**
     * Removes a friendship.
     */
    private void removeFriendship() {

        User first = chooseUser(
                "Prima persoana:"
        );

        User second = chooseUser(
                "A doua persoana:"
        );

        friendships.removeFriendship(
                first,
                second
        );

        System.out.println(
                "Prietenie stearsa!"
        );
    }

    /**
     * Displays all friendships.
     */
    private void showFriendships() {

        Collection<Friendship> all =
                friendships.getAllFriendships();

        if (all.isEmpty()) {

            System.out.println(
                    "Nu exista prietenii."
            );
            return;
        }

        all.stream()
                .sorted(
                        Comparator.comparing(
                                Friendship::getUserId1
                        ).thenComparing(
                                Friendship::getUserId2
                        )
                )
                .map(friendship -> {

                    User first = users.getUserOrThrow(
                            friendship.getUserId1()
                    );

                    User second = users.getUserOrThrow(
                            friendship.getUserId2()
                    );

                    return first.getFullName()
                            + " <-> "
                            + second.getFullName()
                            + " | Data: "
                            + friendship.getFriendshipDate();
                })
                .forEach(System.out::println);
    }

    /**
     * Displays the most sociable community.
     */
    private void showMostSociableCommunity() {

        Set<Long> ids =
                communities.getMostSociableCommunity();

        if (ids.isEmpty()) {

            System.out.println(
                    "Reteaua este goala."
            );
            return;
        }

        System.out.println(
                "Cea mai sociabila comunitate:"
        );

        ids.stream()
                .sorted()
                .map(users::getUserOrThrow)
                .forEach(System.out::println);

        System.out.println(
                "Cel mai lung drum: "
                        + communities
                        .getMostSociableCommunityPathLength()
        );
    }

    /**
     * Displays the community of a selected user.
     */
    private void showUserCommunity() {

        User user = chooseUser(
                "Cautare comunitate:"
        );

        communities.getCommunityOfUser(
                        user.getId()
                )
                .stream()
                .sorted()
                .map(users::getUserOrThrow)
                .forEach(System.out::println);
    }

    /**
     * Reads the next menu option.
     *
     * @return selected option, or zero on input end
     */
    private String readOption() {

        showMenu();

        if (!scanner.hasNextLine()) {
            return "0";
        }

        return read("Optiune: ");
    }

    /**
     * Executes a menu option.
     *
     * @param option the selected option
     * @return true when the application must stop
     */
    private boolean executeOption(String option) {

        if ("0".equals(option)) {

            System.out.println(
                    "La revedere!"
            );
            return true;
        }

        Runnable action = actions.get(option);

        if (action == null) {

            System.out.println(
                    "Optiune invalida."
            );
            return false;
        }

        try {

            action.run();

        } catch (RuntimeException e) {

            System.out.println(
                    "Eroare: " + e.getMessage()
            );
        }

        return false;
    }

    /**
     * Starts the interactive application.
     * A generated stream replaces the classic
     * while loop and stops at option zero.
     */
    public void run() {

        Stream.generate(this::readOption)
                .anyMatch(this::executeOption);
    }
}
