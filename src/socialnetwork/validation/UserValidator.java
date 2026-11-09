
package socialnetwork.validation;

import socialnetwork.domain.User;
import socialnetwork.exceptions.ValidationException;

/**
 * Validates the data of a user.
 * Implements the Strategy Pattern.
 */
public class UserValidator implements Validator<User> {

    /**
     * Checks whether the user data is valid.
     *
     * @param user user to validate
     * @throws ValidationException if data is invalid
     */
    @Override
    public void validate(User user) {

        if (user == null) {
            throw new ValidationException(
                    "Utilizatorul nu poate fi null."
            );
        }

        StringBuilder errors = new StringBuilder();

        if (user.getId() == null || user.getId() <= 0) {
            errors.append("ID invalid.\n");
        }

        if (user.getFirstName() == null ||
                user.getFirstName().trim().isEmpty()) {
            errors.append("Prenume obligatoriu.\n");
        }

        if (user.getLastName() == null ||
                user.getLastName().trim().isEmpty()) {
            errors.append("Nume obligatoriu.\n");
        }

        if (user.getEmail() == null ||
                !user.getEmail().matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                )) {
            errors.append("Email invalid sau lipsa.\n");
        }

        if (user.getAge() == null ||
                user.getAge() < 1 ||
                user.getAge() > 120) {
            errors.append(
                    "Varsta trebuie sa fie intre 1 si 120.\n"
            );
        }

        if (user.getCity() == null ||
                user.getCity().trim().isEmpty()) {
            errors.append("Oras obligatoriu.\n");
        }

        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}
