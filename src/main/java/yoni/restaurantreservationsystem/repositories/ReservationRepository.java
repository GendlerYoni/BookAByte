/**
 * Repository interface for accessing and managing Reservation entities.
 *
 * This interface handles all database operations related to reservations.
 * It includes standard CRUD functionality, complex filtering for upcoming and past
 * bookings, and custom analytical queries used for generating restaurant statistics.
 */

package yoni.restaurantreservationsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yoni.restaurantreservationsystem.entities.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    /**
     * Finds reservations for a specific restaurant, date, and status.
     *
     * @param restId the ID of the restaurant whose reservations should be searched
     * @param date the reservation date to filter by
     * @param status the reservation status to filter by, usually ACTIVE
     * @return a list of reservations matching the restaurant, date, and status
     */
    List<Reservation> findByRestaurant_RestIdAndDateAndStatus(Long restId,
                                                              LocalDate date,
                                                              ReservationStatus status);

    /**
     * Finds all upcoming reservations for a specific user.
     *
     * A reservation is considered upcoming if its date is after the current date,
     * or if it is today and its time is still in the future.
     *
     * @param userId the ID of the user whose upcoming reservations should be returned
     * @param status the reservation status to filter by, usually ACTIVE
     * @param currentDate the current date used to determine whether a reservation is upcoming
     * @param currentTime the current time used when the reservation date is today
     * @return a list of upcoming reservations ordered by date and time in ascending order
     */
    @Query("""
       SELECT r FROM Reservation r
       WHERE r.user.userId = :userId
       AND r.status = :status
       AND (r.date > :currentDate OR (r.date = :currentDate AND r.time > :currentTime))
       ORDER BY r.date ASC, r.time ASC
       """)
    List<Reservation> findUpcoming(@Param("userId") Long userId,
                                   @Param("status") ReservationStatus status,
                                   @Param("currentDate") LocalDate currentDate,
                                   @Param("currentTime") LocalTime currentTime);

    /**
     * Finds all past reservations for a specific user.
     *
     * A reservation is considered past if its date is before the current date,
     * or if it is today and its time has already passed.
     *
     * @param userId the ID of the user whose past reservations should be returned
     * @param status the reservation status to filter by, usually ACTIVE
     * @param currentDate the current date used to determine whether a reservation is in the past
     * @param currentTime the current time used when the reservation date is today
     * @return a list of past reservations ordered by date and time in descending order
     */
    @Query("""
       SELECT r FROM Reservation r
       WHERE r.user.userId = :userId
       AND r.status = :status
       AND (r.date < :currentDate OR (r.date = :currentDate AND r.time < :currentTime))
       ORDER BY r.date DESC, r.time DESC
       """)
    List<Reservation> findPast(@Param("userId") Long userId,
                               @Param("status") ReservationStatus status,
                               @Param("currentDate") LocalDate currentDate,
                               @Param("currentTime") LocalTime currentTime);

    /**
     * Counts all reservations that belong to a specific restaurant.
     *
     * @param restId the ID of the restaurant whose reservations should be counted
     * @return the total number of reservations for the restaurant
     */
    long countByRestaurant_RestId(Long restId);

    /**
     * Counts reservations for a specific restaurant and reservation status.
     *
     * @param restId the ID of the restaurant whose reservations should be counted
     * @param status the reservation status to count, such as ACTIVE or CANCELED
     * @return the number of reservations matching the restaurant and status
     */
    long countByRestaurant_RestIdAndStatus(Long restId, ReservationStatus status);

    /**
     * Counts how many reservations were made for each day of the week for a specific restaurant.
     *
     * Each result row contains:
     * - the day name as a String
     * - the number of reservations for that day as a Long
     *
     * @param restId the ID of the restaurant whose reservations should be grouped by day
     * @return a list of Object arrays, where each row contains day name and reservation count
     */
    @Query("SELECT FUNCTION('DAYNAME', r.date) as dayName, COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.restaurant.restId = :restId " +
            "GROUP BY FUNCTION('DAYNAME', r.date)")
    List<Object[]> countOrdersByDay(@Param("restId") Long restId);

    /**
     * Counts how many reservations were made for each hour of the day for a specific restaurant.
     *
     * Each result row contains:
     * - the hour as an Integer
     * - the number of reservations for that hour as a Long
     *
     * @param restId the ID of the restaurant whose reservations should be grouped by hour
     * @return a list of Object arrays, where each row contains hour and reservation count
     */
    @Query("SELECT HOUR(r.time) as hour, COUNT(r) " +
            "FROM Reservation r " +
            "WHERE r.restaurant.restId = :restId " +
            "GROUP BY HOUR(r.time)")
    List<Object[]> countOrdersByHour(@Param("restId") Long restId);

    /**
     * Calculates the average number of guests per reservation for a specific restaurant.
     *
     * @param restId the ID of the restaurant whose average group size should be calculated
     * @return the average number of guests per reservation
     */
    @Query("SELECT COALESCE(AVG(r.guests), 0.0) FROM Reservation r WHERE r.restaurant.restId = :restId")
    float getAvgNumOfPeopleByRestaurantId(@Param("restId") Long restId);

    /**
     * Counts the number of unique customers who made reservations
     * at a specific restaurant.
     *
     * @param restId the ID of the restaurant whose unique customers should be counted
     * @return the number of distinct customers who made reservations at the restaurant
     */
    @Query("SELECT COUNT(DISTINCT r.user.userId) FROM Reservation r WHERE r.restaurant.restId = :restId")
    long countUniqueCustomers(@Param("restId") Long restId);

    /**
     * Finds customers who made more than one reservation at a specific restaurant.
     *
     * This method is used to calculate returning customer statistics.
     *
     * @param restId the ID of the restaurant whose returning customers should be found
     * @return a list of user IDs belonging to customers with more than one reservation
     */
    @Query("SELECT r.user.userId FROM Reservation r " +
            "WHERE r.restaurant.restId = :restId " +
            "GROUP BY r.user.userId " +
            "HAVING COUNT(r.user.userId) > 1")
    List<Long> findReturningCustomerIds(@Param("restId") Long restId);
}
