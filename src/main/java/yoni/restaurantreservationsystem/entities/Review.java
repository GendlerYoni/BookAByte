/**
 * Represents a review written by a user for a specific reservation.
 *
 * Each review:
 * - Is linked to a specific reservation (one-to-one relationship)
 * - Contains a textual description and a numeric score
 * - Is associated with both a user and a restaurant
 *
 * Only one review is allowed per reservation.
 */
package yoni.restaurantreservationsystem.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    private static final int MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long revId;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "restID", nullable = false)
    private Restaurant restaurant;

    @OneToOne
    @JoinColumn(name = "reservationID", nullable = false, unique = true)
    private Reservation reservation;

    @Column(length = MAX_LENGTH, nullable = false)
    private String revDescription;

    @Column(nullable = false)
    private int score;

    //Constructors

    public Review() {}

    public Review(User user, Restaurant restaurant, Reservation reservation, String revDescription, int score) {
        this.user = user;
        this.restaurant = restaurant;
        this.reservation = reservation;
        this.revDescription = revDescription;
        this.score = score;
    }

    //Getters and Setters

    public Long getRevId() {
        return revId;
    }

    public void setRevId(Long revId) {
        this.revId = revId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getRevDescription() {
        return revDescription;
    }

    public void setRevDescription(String revDescription) {
        this.revDescription = revDescription;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
