/**
 * Service class responsible for generating analytical insights and performance metrics.
 *
 * This class aggregates data from various repositories to provide restaurant admins with:
 * - Order distribution trends across days and hours
 * - Customer loyalty and return rates
 * - Cancellation patterns
 * - Average party sizes
 * - Feedback distribution (rating breakdowns)
 */
package yoni.restaurantreservationsystem.services;

import org.springframework.stereotype.Service;
import yoni.restaurantreservationsystem.entities.ReservationStatus;
import yoni.restaurantreservationsystem.entities.Restaurant;
import yoni.restaurantreservationsystem.repositories.*;
import yoni.restaurantreservationsystem.responses.StatisticsResponse;

import java.util.*;

@Service
public class StatisticsService {
    private static final float PERCENTAGE_FACTOR = 100.0f;
    private static final float EMPTY_RATE = 0.0f;
    private static final long INITIAL_COUNT = 0L;
    private static final int HOURS_IN_DAY = 24;
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final String[] WEEK_DAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    //Constructor
    public StatisticsService(ReservationRepository reservationRepository,
                             ReviewRepository reviewRepository,
                             RestaurantRepository restaurantRepository) {
        this.reservationRepository = reservationRepository;
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Builds and returns all statistics for a specific restaurant.
     *
     * The method first verifies that the restaurant exists and that the requesting
     * admin is the owner of the restaurant. If access is allowed, it calculates
     * and returns all statistics in a StatisticsResponse object.
     *
     * @param restaurantId the ID of the restaurant whose statistics should be calculated
     * @param adminId the ID of the admin requesting access to the statistics
     * @return a StatisticsResponse object containing all calculated restaurant statistics
     * @throws IllegalArgumentException if the restaurant is not found
     * @throws SecurityException if the requesting admin does not own the restaurant
     */
    public StatisticsResponse getRestaurantStatistics(Long restaurantId, Long adminId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with ID: " + restaurantId));

        if (!restaurant.getAdmin().getUserId().equals(adminId)) {
            throw new SecurityException("Access denied: You are not the owner of this restaurant.");
        }

        return new StatisticsResponse(
                calculateCancellationRate(restaurantId),
                getOrdersByDayDistribution(restaurantId),
                getOrdersByHourDistribution(restaurantId),
                calculateAvgGroupSize(restaurantId),
                calculateReturnCustomerRate(restaurantId),
                getRatingDistribution(restaurantId)
        );
    }

    /**
     * Calculates the percentage of reservations that were canceled.
     *
     * @param restaurantId the unique ID of the restaurant
     * @return a float representing the cancellation percentage (0.0 to 100.0)
     */
    private float calculateCancellationRate(Long restaurantId) {
        long total = reservationRepository.countByRestaurant_RestId(restaurantId);
        if (total == INITIAL_COUNT) return EMPTY_RATE;

        long cancelled = reservationRepository.countByRestaurant_RestIdAndStatus(
                restaurantId, ReservationStatus.CANCELED);

        return ((float) cancelled / total) * PERCENTAGE_FACTOR;
    }

    /**
     * Generates a distribution of reservations across the days of the week.
     *
     * Uses a LinkedHashMap initialized with all seven days to ensure the resulting
     * map is ordered from Sunday to Saturday, even if some days have zero orders.
     *
     * @param restaurantId the unique ID of the restaurant
     * @return a map where keys are day names (Strings) and values are reservation counts (Longs)
     */
    private Map<String, Long> getOrdersByDayDistribution(Long restaurantId) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (String day : WEEK_DAYS) {
            distribution.put(day, INITIAL_COUNT);
        }

        for (Object[] result : reservationRepository.countOrdersByDay(restaurantId)) {
            distribution.put((String) result[0], (Long) result[1]);
        }
        return distribution;
    }

    /**
     * Generates a distribution of reservations across the 24 hours of the day.
     *
     * @param restaurantId the unique ID of the restaurant
     * @return a map where keys are hours (0-23) and values are reservation counts
     */
    private Map<Integer, Long> getOrdersByHourDistribution(Long restaurantId) {
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 0; i < HOURS_IN_DAY; i++) {
            distribution.put(i, INITIAL_COUNT);
        }

        for (Object[] result : reservationRepository.countOrdersByHour(restaurantId)) {
            distribution.put((Integer) result[0], (Long) result[1]);
        }
        return distribution;
    }

    /**
     * Calculates the average party size (number of guests) for a restaurant's reservations.
     *
     * @param restaurantId the unique ID of the restaurant
     * @return a float representing the average number of people per booking
     */
    private float calculateAvgGroupSize(Long restaurantId) {
        return Optional.ofNullable(reservationRepository.getAvgNumOfPeopleByRestaurantId(restaurantId))
                .orElse(EMPTY_RATE);
    }

    /**
     * Calculates the percentage of unique customers who have visited the restaurant more than once.
     *
     * @param restaurantId the unique ID of the restaurant
     * @return a float representing the return customer percentage (0.0 to 100.0)
     */
    private float calculateReturnCustomerRate(Long restaurantId) {
        long totalUnique = reservationRepository.countUniqueCustomers(restaurantId);
        if (totalUnique == INITIAL_COUNT) return EMPTY_RATE;

        long returningCount = reservationRepository.findReturningCustomerIds(restaurantId).size();
        return ((float) returningCount / totalUnique) * PERCENTAGE_FACTOR;
    }

    /**
     * Maps the frequency of each rating score (from 1 to 5 stars).
     *
     * @param restaurantId the unique ID of the restaurant
     * @return a map where keys are scores (1-5) and values are the number of reviews for that score
     */
    private Map<Integer, Long> getRatingDistribution(Long restaurantId) {
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = MIN_RATING; i <= MAX_RATING; i++) {
            distribution.put(i, INITIAL_COUNT);
        }

        for (Object[] result : reviewRepository.countReviewsByScore(restaurantId)) {
            distribution.put((Integer) result[0], (Long) result[1]);
        }
        return distribution;
    }
}