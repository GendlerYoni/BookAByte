/**
 * Represents a restaurant in the system.
 *
 * Each restaurant has:
 * - Basic details such as name, description, and type
 * - Operating hours (opening and closing times)
 * - Maximum seating capacity
 * - An associated admin (owner)
 * - A list of reservations and reviews
 *
 * The restaurant also stores an optional image path for display purposes.
 */
package yoni.restaurantreservationsystem.entities;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    private static final int MAX_LENGTH = 90;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long restId;

    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false, length = MAX_LENGTH)
    private String description;
    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalTime openingHour;
    @Column(nullable = false)
    private LocalTime closingHour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantType restaurantType;

    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "adminID", nullable = false)
    private User admin;


    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();


    //Constructors
    public Restaurant() {
    }

    public Restaurant(String name, String description, int capacity,
                      LocalTime openingHour, LocalTime closingHour,
                      RestaurantType restaurantType, String imagePath, User admin) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.restaurantType = restaurantType;
        this.admin = admin;
        this.imagePath = imagePath;
    }

    //Getters and Setters.
    public Long getRestId() {
        return restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

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

    public LocalTime getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(LocalTime closingHour) {
        this.closingHour = closingHour;
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

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
