/**
 * Repository interface for accessing and managing Restaurant entities.
 *
 * This interface provides:
 * - Standard CRUD operations for restaurants
 * - Administrative lookups for restaurant owners (Admins)
 * - Name uniqueness validation
 * - Concurrency control using pessimistic locking for critical updates
 */
package yoni.restaurantreservationsystem.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yoni.restaurantreservationsystem.entities.*;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    /**
     * Finds all restaurants managed by a specific admin user.
     *
     * @param adminId the ID of the admin whose restaurants should be returned
     * @return a list of restaurants managed by the given admin
     */
    List<Restaurant> findByAdmin_UserId(Long adminId);

    /**
     * Checks whether a restaurant with the given name already exists,
     * ignoring uppercase/lowercase differences.
     *
     * @param name the restaurant name to check
     * @return true if a restaurant with this name already exists, otherwise false
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Finds a restaurant by its ID and applies a pessimistic write lock.
     *
     * This method is crucial for defensive programming; it prevents race conditions
     * by locking the database row during seat availability checks and booking updates,
     * ensuring no two customers can overbook the same time slot simultaneously.
     *
     * @param id the unique ID of the restaurant to retrieve and lock
     * @return an Optional containing the restaurant if found, or empty if not
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Restaurant r WHERE r.restId = :id")
    Optional<Restaurant> findByIdWithLock(@Param("id") Long id);
}