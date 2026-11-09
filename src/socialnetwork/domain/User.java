
package socialnetwork.domain;

import java.util.Objects;

/**
 * Represents a user of the social network.
 * Stores personal information and a unique internal ID.
 */
public class User implements Entity<Long> {

    private final Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
    private String city;

    /**
     * Creates a user with personal information.
     *
     * @param id the internal identifier
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address
     * @param age the age
     * @param city the city
     */
    public User(Long id, String firstName, String lastName,
                String email, Integer age, String city) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
        this.city = city;
    }

    /**
     * Creates a user with basic information.
     *
     * @param id the internal identifier
     * @param firstName the first name
     * @param lastName the last name
     */
    public User(Long id, String firstName, String lastName) {
        this(id, firstName, lastName, null, null, null);
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        User other = (User) obj;

        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return getFullName()
                + " | email: " + email
                + " | varsta: " + age
                + " | oras: " + city;
    }
}
