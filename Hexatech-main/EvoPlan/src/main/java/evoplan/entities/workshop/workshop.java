package evoplan.entities.workshop;

import java.util.Date;
import java.util.Objects;

public class workshop {
    private int id_workshop;
    private String title;
    private Date date;
    private Date enddate;
    private int instructor;
    private int id_event;
    private int capacity;
    private String location;
    private String description;

    public workshop() {}


    public workshop(int id_workshop, String title, String location, String description) {
        this.id_workshop = id_workshop;
        this.title = title;
        this.location = location;
        this.description = description;
    }

    public workshop(int id_workshop, String title, Date date, Date enddate, int instructor, int id_event, int capacity, String location, String description) {
        this.id_workshop = id_workshop;
        this.title = title;
        this.date = date;
        this.enddate = enddate;
        this.instructor = instructor;
        this.id_event = id_event;
        this.capacity = capacity;
        this.location = location;
        this.description = description;
    }

    public int getId_workshop() {
        return id_workshop;
    }

    public void setId_workshop(int id_workshop) {
        this.id_workshop = id_workshop;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getInstructor() {
        return instructor;
    }

    public void setInstructor(int instructor) {
        this.instructor = instructor;
    }

    // Fix this method name
    public int getId_event() {
        return id_event;
    }

    // Fix this method name
    public void setId_event(int id_event) {
        this.id_event = id_event;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        workshop workshop = (workshop) o;
        return id_workshop == workshop.id_workshop && instructor == workshop.instructor && id_event == workshop.id_event && capacity == workshop.capacity && Objects.equals(title, workshop.title) && Objects.equals(date, workshop.date) && Objects.equals(location, workshop.location) && Objects.equals(description, workshop.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_workshop, title, date, instructor, id_event, capacity, location, description);
    }

    @Override
    public String toString() {
        return "workshop{" +
                "id_workshop=" + id_workshop +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", instructor=" + instructor +
                ", id_event=" + id_event +
                ", capacity=" + capacity +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}