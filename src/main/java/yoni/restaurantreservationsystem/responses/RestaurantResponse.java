/**
 * Data Transfer Object used to provide comprehensive restaurant details.
 * In addition to basic restaurant metadata, this response includes calculated
 * fields like average score and total review count, which are essential for
 * rendering restaurant cards and profile pages.
 */
package yoni.restaurantreservationsystem.responses;

import yoni.restaurantreservationsystem.entities.RestaurantType;
import java.time.LocalTime;

public class RestaurantResponse {
    private Long restId;
    private String name;
    private String description;
    private LocalTime openingHour;
    private LocalTime closingHour;
    private RestaurantType restaurantType;
    private int capacity;
    private String imagePath;
    private float averageScore;
    private int reviewCount;

    public RestaurantResponse(Long restId, String name, String description,
                              LocalTime openingHour, LocalTime closingHour,
                              RestaurantType restaurantType,int capacity, String imagePath,
                              float averageScore, int reviewCount) {
        this.restId = restId;
        this.name = name;
        this.description = description;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.restaurantType = restaurantType;
        this.capacity = capacity;
        this.imagePath = imagePath;
        this.averageScore = averageScore;
        this.reviewCount = reviewCount;
    }

    public Long getRestId() {
        return restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public RestaurantType getRestaurantType() {
        return restaurantType;
    }

    public void setRestaurantType(RestaurantType restaurantType) {
        this.restaurantType = restaurantType;
    }

    public LocalTime getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(LocalTime openingHour) {
        this.openingHour = openingHour;
    }

    public LocalTime getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(LocalTime closingHour) {
        this.closingHour = closingHour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
