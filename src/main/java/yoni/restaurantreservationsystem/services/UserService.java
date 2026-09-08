/**
 * Service class for managing user-related operations and identity.
 *
 * This class provides functionality for:
 * - Registering new accounts with unique usernames
 * - Handling user authentication and login processes
 * - Retrieving user profile data for session management and authorization
 */
package yoni.restaurantreservationsystem.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import yoni.restaurantreservationsystem.entities.*;
import yoni.restaurantreservationsystem.repositories.UserRepository;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //Constructor
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     *
     * The method checks that the username does not already exist,
     * hashes the provided password using the configured PasswordEncoder,
     * creates a new User entity, and saves it in the database.
     *
     * @param username the username requested by the new user
     * @param password the raw password provided by the new user
     * @param type the type/role of the user, either CUSTOMER or ADMIN
     * @return the newly created user entity
     * @throws RuntimeException if the username already exists
     */
    @Transactional
    public User register(String username, String password, UserType type) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User newUser = new User(username, encodedPassword, type);

        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user by username and password.
     *
     * The method searches for the user by username and verifies that
     * the provided raw password matches the stored encoded password.
     *
     * @param username the username provided during login
     * @param password the raw password provided during login
     * @return the authenticated user entity
     * @throws RuntimeException if the username does not exist or the password is incorrect
     */
    public User login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return user;
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id the ID of the user to retrieve
     * @return the user entity with the given ID
     * @throws RuntimeException if the user is not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Retrieves a user by username.
     *
     * @param username the username of the user to retrieve
     * @return the user entity with the given username
     * @throws RuntimeException if the user is not found
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
