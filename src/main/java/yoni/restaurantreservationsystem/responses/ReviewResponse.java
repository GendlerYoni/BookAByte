/**
 * Data Transfer Object used for displaying customer feedback.
 * This class flattens the Review entity for the Frontend, providing the
 * reviewer's name and the date of the original reservation alongside
 * the score and description for public display.
 */
package yoni.restaurantreservationsystem.responses;

import java.time.LocalDate;

public class ReviewResponse {
    private Long revId;
    private String username;
    private int score;
    private String description;
    private LocalDate reservationDate;

    public ReviewResponse(Long revId, String username, int score, String description, LocalDate reservationDate) {
        this.revId = revId;
        this.username = username;
        this.score = score;
        this.description = description;
        this.reservationDate = reservationDate;
    }

    public Long getRevId() {
        return revId;
    }

    public void setRevId(Long revId) {
        this.revId = revId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }
}
