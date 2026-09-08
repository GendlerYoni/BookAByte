/**
 * RestController that handles all incoming HTTP requests related to restaurants.
 *
 * This controller provides endpoints for:
 * - Retrieving all restaurants or a specific restaurant
 * - Retrieving restaurants owned by the logged-in admin
 * - Creating, updating, and deleting restaurants
 * - Uploading restaurant images
 *
 * It ensures that only authorized ADMIN users can perform management operations.
 */
package yoni.restaurantreservationsystem.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yoni.restaurantreservationsystem.entities.Restaurant;
import yoni.restaurantreservationsystem.requests.RestaurantRequest;
import yoni.restaurantreservationsystem.responses.RestaurantResponse;
import yoni.restaurantreservationsystem.services.RestaurantService;
import yoni.restaurantreservationsystem.util.AuthUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    //Constructor
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * Retrieves all restaurants in the system.
     *
     * @return a ResponseEntity containing a list of RestaurantResponse objects
     * @throws RuntimeException if an error occurs while fetching restaurants
     */
    @GetMapping
    public ResponseEntity<?> getAllRestaurants() {
        try {
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();
            List<RestaurantResponse> responseList = new ArrayList<>();

            for (Restaurant rest : restaurants) {
                responseList.add(convertToResponse(rest));
            }

            return ResponseEntity.ok(responseList);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching restaurants.");
        }
    }

    /**
     * Retrieves a specific restaurant by its ID.
     *
     * @param id the unique identifier of the restaurant
     * @return a ResponseEntity containing the restaurant details
     * @throws NoSuchElementException if the restaurant is not found
     * @throws IllegalArgumentException if the request is invalid
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurantById(@PathVariable Long id) {
        try {
            Restaurant rest = restaurantService.getRestaurantById(id);

            return ResponseEntity.ok(convertToResponse(rest));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the restaurant.");
        }
    }

    /**
     * Retrieves all restaurants owned by the currently logged-in admin.
     *
     * @param session the HTTP session used to identify the logged-in admin
     * @return a ResponseEntity containing a list of the admin's restaurants
     * @throws SecurityException if the user is not logged in or not an admin
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyRestaurants(HttpSession session) {
        try {
            Long adminId = AuthUtil.getUserIdOrThrow(session);

            List<Restaurant> restaurants = restaurantService.getRestaurantsByAdminId(adminId);
            List<RestaurantResponse> responseList = new ArrayList<>();

            for (Restaurant rest : restaurants) {
                responseList.add(convertToResponse(rest));
            }

            return ResponseEntity.ok(responseList);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching your restaurants.");
        }
    }

    /**
     * Creates a new restaurant for the currently logged-in admin.
     *
     * @param request the restaurant request containing restaurant details
     * @param session the HTTP session used to identify the logged-in admin
     * @return a ResponseEntity containing the created restaurant
     * @throws SecurityException if the user is not authorized
     * @throws IllegalArgumentException if the request contains invalid data
     */
    @PostMapping
    public ResponseEntity<?> createRestaurant(@RequestBody RestaurantRequest request, HttpSession session) {
        try {
            Long adminId = AuthUtil.getUserIdOrThrow(session);

            Restaurant newRestaurant = restaurantService.createRestaurant(request.getName(),
                    request.getDescription(), request.getCapacity(), request.getOpeningHour(),
                    request.getClosingHour(), request.getRestaurantType(), adminId);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(newRestaurant));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the restaurant.");
        }
    }

    /**
     * Updates an existing restaurant owned by the currently logged-in admin.
     *
     * @param id the unique identifier of the restaurant to update
     * @param request the updated restaurant data
     * @param session the HTTP session used to identify the logged-in admin
     * @return a ResponseEntity containing the updated restaurant
     * @throws SecurityException if the user is not authorized
     * @throws NoSuchElementException if the restaurant is not found
     * @throws IllegalArgumentException if the request contains invalid data
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id,
                                              @RequestBody RestaurantRequest request,
                                              HttpSession session) {
        try {
            Long adminId = AuthUtil.getUserIdOrThrow(session);

            Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, request.getName(),
                    request.getDescription(), request.getCapacity(), request.getOpeningHour(),
                    request.getClosingHour(), request.getRestaurantType(), adminId);

            return ResponseEntity.ok(convertToResponse(updatedRestaurant));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the restaurant.");
        }
    }

    /**
     * Deletes a restaurant owned by the currently logged-in admin.
     *
     * @param id the unique identifier of the restaurant to delete
     * @param session the HTTP session used to identify the logged-in admin
     * @return a ResponseEntity containing a success message
     * @throws SecurityException if the user is not authorized
     * @throws NoSuchElementException if the restaurant is not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id, HttpSession session) {
        try {
            Long adminId = AuthUtil.getUserIdOrThrow(session);

            restaurantService.deleteRestaurant(id, adminId);

            return ResponseEntity.ok(java.util.Map.of("message", "Restaurant deleted successfully"));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the restaurant.");
        }
    }

    /**
     * Uploads and assigns a new image to a restaurant.
     *
     * @param id the unique identifier of the restaurant
     * @param image the uploaded image file
     * @param session the HTTP session used to identify the logged-in admin
     * @return a ResponseEntity containing the updated restaurant with the new image
     * @throws SecurityException if the user is not authorized
     * @throws NoSuchElementException if the restaurant is not found
     * @throws IllegalArgumentException if the uploaded file is invalid
     */
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadRestaurantImage(@PathVariable Long id,
                                                   @RequestParam("image") MultipartFile image,
                                                   HttpSession session) {
        try {
            Long adminId = AuthUtil.getUserIdOrThrow(session);

            Restaurant updatedRestaurant = restaurantService.uploadRestaurantImage(id, image, adminId);

            return ResponseEntity.ok(convertToResponse(updatedRestaurant));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while uploading the restaurant image.");
        }
    }

    /**
     * Converts a Restaurant entity into a RestaurantResponse DTO.
     *
     * @param rest the restaurant entity
     * @return the mapped RestaurantResponse object
     */
    private RestaurantResponse convertToResponse(Restaurant rest) {
        return new RestaurantResponse(rest.getRestId(), rest.getName(), rest.getDescription(),
                rest.getOpeningHour(), rest.getClosingHour(), rest.getRestaurantType(),
                rest.getCapacity(), rest.getImagePath(),
                restaurantService.getAverageRating(rest.getRestId()), rest.getReviews().size());
    }


}
