/**
 * Repository interface for accessing and managing User entities.
 *
 * This interface provides:
 * - Identity management and account lookup
 * - Uniqueness validation for usernames during registration
 * - Support for authentication processes by fetching credentials via username
 */
package yoni.restaurantreservationsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import yoni.restaurantreservationsystem.entities.*;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Checks if a specific username is already registered in the system.
     *
     * This method is primarily used during the sign-up process to prevent
     * duplicate accounts and ensure each username remains a unique identifier.
     *
     * @param username the login name to check for existence
     * @return true if the username is already taken, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Finds a user by their username.
     *
     * This method is typically used during login to retrieve
     * user details for authentication.
     *
     * @param username the username of the user to find
     * @return an Optional containing the user if found, otherwise empty
     */
    Optional<User> findByUsername(String username);
}

















