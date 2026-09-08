/**
 * Data Transfer Object representing a user's login credentials.
 *
 * This request is used during the authentication process to:
 * - Capture the username and password provided by the user
 * - Facilitate the transition of raw JSON data into the authentication logic
 */
package yoni.restaurantreservationsystem.requests;

public class LoginRequest {
    private String username;
    private String password;

    //Constructors

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    //Getters and Setters.
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}