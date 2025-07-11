package evoplan.entities.feedback;

import java.util.Date;

public class Claim {

    // Enum pour les types de réclamation
    public enum ClaimType {
        EVENT_CANCELLATION("Event Cancellation"),
        SCHEDULE_CONFLICT("Schedule Conflict"),
        VENUE_ISSUE("Venue Issue"),
        INSTRUCTOR_ISSUE("Instructor Issue"),
        PAYMENT_PROBLEM("Payment Problem"),
        TECHNICAL_PROBLEM("Technical Problem"),
        SERVICE_QUALITY("Service Quality"),
        OTHER("Other");

        private final String displayName;

        ClaimType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Enum pour les statuts de réclamation
    public enum ClaimStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved"),
        CLOSED("Closed"),
        REJECTED("Rejected");

        private final String displayName;

        ClaimStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private String id;
    private String description;
    private ClaimType claimType;
    private Date creationDate;
    private ClaimStatus claimStatus;
    private int clientId;
    private Integer eventId;

    // Constructeur
    public Claim(String id, String description, ClaimType claimType, Date creationDate, ClaimStatus claimStatus, int clientId, Integer eventId) {
        this.id = id;
        this.description = description;
        this.claimType = claimType;
        this.creationDate = creationDate;
        this.claimStatus = claimStatus;
        this.clientId = clientId;
        this.eventId = eventId;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ClaimType getClaimType() {
        return claimType;
    }

    public void setClaimType(ClaimType claimType) {
        this.claimType = claimType;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
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

    @Override
    public String toString() {
        return "Claim{id='" + id + "', description='" + description + "', type=" + claimType +
                ", creationDate=" + creationDate + ", status=" + claimStatus +
                ", clientId=" + clientId + ", eventId=" + eventId + "}";
    }
}
