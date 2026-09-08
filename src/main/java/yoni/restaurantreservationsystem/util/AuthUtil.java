/**
 * Utility class for authentication-related session operations.
 *
 * This class provides helper methods used by controllers to extract
 * authenticated user data from the current HTTP session.
 */
package yoni.restaurantreservationsystem.util;

import jakarta.servlet.http.HttpSession;

public class AuthUtil {
    /**
     * Retrieves the logged-in user's ID from the current session.
     *
     * @param session the current HTTP session that should contain the authenticated user's data
     * @return the ID of the logged-in user
     * @throws SecurityException if the user is not logged in or the session does not contain a user ID
     */
    public static Long getUserIdOrThrow(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new SecurityException("You must be logged in to perform this action.");
        }
        return userId;
    }
}
