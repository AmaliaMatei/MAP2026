
package socialnetwork.domain;

import java.util.Objects;

/**
 * Represents an immutable pair of values.
 *
 * @param <E1> the type of the first value
 * @param <E2> the type of the second value
 */
public class Tuple<E1, E2> {

    private final E1 first;
    private final E2 second;

    /**
     * Creates a pair of values.
     *
     * @param first the first value
     * @param second the second value
     */
    public Tuple(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value.
     *
     * @return the first value
     */
    public E1 getFirst() {
        return first;
    }

    /**
     * Returns the second value.
     *
     * @return the second value
     */
    public E2 getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Tuple<?, ?>)) {
            return false;
        }

        Tuple<?, ?> other = (Tuple<?, ?>) obj;

        return Objects.equals(first, other.first)
                && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
