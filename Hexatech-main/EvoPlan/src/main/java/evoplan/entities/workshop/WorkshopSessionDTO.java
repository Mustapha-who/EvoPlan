package evoplan.entities.workshop;

public class WorkshopSessionDTO {
    private String title;
    private String description;
    private String location;
    private String date;
    private String startTime;
    private String endTime;
    private int sessionId; // Add this field


    WorkshopSessionDTO(){}

    // Constructor
    public WorkshopSessionDTO(String title, String description, String location, String date, String startTime, String endTime, int sessionId) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sessionId = sessionId;
    }



    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }


    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
}
