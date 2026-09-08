/**
 * Data Transfer Object representing a request to book a table at a restaurant.
 *
 * Each reservation request contains:
 * - The unique identifier of the target restaurant
 * - The intended date and time slot for the reservation
 * - The total number of guests attending (guests)
 *
 * This object is the primary input for the availability-check and booking-creation logic.
 */
package yoni.restaurantreservationsystem.requests;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationRequest {

    private Long restId;
    private LocalDate date;
    private LocalTime time;
    private int guests;

    //Constructors

    public ReservationRequest() {}

    public ReservationRequest(Long restId, LocalDate date, LocalTime time, int guests) {
        this.restId = restId;
        this.date = date;
        this.time = time;
        this.guests = guests;
    }

    //Getters and Setters.
    public Long getRestId() { return restId; }
    public void setRestId(Long restId) { this.restId = restId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
}
