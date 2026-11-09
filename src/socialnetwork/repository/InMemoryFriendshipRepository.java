package socialnetwork.repository;

import socialnetwork.domain.Friendship;
import socialnetwork.exceptions.DuplicateEntityException;
import socialnetwork.exceptions.EntityNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementeaza persistenta prieteniilor in memorie.
 */
public class InMemoryFriendshipRepository
        implements FriendshipRepository {

    private final Set<Friendship> friendships = new HashSet<>();

    @Override
    public void save(Friendship friendship) {

        if (!friendships.add(friendship)) {
            throw new DuplicateEntityException(
                    "Aceasta prietenie exista deja."
            );
        }
    }

    @Override
    public void delete(Friendship friendship) {

        if (!friendships.remove(friendship)) {
            throw new EntityNotFoundException(
                    "Aceasta prietenie nu exista."
            );
        }
    }

    @Override
    public Collection<Friendship> findAll() {
        return Collections.unmodifiableSet(friendships);
    }

    @Override
    public boolean exists(Friendship friendship) {
        return friendships.contains(friendship);
    }

    @Override
    public void deleteAllForUser(Long userId) {

        friendships.removeIf(friendship ->
                friendship.getUserId1().equals(userId) ||
                        friendship.getUserId2().equals(userId)
        );
    }
}