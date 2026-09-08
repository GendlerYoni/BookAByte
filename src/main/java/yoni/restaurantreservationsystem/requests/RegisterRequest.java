/**
 * Data Transfer Object representing the data required for new user registration.
 *
 * This request captures:
 * - Account identity (username and password)
 * - The specific role assigned to the user (CUSTOMER or ADMIN)
 *
 * It ensures all necessary fields are present before an account is created in the database.
 */
package yoni.restaurantreservationsystem.requests;

import yoni.restaurantreservationsystem.entities.UserType;

public class RegisterRequest {
    private String username;
    private String password;
    private UserType userType;

    //Constructors

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, UserType userType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    //Getters and Setters.
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}