/**
 * RestController that handles authentication-related requests.
 *
 * This controller provides:
 * - User registration for both Customers and Admins
 * - Secure login functionality with session establishment
 * - Session invalidation (logout)
 * - Real-time session state retrieval for Frontend synchronization
 */

package yoni.restaurantreservationsystem.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import yoni.restaurantreservationsystem.entities.*;
import yoni.restaurantreservationsystem.responses.AuthResponse;
import yoni.restaurantreservationsystem.services.*;
import yoni.restaurantreservationsystem.requests.*;

@RestController
public class AuthController {

    private final UserService userService;

    //Constructor
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user and starts a session for them.
     *
     * The method receives registration details, validates that a user type was selected,
     * creates the user through the UserService, stores the user details in the session,
     * and returns an authentication response.
     *
     * @param request the registration request containing username, password, and user type
     * @param session the HTTP session used to store logged-in user data
     * @return ResponseEntity containing AuthResponse on success, or an error message on failure
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            if (request.getUserType() == null) {
                throw new IllegalArgumentException("User type is mandatory. Please select Admin or Customer.");
            }
            User newUser = userService.register(request.getUsername(), request.getPassword(),
                    request.getUserType());

            setupSession(session, newUser);

            AuthResponse response = new AuthResponse(newUser.getUserId(), newUser.getUsername(),
                    newUser.getUserType().name());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Registers a new user and starts a session for them.
     *
     * Stores the user details in the session,and returns an authentication response.
     *
     * @param request a LoginRequest object containing the user's credentials
     * @param session the current HTTP session to store authentication attributes
     * @return a ResponseEntity with an AuthResponse on success, or UNAUTHORIZED status on failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.login(request.getUsername(), request.getPassword());

            setupSession(session, user);

            AuthResponse response = new AuthResponse(user.getUserId(), user.getUsername(),
                    user.getUserType().name());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /**
     * Terminates the current user session.
     *
     * @param session the HTTP session to be invalidated
     * @return a ResponseEntity with a success message confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Retrieves information about the currently logged-in user based on the session.
     *
     * Used by the Frontend to check if a user is still authenticated after a page refresh.
     *
     * @param session the current HTTP session containing user attributes
     * @return a ResponseEntity with AuthResponse data if logged in, or UNAUTHORIZED if not
     */
    @GetMapping("/session")
    public ResponseEntity<?> getCurrentSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String userType = (String) session.getAttribute("userType");

        if (userId == null || username == null || userType == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not logged in.");
        }

        AuthResponse response = new AuthResponse(userId, username, userType);

        return ResponseEntity.ok(response);
    }

    /**
     * Stores authenticated user details in the HTTP session.
     *
     * @param session the HTTP session where the user data should be stored
     * @param user the authenticated or newly registered user whose data should be saved
     */
    private void setupSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userType", user.getUserType().name());
    }
}
