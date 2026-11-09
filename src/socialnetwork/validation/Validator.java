
package socialnetwork.validation;

import socialnetwork.exceptions.ValidationException;
import java.util.Objects;

/**
 * Functional contract for entity validation.
 *
 * @param <T> validated entity type
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates an entity.
     *
     * @param entity entity to validate
     * @throws ValidationException if invalid
     */
    void validate(T entity);

    /**
     * Composes this validator with another,
     * in execution order.
     *
     * @param other validator executed after this validator
     * @return a composed validator
     * @throws NullPointerException if other is null
     */
    default Validator<T> andThen(Validator<T> other) {
        Objects.requireNonNull(other, "other validator");

        return entity -> {
            validate(entity);
            other.validate(entity);
        };
    }
}
