/**
 * Repository interface for accessing and managing Review entities.
 *
 * This interface provides:
 * - Access to customer feedback linked to specific restaurants
 * - Safe calculation of average ratings using null-handling functions
 * - Enforcement of the "one review per reservation" business rule
 * - Statistical distribution of ratings for analytical dashboards
 */
package yoni.restaurantreservationsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yoni.restaurantreservationsystem.entities.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds all reviews for a specific restaurant.
     *
     * @param restId the ID of the restaurant whose reviews should be retrieved
     * @return a list of reviews belonging to the specified restaurant
     */
    List<Review> findByRestaurant_RestId(Long restId);

    /**
     * Calculates the average score of reviews for a specific restaurant.
     *
     * Uses COALESCE to return 0.0 if no reviews exist.
     *
     * @param restId the ID of the restaurant whose average score should be calculated
     * @return the average review score (or 0.0 if no reviews exist)
     */
    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Review r WHERE r.restaurant.restId = :restId")
    float getAverageScoreByRestaurantId(@Param("restId") Long restId);

    /**
     * Checks whether a review already exists for a given reservation.
     *
     * This is used to enforce the rule of one review per reservation.
     *
     * @param reseId the ID of the reservation
     * @return true if a review exists for the reservation, otherwise false
     */
    boolean existsByReservation_ReseId(Long reseId);

    /**
     * Finds the review associated with a specific reservation.
     *
     * @param reseId the ID of the reservation
     * @return an Optional containing the review if found, otherwise empty
     */
    Optional<Review> findByReservation_ReseId(Long reseId);

    /**
     * Groups reviews by their numerical score to show the distribution of ratings.
     *
     * Each result row in the list contains:
     * - The score value (Integer)
     * - The count of reviews with that specific score (Long)
     *
     * @param restId the unique ID of the restaurant whose rating distribution is needed
     * @return a list of Object arrays representing the count for each rating level (1-5)
     */
    @Query("SELECT r.score, COUNT(r) FROM Review r " +
            "WHERE r.restaurant.restId = :restId " +
            "GROUP BY r.score")
    List<Object[]> countReviewsByScore(@Param("restId") Long restId);
}
