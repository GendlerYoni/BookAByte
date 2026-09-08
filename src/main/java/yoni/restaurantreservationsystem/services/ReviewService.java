/**
 * Service class that handles all review-related business logic.
 *
 * This service is responsible for:
 * - Creating reviews for reservations
 * - Validating whether a user is allowed to review a reservation
 * - Retrieving reviews by restaurant
 * - Retrieving a specific review for a reservation
 *
 * The service enforces important rules such as:
 * - Only the user who made the reservation can review it
 * - A reservation can only be reviewed once
 * - Reviews can only be created after the reservation time has passed
 * - Review scores must be within a valid range
 */
package yoni.restaurantreservationsystem.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import yoni.restaurantreservationsystem.entities.Reservation;
import yoni.restaurantreservationsystem.entities.Review;
import yoni.restaurantreservationsystem.repositories.*;
import yoni.restaurantreservationsystem.entities.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    //Constructor
    public ReviewService(ReviewRepository reviewRepository, ReservationRepository reservationRepository) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Checks if a specific user is eligible to review a specific reservation.
     *
     * @param userId the unique ID of the user trying to write a review
     * @param reseId the unique ID of the reservation to be reviewed
     * @return true if the user is authorized and all business rules are met, false otherwise
     */
    public boolean canUserReviewReservation(Long userId, Long reseId) {
        if (reseId == null) {
            return false;
        }

        Reservation reservation = reservationRepository.findById(reseId).orElse(null);
        return canUserReviewReservation(userId, reservation);
    }

    /**
     * Creates a new review for a reservation.
     *
     * The method performs the following validations:
     * - Ensures required parameters are provided
     * - Validates the score range (1–5)
     * - Ensures the reservation exists
     * - Ensures the user is allowed to review the reservation
     *
     * If all validations pass, a new review is created and saved.
     *
     * @param userId the ID of the user creating the review
     * @param reseId the ID of the reservation being reviewed
     * @param revDescription the textual description of the review
     * @param score the rating given (must be between 1 and 5)
     * @return the created Review entity
     *
     * @throws IllegalArgumentException if any input is invalid
     * @throws NoSuchElementException if the reservation does not exist
     */
    @Transactional
    public Review createReview(Long userId, Long reseId, String revDescription, Integer score) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }

        if (reseId == null) {
            throw new IllegalArgumentException("Reservation ID is required.");
        }

        if (revDescription == null || revDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Review description is required.");
        }

        if (score == null) {
            throw new IllegalArgumentException("Score is required.");
        }

        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("Score must be between 1 and 5.");
        }

        Reservation reservation = reservationRepository.findById(reseId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found."));

        if (!canUserReviewReservation(userId, reservation)) {
            throw new IllegalStateException("This reservation cannot be reviewed.");
        }

        Review review = new Review(reservation.getUser(), reservation.getRestaurant(),
                reservation, revDescription.trim(), score);

        return reviewRepository.save(review);
    }


    /**
     * Retrieves all reviews for a specific restaurant.
     *
     * @param restId the ID of the restaurant
     * @return a list of reviews for the restaurant
     *
     * @throws IllegalArgumentException if restId is null
     */
    public List<Review> getReviewsByRestaurant(Long restId) {
        if (restId == null) {
            throw new IllegalArgumentException("Restaurant ID is required.");
        }
        return reviewRepository.findByRestaurant_RestId(restId);
    }

    /**
     * Retrieves a single review associated with a specific reservation.
     *
     * @param reseId the ID of the reservation
     * @param userId the ID of the user requesting the data (for ownership verification)
     * @return the found Review entity
     * @throws SecurityException if the user is not the owner of the reservation
     * @throws NoSuchElementException if the reservation or the review does not exist
     */
    public Review getReviewByReservation(Long reseId, Long userId) {

        if (reseId == null) {
            throw new IllegalArgumentException("Reservation ID is required.");
        }

        Reservation reservation = reservationRepository.findById(reseId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found."));

        if (!reservation.getUser().getUserId().equals(userId)) {
            throw new SecurityException("You are not allowed to access this review.");
        }

        return reviewRepository.findByReservation_ReseId(reseId)
                .orElseThrow(() -> new NoSuchElementException("Review not found."));
    }

    /**
     * Determines whether a user can review a given reservation.
     *
     * The method checks:
     * - The user and reservation are not null
     * - The reservation belongs to the user
     * - The reservation status is ACTIVE
     * - The reservation has not already been reviewed
     * - The reservation date and time have already passed
     *
     * @param userId the ID of the user
     * @param reservation the reservation entity
     * @return true if the user can review the reservation, otherwise false
     */
    private boolean canUserReviewReservation(Long userId, Reservation reservation) {
        if (userId == null || reservation == null) {
            return false;
        }

        if (reservation.getUser() == null || reservation.getUser().getUserId() == null) {
            return false;
        }

        if (!reservation.getUser().getUserId().equals(userId)) {
            return false;
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            return false;
        }

        if (reviewRepository.existsByReservation_ReseId(reservation.getReseId())) {
            return false;
        }

        LocalDate reservationDate = reservation.getReservationDate();
        LocalTime reservationTime = reservation.getReservationTime();

        if (reservationDate == null || reservationTime == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (reservationDate.isAfter(today)) {
            return false;
        }

        if (reservationDate.isEqual(today) && reservationTime.isAfter(now)) {
            return false;
        }

        return true;
    }

}
