/**
 * RestController that handles all incoming HTTP requests related to restaurant statistics.
 *
 * This controller provides endpoints for:
 * - Retrieving statistical data for a specific restaurant
 *
 * It ensures that only the restaurant owner (ADMIN) can access the statistics.
 */
package yoni.restaurantreservationsystem.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import yoni.restaurantreservationsystem.responses.StatisticsResponse;
import yoni.restaurantreservationsystem.services.StatisticsService;
import yoni.restaurantreservationsystem.util.AuthUtil;

@RestController
public class StatisticsController {

    private final StatisticsService statisticsService;

    //Constructor
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Retrieves statistical data for a specific restaurant owned by the logged-in admin.
     *
     * @param id the unique identifier of the restaurant
     * @param session the HTTP session used to identify the logged-in admin
     * @return a ResponseEntity containing the restaurant statistics
     * @throws SecurityException if the user is not logged in or not the owner of the restaurant
     * @throws IllegalArgumentException if the restaurant ID is invalid
     */
    @GetMapping("/restaurant/api/statistics/{id}")
    public ResponseEntity<?> getRestaurantStatistics(@PathVariable Long id, HttpSession session) {
        try {
            Long adminId = AuthUtil.getUserIdOrThrow(session);

            StatisticsResponse response = statisticsService.getRestaurantStatistics(id, adminId);

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching statistics.");
        }
    }
}