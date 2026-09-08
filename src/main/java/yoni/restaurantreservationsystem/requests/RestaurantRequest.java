/**
 * Data Transfer Object representing a request to create or update a restaurant's profile.
 *
 * This request encapsulates all core business data for a restaurant, including:
 * - Descriptive details (name, description, type)
 * - Operational constraints (seating capacity, opening and closing hours)
 *
 * It is used primarily by ADMIN users to manage their restaurant listings.
 */
package yoni.restaurantreservationsystem.requests;

import yoni.restaurantreservationsystem.entities.RestaurantType;
import java.time.LocalTime;

public class RestaurantRequest {
    private String name;
    private String description;
    private int capacity;
    private LocalTime openingHour;
    private LocalTime closingHour;
    private RestaurantType restaurantType;

    //Constructors

    public RestaurantRequest() {}

    public RestaurantRequest(String name, String description, int capacity,
                             LocalTime openingHour, LocalTime closingHour,
                             RestaurantType restaurantType) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.restaurantType = restaurantType;
    }

    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LocalTime getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(LocalTime openingHour) {
        this.openingHour = openingHour;
    }

    public RestaurantType getRestaurantType() {
        return restaurantType;
    }

    public void setRestaurantType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    public LocalTime getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(LocalTime closingHour) {
        this.closingHour = closingHour;
    }
}