/**
 * Service class that handles all restaurant-related business logic.
 *
 * This service is responsible for creating, updating, deleting, retrieving,
 * and managing restaurants. It also handles restaurant image uploads and
 * rating calculations.
 *
 * The service enforces important business rules such as:
 * - Only ADMIN users can create, update, delete, or manage restaurants.
 * - Restaurant names must be unique.
 * - Restaurant capacity must be valid.
 * - Opening and closing hours must be valid and aligned to 30-minute intervals.
 */
package yoni.restaurantreservationsystem.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import yoni.restaurantreservationsystem.entities.*;
import yoni.restaurantreservationsystem.repositories.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RestaurantService {

    private static final int MIN_OPENING_TIME = 90;
    private static final int HALF_HOUR = 30;


    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    //Constructor
    public RestaurantService(RestaurantRepository restaurantRepository,
                             UserService userService,ReviewRepository reviewRepository ) {
        this.restaurantRepository = restaurantRepository;
        this.userService = userService;
        this.reviewRepository =  reviewRepository;
    }

    /**
     * Creates a new restaurant listing after validating admin permissions and business constraints.
     *
     * @param name the commercial name of the restaurant; must be unique and non-blank
     * @param description a brief text describing the restaurant's culinary style or atmosphere
     * @param capacity the maximum number of guests the establishment can accommodate
     * @param openingHour the time the restaurant starts daily operations; must be on a 30-minute interval
     * @param closingHour the time the restaurant ends daily operations; must allow at least 90 minutes of operation
     * @param restaurantType the cuisine category (e.g., PIZZA, MEAT) used for categorization and default images
     * @param adminId the unique ID of the user creating the restaurant; must have ADMIN privileges
     * @return the saved Restaurant entity with a default image path assigned based on its type
     * @throws SecurityException if the user is not an administrator
     * @throws IllegalArgumentException if the name exists, capacity is invalid, or time constraints are violated
     */
    @Transactional
    public Restaurant createRestaurant(String name, String description, int capacity,
                                       LocalTime openingHour, LocalTime closingHour,
                                       RestaurantType restaurantType, Long adminId) {

        User admin = userService.getUserById(adminId);

        if (admin.getUserType() != UserType.ADMIN) {
            throw new SecurityException("Only admins can create restaurants");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Restaurant name is required");
        }

        if (restaurantRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Restaurant name already exists.");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        if (restaurantType == null) {
            throw new IllegalArgumentException("Restaurant type is required");
        }

        if (openingHour == null || closingHour == null) {
            throw new IllegalArgumentException("Opening and closing hours are required");
        }

        if (!openingHour.isBefore(closingHour)) {
            throw new IllegalArgumentException("Opening hour must be before closing hour");
        }

        if (openingHour.getMinute() % HALF_HOUR != 0 || closingHour.getMinute() % HALF_HOUR != 0) {
            throw new IllegalArgumentException("Opening and closing hours must be exactly on the hour or half-hour (e.g., 18:00 or 18:30)");
        }

        long minutesBetween = java.time.Duration.between(openingHour, closingHour).toMinutes();
        if (minutesBetween < MIN_OPENING_TIME) {
            throw new IllegalArgumentException("There must be at least one and a half hours between opening and closing time");
        }

        name = name.trim();

        String typeName = restaurantType.name().toLowerCase();
        String defaultImagePath = "/restImg/" + typeName + "_default.png";

        Restaurant restaurant = new Restaurant(name, description, capacity,
                openingHour, closingHour, restaurantType, defaultImagePath, admin);

        return restaurantRepository.save(restaurant);
    }

    /**
     * Updates an existing restaurant's profile information.
     *
     * @param restaurantId the ID of the restaurant to be modified
     * @param name the new name; validated for uniqueness if changed
     * @param description updated text about the restaurant
     * @param capacity updated seating capacity
     * @param openingHour updated opening time; must remain valid according to operational rules
     * @param closingHour updated closing time; must remain valid according to operational rules
     * @param restaurantType updated cuisine type
     * @param adminId the ID of the administrator requesting the update; must be the owner of the restaurant
     * @return the updated and saved Restaurant entity
     * @throws SecurityException if the user is not the owner or lacks admin rights
     * @throws NoSuchElementException if the restaurant does not exist
     */
    @Transactional
    public Restaurant updateRestaurant(Long restaurantId,  String name, String description,
                                       int capacity, LocalTime openingHour, LocalTime closingHour,
                                       RestaurantType restaurantType, Long adminId) {

        Restaurant restaurant = getRestaurantById(restaurantId);
        User admin = userService.getUserById(adminId);

        if (admin.getUserType() != UserType.ADMIN) {
            throw new SecurityException("Only admins can update restaurants");
        }

        if (!restaurant.getAdmin().getUserId().equals(adminId)) {
            throw new SecurityException("You can only update your own restaurants");
        }

        if (name != null && !name.isBlank()) {
            String trimmedName = name.trim();
            if (!trimmedName.equalsIgnoreCase(restaurant.getName()) &&
                    restaurantRepository.existsByNameIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("Restaurant name already exists.");
            }
            restaurant.setName(trimmedName);
        }

        if (description != null) {
            restaurant.setDescription(description.trim());
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        } else {
            restaurant.setCapacity(capacity);
        }

        if (openingHour != null && closingHour != null) {
            if (!openingHour.isBefore(closingHour)) {
                throw new IllegalArgumentException("Opening hour must be before closing hour");
            }
            if (openingHour.getMinute() % HALF_HOUR != 0 || closingHour.getMinute() % HALF_HOUR != 0) {
                throw new IllegalArgumentException("Opening and closing hours must be exactly on the hour or half-hour");
            }
            long minutesBetween = java.time.Duration.between(openingHour, closingHour).toMinutes();
            if (minutesBetween < MIN_OPENING_TIME) {
                throw new IllegalArgumentException("There must be at least one and a half hours between opening and closing time");
            }

            restaurant.setOpeningHour(openingHour);
            restaurant.setClosingHour(closingHour);
        }

        if (restaurantType != null) {
            restaurant.setRestaurantType(restaurantType);
        }

        return restaurantRepository.save(restaurant);
    }

    /**
     * Permanently removes a restaurant from the system.
     *
     * @param restaurantId the ID of the restaurant to delete
     * @param adminId the ID of the administrator requesting deletion; must be the owner
     * @throws SecurityException if the user is not the authorized owner
     */
    @Transactional
    public void deleteRestaurant(Long restaurantId, Long adminId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        User admin = userService.getUserById(adminId);

        if (admin.getUserType() != UserType.ADMIN) {
            throw new SecurityException("Only admins can delete restaurants");
        }

        if (!restaurant.getAdmin().getUserId().equals(adminId)) {
            throw new SecurityException("You can only delete your own restaurants");
        }

        restaurantRepository.delete(restaurant);
    }

    /**
     * Uploads and saves a new image for a restaurant.
     *
     * The method validates that an image was provided, verifies that the uploaded
     * file is an image, checks that the requesting admin owns the restaurant,
     * saves the image file into the static restaurant images directory, and updates
     * the restaurant image path in the database.
     *
     * @param restId the ID of the restaurant whose image should be updated
     * @param image the uploaded image file
     * @param adminId the ID of the admin requesting the upload
     * @return the updated restaurant entity with the new image path
     * @throws IllegalArgumentException if the uploaded file is missing or is not an image
     * @throws NoSuchElementException if the restaurant is not found
     * @throws SecurityException if the admin does not own the restaurant
     * @throws RuntimeException if saving the image file fails
     */
    @Transactional
    public Restaurant uploadRestaurantImage(Long restId, MultipartFile image, Long adminId) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Please select an image to upload.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }

        Restaurant restaurant = restaurantRepository.findById(restId)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found."));

        if (!restaurant.getAdmin().getUserId().equals(adminId)) {
            throw new SecurityException("You are not allowed to upload an image for this restaurant.");
        }

        try {
            String projectDir = System.getProperty("user.dir");
            Path uploadDir = Paths.get(projectDir, "src/main/resources/static/restImg");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFileName = image.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = "restaurant_" + restId + extension;
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativeImagePath = "/restImg/" + fileName;
            restaurant.setImagePath(relativeImagePath);

            return restaurantRepository.save(restaurant);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file.", e);
        }
    }

    /**
     * Retrieves all restaurants owned by a specific administrator.
     *
     * @param adminId the ID of the admin user
     * @return a list of restaurants managed by the given admin
     * @throws SecurityException if the requested user is not an administrator
     */
    public List<Restaurant> getRestaurantsByAdminId(Long adminId) {
        User admin = userService.getUserById(adminId);

        if (admin.getUserType() != UserType.ADMIN) {
            throw new SecurityException("Only admins can view their restaurants");
        }

        return restaurantRepository.findByAdmin_UserId(adminId);
    }

    /**
     * Retrieves a restaurant by its ID.
     *
     * @param id the ID of the restaurant to retrieve
     * @return the restaurant entity with the given ID
     * @throws NoSuchElementException if the restaurant is not found
     */
    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found"));
    }

    /**
     * Retrieves all restaurants in the system.
     *
     * @return a list of all restaurant entities
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Retrieves the average user rating for a specific restaurant.
     *
     * @param restId the restaurant's primary key
     * @return a float representing the calculated average score (0.0 to 5.0)
     */
    public float getAverageRating(Long restId) {
        return reviewRepository.getAverageScoreByRestaurantId(restId);
    }
}


