
package socialnetwork.service;

import socialnetwork.domain.User;
import socialnetwork.domain.Friendship;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Reusable search criteria composed
 * with Predicate operations.
 */
public final class CriteriiUtilizator {

    private CriteriiUtilizator() {
    }

    /**
     * Creates a last-name search criterion.
     *
     * @param name last name to match
     * @return matching predicate
     */
    public static Predicate<User> nume(String name) {
        return user ->
                user.getLastName() != null &&
                        user.getLastName().equalsIgnoreCase(name.trim());
    }

    /**
     * Creates a city search criterion.
     *
     * @param city city to match
     * @return matching predicate
     */
    public static Predicate<User> oras(String city) {
        return user ->
                user.getCity() != null &&
                        user.getCity().equalsIgnoreCase(city.trim());
    }

    /**
     * Selects users meeting a minimum age.
     *
     * @param minimum minimum age, inclusive
     * @return matching predicate
     */
    public static Predicate<User> varstaMinima(int minimum) {
        return user ->
                user.getAge() != null &&
                        user.getAge() >= minimum;
    }

    /**
     * Selects users without friendships.
     *
     * @param friendships available friendships
     * @return predicate selecting users without friends
     */
    public static Predicate<User> faraPrieteni(
            Collection<Friendship> friendships) {

        Objects.requireNonNull(friendships, "friendships");

        return user -> friendships.stream().noneMatch(
                friendship ->
                        friendship.getUserId1().equals(user.getId()) ||
                                friendship.getUserId2().equals(user.getId())
        );
    }
}
