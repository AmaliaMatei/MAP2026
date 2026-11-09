package socialnetwork.validation;

import socialnetwork.domain.Friendship;
import socialnetwork.exceptions.ValidationException;

/**
 * Valideaza o relatie de prietenie.
 */
public class FriendshipValidator implements Validator<Friendship> {

    @Override
    public void validate(Friendship friendship) {

        if (friendship == null) {
            throw new ValidationException(
                    "Prietenia nu poate fi null."
            );
        }

        Long id1 = friendship.getUserId1();
        Long id2 = friendship.getUserId2();

        if (id1 == null || id2 == null) {
            throw new ValidationException(
                    "ID-urile utilizatorilor nu pot fi null."
            );
        }

        if (id1 <= 0 || id2 <= 0) {
            throw new ValidationException(
                    "ID-urile trebuie sa fie pozitive."
            );
        }

        if (id1.equals(id2)) {
            throw new ValidationException(
                    "Un utilizator nu poate fi prieten cu el insusi."
            );
        }
    }
}