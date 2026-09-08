/**
 * RestController that handles all incoming HTTP requests related to reservations.
 *
 * This controller acts as the entry point for reservation management, providing endpoints for:
 * - Real-time availability checks for specific restaurants and time slots
 * - Secure creation and cancellation of reservations
 * - Retrieval of filtered reservation lists (Upcoming vs. Past) for users
 * - Conversion of internal entities to public-facing response objects
 */
package yoni.restaurantreservationsystem.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yoni.restaurantreservationsystem.entities.Reservation;
import yoni.restaurantreservationsystem.requests.ReservationRequest;
import yoni.restaurantreservationsystem.responses.ReservationResponse;
import yoni.restaurantreservationsystem.services.ReservationService;
import yoni.restaurantreservationsystem.util.AuthUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    //Constructor
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Checks if a restaurant has enough available seats for a specific date, time, and party size.
     *
     * @param request a ReservationRequest object containing restaurant ID, date, time, and guest count
     * @return a ResponseEntity containing a map with a boolean "available" status
     * @throws RuntimeException if the availability check fails due to invalid input data
     */
    @PostMapping("/availability")
    public ResponseEntity<?> checkAvailability(@RequestBody ReservationRequest request) {
        try {
            boolean isAvailable = reservationService.checkAvailability(request.getRestId(),
                    request.getDate(), request.getTime(), request.getGuests());

            return ResponseEntity.ok(Map.of("available", isAvailable));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Creates a new reservation for the currently logged-in user.
     *
     * The method retrieves the user ID from the session, sends the request
     * to the service layer, and returns the created reservation as a response DTO.
     *
     * @param request the reservation request containing reservation details
     * @param session the HTTP session used to retrieve the logged-in user
     * @return ResponseEntity containing the created reservation
     * @throws SecurityException if the user is not logged in or unauthorized
     * @throws IllegalStateException if there is a scheduling conflict or no seats available
     * @throws IllegalArgumentException if the request contains invalid parameters
     */
    @PostMapping("/create")
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest request, HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            Reservation newReservation = reservationService.createReservation(userId,
                    request.getRestId(), request.getDate(), request.getTime(), request.getGuests());

            ReservationResponse response = new ReservationResponse(newReservation.getReservationId(),
                    newReservation.getRestaurant().getRestId(), newReservation.getRestaurant().getName(),
                    newReservation.getReservationDate(), newReservation.getReservationTime(),
                    newReservation.getGuests(), newReservation.getStatus());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing your reservation.");
        }
    }

    /**
     * Cancels a reservation belonging to the currently logged-in user.
     *
     * @param id the unique identifier of the reservation to be canceled
     * @param session the current HTTP session used to verify ownership of the reservation
     * @return a ResponseEntity containing the updated reservation details with CANCELED status
     * @throws SecurityException if the user attempts to cancel a reservation they do not own
     * @throws NoSuchElementException if the reservation record is not found in the database
     */
    @PostMapping("/cancel/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id, HttpSession session) {

        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            Reservation canceledReservation = reservationService.cancelReservation(id, userId);

            ReservationResponse response = new ReservationResponse(canceledReservation.getReservationId(),
                    canceledReservation.getRestaurant().getRestId(), canceledReservation.getRestaurant().getName(),
                    canceledReservation.getReservationDate(), canceledReservation.getReservationTime(),
                    canceledReservation.getGuests(), canceledReservation.getStatus());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Retrieves all upcoming active reservations for the logged-in user.
     *
     * Upcoming reservations are defined as bookings with a date in the future,
     * or bookings for today with a time that has not yet passed.
     *
     * @param session the current HTTP session used to verify the user's identity
     * @return a ResponseEntity containing a list of upcoming ReservationResponse objects
     * @throws SecurityException if the user is not authenticated or session has expired
     * @throws IllegalArgumentException if the user ID retrieved from the session is invalid
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingReservations(HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            List<Reservation> upcomingReservations = reservationService.getUpcomingReservations(userId);

            List<ReservationResponse> responseList = convertToResponseList(upcomingReservations);

            return ResponseEntity.ok(responseList);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching your upcoming reservations.");
        }
    }

    /**
     * Retrieves the reservation history (past bookings) for the logged-in user.
     *
     * @param session the current HTTP session used to identify the user
     * @return a ResponseEntity containing a list of past ReservationResponse objects
     * @throws SecurityException if the user is not authenticated
     */
    @GetMapping("/past")
    public ResponseEntity<?> getPastReservations(HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            List<Reservation> pastReservations = reservationService.getPastReservations(userId);

            List<ReservationResponse> responseList = convertToResponseList(pastReservations);

            return ResponseEntity.ok(responseList);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching your past reservations.");
        }
    }

    /**
     * Fetches a specific reservation by its unique identifier.
     *
     * This method ensures the requester is the owner of the reservation
     * before returning the sensitive data.
     *
     * @param id the unique Long ID of the reservation to retrieve
     * @param session the current HTTP session for ownership verification
     * @return a ResponseEntity containing the detailed ReservationResponse
     * @throws SecurityException if the user attempts to access a reservation they did not create
     * @throws NoSuchElementException if the reservation ID is not found in the database
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = AuthUtil.getUserIdOrThrow(session);

            Reservation reservation = reservationService.secureGetReservationById(id, userId);

            ReservationResponse response = new ReservationResponse(reservation.getReservationId(),
                    reservation.getRestaurant().getRestId(), reservation.getRestaurant().getName(),
                    reservation.getReservationDate(), reservation.getReservationTime(),
                    reservation.getGuests(),
                    reservation.getStatus(), reservation.getRestaurant().getImagePath());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching reservation.");
        }
    }

    /**
     * Internal helper method to map Reservation list entities into DTOs.
     *
     * @param reservations a list of Reservation entities fetched from the database
     * @return a list of formatted ReservationResponse objects suitable for JSON serialization
     */
    private List<ReservationResponse> convertToResponseList(List<Reservation> reservations) {
        List<ReservationResponse> responseList = new ArrayList<>();
        for (Reservation res : reservations) {
            responseList.add(new ReservationResponse(res.getReservationId(), res.getRestaurant().getRestId(),
                    res.getRestaurant().getName(), res.getReservationDate(),
                    res.getReservationTime(), res.getGuests(), res.getStatus(),
                    res.getRestaurant().getImagePath()));
        }
        return responseList;
    }
}
