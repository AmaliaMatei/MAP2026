
package socialnetwork.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an undirected friendship between two users.
 * Stores the creation date of the friendship.
 */
public class Friendship
        implements Entity<Tuple<Long, Long>> {

    private final Tuple<Long, Long> id;
    private LocalDateTime friendshipDate;

    /**
     * Creates a friendship with a given date.
     * User identifiers are normalized.
     *
     * @param userId1 the first user identifier
     * @param userId2 the second user identifier
     * @param friendshipDate the creation date
     * @throws IllegalArgumentException if an ID or
     *         the friendship date is null
     */
    public Friendship(Long userId1, Long userId2,
                      LocalDateTime friendshipDate) {

        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException(
                    "User IDs cannot be null."
            );
        }

        if (friendshipDate == null) {
            throw new IllegalArgumentException(
                    "Friendship date cannot be null."
            );
        }

        if (userId1 <= userId2) {
            this.id = new Tuple<>(userId1, userId2);
        } else {
            this.id = new Tuple<>(userId2, userId1);
        }

        this.friendshipDate = friendshipDate;
    }

    /**
     * Creates a friendship using the current date.
     *
     * @param userId1 the first user identifier
     * @param userId2 the second user identifier
     */
    public Friendship(Long userId1, Long userId2) {
        this(userId1, userId2, LocalDateTime.now());
    }

    /**
     * Returns the friendship identifier.
     *
     * @return the normalized pair of user IDs
     */
    @Override
    public Tuple<Long, Long> getId() {
        return id;
    }

    public Long getUserId1() {
        return id.getFirst();
    }

    public Long getUserId2() {
        return id.getSecond();
    }

    public LocalDateTime getFriendshipDate() {
        return friendshipDate;
    }

    public void setFriendshipDate(LocalDateTime friendshipDate) {

        if (friendshipDate == null) {
            throw new IllegalArgumentException(
                    "Friendship date cannot be null."
            );
        }

        this.friendshipDate = friendshipDate;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Friendship)) {
            return false;
        }

        Friendship other = (Friendship) obj;

        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Friendship{" +
                getUserId1() + " <-> " + getUserId2() +
                ", date=" + friendshipDate +
                '}';
    }
}
