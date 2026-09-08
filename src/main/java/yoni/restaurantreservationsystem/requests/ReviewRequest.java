/**
 * Data Transfer Object representing a request to submit customer feedback.
 *
 * This request links the feedback to a specific experience by:
 * - Identifying the relevant reservation (reseId)
 * - Storing the customer's rating (score) and textual comments (revDescription)
 *
 * It acts as the payload for creating Review entities after a successful dining event.
 */
package yoni.restaurantreservationsystem.requests;

public class ReviewRequest {
    private Long reseId;
    private String revDescription;
    private int score;

    //Constructors
    public ReviewRequest() {}

    public ReviewRequest(Long reseId, String revDescription, int score) {
        this.reseId = reseId;
        this.revDescription = revDescription;
        this.score = score;
    }

    //Getters and Setters

    public Long getReseId() {
        return reseId;
    }

    public void setReseId(Long reseId) {
        this.reseId = reseId;
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
