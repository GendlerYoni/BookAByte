/**
 * RestController that handles all incoming HTTP requests related to reviews.
 *
 * This controller provides endpoints for:
 * - Creating a review for a completed reservation
 * - Retrieving all reviews for a specific restaurant
 * - Checking if the logged-in user can review a reservation
 * - Retrieving a review connected to a specific reservation
 *
 * It uses the current HTTP session to protect user-specific review actions.
 */
package yoni.restaurantreservationsystem.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yoni.restaurantreservationsystem.entities.Review;
import yoni.restaurantreservationsystem.requests.ReviewRequest;
import yoni.restaurantreservationsystem.responses.ReviewResponse;
import yoni.restaurantreservationsystem.services.ReviewService;
import yoni.restaurantreservationsystem.util.AuthUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    //Constructor
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Submits a new customer review for a specific reservation.
     *
     * The method verifies the user session, validates the review data, and
     * ensures the user is eligible to review that specific reservation.
     *
     * @param request the ReviewRequest containing the reservation ID, feedback text, and numeric score
     * @param session the current HTTP session used to retrieve the authenticated user ID
     * @return a ResponseEntity containing the successfully created ReviewResponse
     * @throws SecurityException if the user is not logged in
     * @throws IllegalStateException if a review already exists for this reservation (Conflict)
     * @throws IllegalArgumentException if the score is out of range or description is missing
     * @throws NoSuchElementException if the target reservation cannot be found
     */
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request, HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            Review newReview = reviewService.createReview(userId,
                    request.getReseId(), request.getRevDescription(), request.getScore());

            return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(newReview));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while adding your review.");
        }
    }

    /**
     * Retrieves all reviews for a specific restaurant.
     *
     * @param restId the unique identifier of the restaurant
     * @return a ResponseEntity containing a list of ReviewResponse objects
     * @throws IllegalArgumentException if the restaurant ID is invalid
     */
    @GetMapping("/restaurant/{restId}")
    public ResponseEntity<?> getReviewsByRestaurant(@PathVariable Long restId) {
        try {
            List<Review> reviews = reviewService.getReviewsByRestaurant(restId);
            List<ReviewResponse> responseList = new ArrayList<>();

            for (Review review : reviews) {
                responseList.add(convertToResponse(review));
            }

            return ResponseEntity.ok(responseList);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching reviews.");
        }
    }

    /**
     * Checks whether the currently logged-in user can review a specific reservation.
     *
     * @param reseId the unique identifier of the reservation
     * @param session the HTTP session used to identify the logged-in user
     * @return a ResponseEntity containing true if the user can review the reservation, otherwise false
     * @throws SecurityException if the user is not logged in
     */
    @GetMapping("/can-review/{reseId}")
    public ResponseEntity<?> canReview(@PathVariable Long reseId, HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            boolean canReview = reviewService.canUserReviewReservation(userId, reseId);

            return ResponseEntity.ok(canReview);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking review eligibility.");
        }
    }

    /**
     * Retrieves the specific review details linked to a reservation.
     *
     * The method verifies that the logged-in user owns the reservation before returning the review.
     *
     * @param reseId the unique identifier of the reservation
     * @param session the current HTTP session used to verify the user owns the reservation
     * @return a ResponseEntity containing the ReviewResponse if found
     * @throws NoSuchElementException if no review exists for the given reservation
     * @throws SecurityException if the user attempts to access a review for a reservation they do not own
     */
    @GetMapping("/reservation/{reseId}")
    public ResponseEntity<?> getReviewByReservation(@PathVariable Long reseId, HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            Review review = reviewService.getReviewByReservation(reseId, userId);

            return ResponseEntity.ok(convertToResponse(review));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching review.");
        }
    }

    /**
     * Internal helper method to map a Review entity to a ReviewResponse DTO.
     *
     * @param review the Review database entity to be converted
     * @return a formatted ReviewResponse object containing display-ready data
     */
    private ReviewResponse convertToResponse(Review review) {
        return new ReviewResponse(review.getRevId(), review.getUser().getUsername(), review.getScore(),
                review.getRevDescription(), review.getReservation().getReservationDate());
    }
}
