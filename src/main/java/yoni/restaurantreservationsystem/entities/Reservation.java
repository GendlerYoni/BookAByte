/**
 * Represents a reservation made by a customer in a restaurant.
 *
 * Each reservation includes:
 * - The user who made the reservation
 * - The restaurant where the reservation takes place
 * - The date and time of the reservation
 * - The number of guests
 * - The current status (ACTIVE / CANCELED)
 *
 * A reservation may optionally have a Review associated with it,
 * but only one review is allowed per reservation.
 */
package yoni.restaurantreservationsystem.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reseId;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "restID", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private int guests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Review review;

    //Constructors.

    public Reservation() {}

    public Reservation(User user, Restaurant restaurant, LocalDate date,
                       LocalTime time, int guests, ReservationStatus status) {
        this.user = user;
        this.restaurant = restaurant;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.status = status;
    }

    //Getters and Setters.
    public Long getReservationId() {
        return reseId;
    }

    public void setReservationId(Long reseId) {
        this.reseId = reseId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int numOfPeople) {
        this.guests = numOfPeople;
    }

    public LocalTime getReservationTime() {
        return time;
    }

    public void setReservationTime(LocalTime reseTime) {
        this.time = reseTime;
    }

    public LocalDate getReservationDate() {
        return date;
    }

    public void setReservationDate(LocalDate date) {
        this.date = date;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getReseId() {
        return reseId;
    }

    public void setReseId(Long reseId) {
        this.reseId = reseId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime reseTime) {
        this.time = reseTime;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}