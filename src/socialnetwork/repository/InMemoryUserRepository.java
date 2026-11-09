package socialnetwork.repository;

import socialnetwork.domain.User;
import socialnetwork.exceptions.DuplicateEntityException;
import socialnetwork.exceptions.EntityNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementeaza persistenta utilizatorilor in memorie.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public void save(User user) {

        if (users.containsKey(user.getId())) {
            throw new DuplicateEntityException(
                    "Utilizatorul cu ID-ul " +
                            user.getId() + " exista deja."
            );
        }

        users.put(user.getId(), user);
    }

    @Override
    public Optional<User> findOne(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> findAll() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public void delete(Long id) {

        if (users.remove(id) == null) {
            throw new EntityNotFoundException(
                    "Utilizatorul cu ID-ul " +
                            id + " nu exista."
            );
        }
    }
}