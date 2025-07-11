package evoplan.entities.feedback;

public class Feedback {
    private int id;
    private int clientId;
    private Integer eventId;
    private String comments;
    private int rating;

    // Constructor
    public Feedback(int clientId, Integer eventId, String comments, int rating) {
        this.clientId = clientId;
        this.eventId = eventId;
        this.comments = comments;
        this.rating = rating;
    }

    // Constructor with ID
    public Feedback(int id, int clientId, Integer eventId, String comments, int rating) {
        this.id = id;
        this.clientId = clientId;
        this.eventId = eventId;
        this.comments = comments;
        this.rating = rating;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Feedback{id=" + id + ", clientId=" + clientId + ", eventId=" + eventId + ", comments='" + comments + "', rating=" + rating + "}";
    }
}
