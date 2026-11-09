package socialnetwork.repository;

import socialnetwork.domain.Friendship;

import java.util.Collection;

/**
 * Defineste operatiile de persistenta pentru prietenii.
 */
public interface FriendshipRepository {

    /**
     * Salveaza o relatie de prietenie.
     */
    void save(Friendship friendship);

    /**
     * Sterge o relatie de prietenie.
     */
    void delete(Friendship friendship);

    /**
     * Returneaza toate prieteniile.
     */
    Collection<Friendship> findAll();

    /**
     * Verifica existenta unei prietenii.
     */
    boolean exists(Friendship friendship);

    /**
     * Sterge toate prieteniile unui utilizator.
     */
    void deleteAllForUser(Long userId);
}