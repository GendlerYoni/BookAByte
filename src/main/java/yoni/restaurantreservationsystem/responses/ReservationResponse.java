/**
 * Data Transfer Object representing a reservation's details for display.
 * This class is used to send booking information to the customer, combining
 * data from the Reservation entity and the associated Restaurant (such as name and image)
 * to provide a complete view for history and upcoming lists.
 */
package yoni.restaurantreservationsystem.responses;

import yoni.restaurantreservationsystem.entities.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationResponse {
    private Long reservationId;
    private Long restId;
    private String restaurantName;
    private LocalDate date;
    private LocalTime time;
    private int numOfPeople;
    private ReservationStatus status;
    private String imagePath;

    public ReservationResponse(Long reservationId, Long restId, String restaurantName,
                               LocalDate date, LocalTime time, int numOfPeople,
                               ReservationStatus status) {
        this.reservationId = reservationId;
        this.restId = restId;
        this.restaurantName = restaurantName;
        this.date = date;
        this.time = time;
        this.numOfPeople = numOfPeople;
        this.status = status;
    }

    public ReservationResponse(Long reservationId, Long restId, String restaurantName,
                               LocalDate date, LocalTime time, int numOfPeople,
                               ReservationStatus status, String imagePath) {
        this(reservationId, restId, restaurantName, date, time, numOfPeople, status);
        this.imagePath = imagePath;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getRestId() {
        return restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public int getNumOfPeople() {
        return numOfPeople;
    }

    public void setNumOfPeople(int numOfPeople) {
        this.numOfPeople = numOfPeople;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
