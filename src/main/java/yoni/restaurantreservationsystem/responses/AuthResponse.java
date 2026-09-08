/**
 * Data Transfer Object sent back to the client upon successful authentication.
 * This response contains essential user identity information, including
 * the user ID and role (UserType), allowing the Frontend to manage sessions
 * and enforce role-based UI logic.
 */
package yoni.restaurantreservationsystem.responses;

public class AuthResponse {

    private Long userId;
    private String username;
    private String userType;

    public AuthResponse(Long userId, String username, String userType) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
