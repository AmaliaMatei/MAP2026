package socialnetwork.repository;

import socialnetwork.domain.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Defineste operatiile de persistenta pentru utilizatori.
 */
public interface UserRepository {

    /**
     * Salveaza un utilizator nou.
     *
     * @param user utilizatorul care trebuie salvat
     */
    void save(User user);

    /**
     * Cauta un utilizator dupa ID.
     *
     * @param id identificatorul utilizatorului
     * @return utilizatorul, daca exista
     */
    Optional<User> findOne(Long id);

    /**
     * Returneaza toti utilizatorii.
     */
    Collection<User> findAll();

    /**
     * Sterge utilizatorul cu ID-ul dat.
     */
    void delete(Long id);
}