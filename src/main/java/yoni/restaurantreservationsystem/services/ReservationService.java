/**
 * Service class for managing restaurant reservations.
 *
 * This class handles the core business logic of the system, including:
 * - Validating and creating new reservations with concurrency control (locking)
 * - Calculating table availability based on time-slot overlaps
 * - Managing reservation lifecycles (cancellation and retrieval)
 * - Ensuring security by verifying user permissions for specific actions
 */
package yoni.restaurantreservationsystem.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import yoni.restaurantreservationsystem.entities.*;
import yoni.restaurantreservationsystem.repositories.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

@Service
public class ReservationService {

    private static final int SLOT_DURATION_MINUTES = 90;

    private final ReservationRepository reservationRepository;
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final RestaurantRepository restaurantRepository;

    //Constructor
    public ReservationService(ReservationRepository reservationRepository,
                              UserService userService,
                              RestaurantService restaurantService,
                              RestaurantRepository restaurantRepository) {
        this.reservationRepository = reservationRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Creates a new reservation after performing strict validation and availability checks.
     * Uses pessimistic locking on the restaurant record to prevent race conditions
     * where two users might book the last available seats at the same microsecond.
     *
     * @param userId the ID of the customer making the booking
     * @param restId the ID of the restaurant being booked
     * @param date the intended date of the reservation
     * @param startTime the intended start time of the reservation
     * @param requestedSeats the number of guests in the party
     * @return the saved Reservation entity
     * @throws IllegalArgumentException if IDs are null or basic validation fails
     * @throws SecurityException if the user is not a CUSTOMER
     * @throws IllegalStateException if the restaurant is fully booked for the requested slot
     */
    @Transactional
    public Reservation createReservation(Long userId, Long restId, LocalDate date,
                                         LocalTime startTime, int requestedSeats) {

        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        validateReservationRequest(restId, date, startTime, requestedSeats);
        /*if (date.isBefore(LocalDate.now()) ||
                (date.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now()))) {
            throw new IllegalArgumentException("Cannot make a reservation in the past");
        }*/
        //Need to comment if you want to make a past reservation ^^
        User user = userService.getUserById(userId);

        if (user.getUserType() != UserType.CUSTOMER) {
            throw new SecurityException("Only customers can make a reservation.");
        }

        Restaurant restaurant = restaurantRepository.findByIdWithLock(restId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        if (!checkAvailability(restId, date, startTime, requestedSeats)) {
            throw new IllegalStateException("No available seats for the requested time");
        }

        Reservation newReservation = new Reservation(user, restaurant, date,
                startTime, requestedSeats, ReservationStatus.ACTIVE);

        return reservationRepository.save(newReservation);
    }

    /**
     * Cancels an existing reservation if it belongs to the given user.
     *
     * The reservation is not deleted from the database. Instead, its status is changed
     * to CANCELED so the system can keep reservation history and use it for statistics.
     *
     * @param reservationId the ID of the reservation that the user wants to cancel
     * @param userId the ID of the user requesting the cancellation
     * @return the updated reservation after its status is changed to CANCELED
     * @throws IllegalArgumentException if reservationId or userId is null
     * @throws SecurityException if the reservation does not belong to the given user
     * @throws IllegalStateException if the reservation is already canceled
     */
    @Transactional
    public Reservation cancelReservation(Long reservationId, Long userId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Reservation id is required");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        Reservation reservation = getReservationById(reservationId);

        if (!reservation.getUser().getUserId().equals(userId)) {
            throw new SecurityException("You can cancel only your own reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELED);

        return reservationRepository.save(reservation);
    }

    /**
     * Fetches all active reservations for a user that are scheduled for the future.
     *
     * @param userId the ID of the user
     * @return a list of upcoming active reservations
     * @throws IllegalArgumentException if the userId is null
     */
    public List<Reservation> getUpcomingReservations(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        userService.getUserById(userId);

        return reservationRepository.findUpcoming(userId, ReservationStatus.ACTIVE,
                LocalDate.now(), LocalTime.now());
    }

    /**
     * Fetches all past reservations for a user for history display.
     *
     * @param userId the ID of the user
     * @return a list of historical reservations
     * @throws IllegalArgumentException if the userId is null
     */
    public List<Reservation> getPastReservations(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        userService.getUserById(userId);

        return reservationRepository.findPast(userId, ReservationStatus.ACTIVE,
                LocalDate.now(), LocalTime.now());
    }

    /**
     * Checks whether a reservation can be created for the requested time and number of guests.
     *
     * The method validates the reservation request, checks the restaurant's
     * capacity and opening hours, and calculates the maximum number of seats
     * occupied at any point during the requested time window.
     *
     * @param restId the ID of the restaurant
     * @param date the requested reservation date
     * @param startTime the requested reservation start time
     * @param requestedSeats the number of guests for the reservation
     * @return true if the reservation can be accommodated, otherwise false
     */
    public boolean checkAvailability(Long restId, LocalDate date, LocalTime startTime, int requestedSeats) {
        validateReservationRequest(restId, date, startTime, requestedSeats);

        Restaurant restaurant = restaurantService.getRestaurantById(restId);

        if (requestedSeats > restaurant.getCapacity()) {
            return false;
        }

        LocalTime endTime = startTime.plusMinutes(SLOT_DURATION_MINUTES);

        if (startTime.isBefore(restaurant.getOpeningHour()) || endTime.isAfter(restaurant.getClosingHour())) {
            return false;
        }

        List<Reservation> existingReservations =
                reservationRepository.findByRestaurant_RestIdAndDateAndStatus(restId,
                        date, ReservationStatus.ACTIVE);

        int maxOccupiedSeats =
                calculateMaxOccupiedSeats(existingReservations, startTime, endTime);

        return maxOccupiedSeats + requestedSeats <= restaurant.getCapacity();
    }

    /**
     * Retrieves a reservation by its ID without security checks.
     *
     * @param reservationId the ID of the reservation
     * @return the Reservation entity
     * @throws RuntimeException if the reservation is not found
     */
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    /**
     * Securely retrieves a reservation by checking if it belongs to the requesting user.
     *
     * @param reservationId the ID of the reservation
     * @param userId the ID of the user trying to access it
     * @return the Reservation entity
     * @throws NoSuchElementException if not found
     * @throws SecurityException if the user does not own the reservation
     */
    public Reservation secureGetReservationById(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

        if (!reservation.getUser().getUserId().equals(userId)) {
            throw new SecurityException("The user trying to access the reservation isn't the one who did the reservation");
        }

        return reservation;
    }

    /**
     * Performs basic null and value checks on reservation input data.
     *
     * @param restId restaurant ID
     * @param date reservation date
     * @param startTime reservation start time
     * @param requestedSeats number of guests
     * @throws IllegalArgumentException if any validation fails
     */
    private void validateReservationRequest(Long restId, LocalDate date,
                                            LocalTime startTime, int requestedSeats) {
        if (restId == null) {
            throw new IllegalArgumentException("Restaurant id is required");
        }

        if (date == null) {
            throw new IllegalArgumentException("Reservation date is required");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("Reservation time is required");
        }

        if (requestedSeats <= 0) {
            throw new IllegalArgumentException("Requested seats must be greater than 0");
        }
    }

    /**
     * Determines if two time slots overlap.
     *
     * @param start1 start of first slot
     * @param end1 end of first slot
     * @param start2 start of second slot
     * @param end2 end of second slot
     * @return true if the slots overlap, false otherwise
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Calculates the maximum number of occupied seats at any point
     * during the requested reservation time window.
     *
     * The method creates time-based events for overlapping reservations,
     * adding guests when an overlap begins and removing them when it ends.
     * The events are processed in chronological order to determine the
     * highest number of simultaneously occupied seats.
     *
     * @param reservations the existing active reservations for the restaurant
     * @param startTime the start time of the requested reservation
     * @param endTime the end time of the requested reservation
     * @return the maximum number of seats occupied at the same time
     */
    private int calculateMaxOccupiedSeats(
            List<Reservation> reservations,
            LocalTime startTime,
            LocalTime endTime) {

        TreeMap<LocalTime, Integer> events = new TreeMap<>();

        for (Reservation res : reservations) {
            LocalTime resStart = res.getReservationTime();
            LocalTime resEnd = resStart.plusMinutes(SLOT_DURATION_MINUTES);

            if (isTimeOverlap(resStart, resEnd, startTime, endTime)) {
                LocalTime overlapStart = resStart.isAfter(startTime) ? resStart : startTime;
                LocalTime overlapEnd = resEnd.isBefore(endTime) ? resEnd : endTime;

                events.merge(overlapStart, res.getGuests(), Integer::sum);
                events.merge(overlapEnd, -res.getGuests(), Integer::sum);
            }
        }

        int currentOccupiedSeats = 0;
        int maxOccupiedSeats = 0;

        for (int change : events.values()) {
            currentOccupiedSeats += change;
            maxOccupiedSeats = Math.max(maxOccupiedSeats, currentOccupiedSeats);
        }

        return maxOccupiedSeats;
    }
}