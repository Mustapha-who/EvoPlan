package evoplan.entities.workshop;

import java.sql.Date;
import java.util.Objects;

public class session {
    private int id_session;
    private Date date; // Change this if necessary to String for consistency
    private String dateheuredeb; // Change to String
    private String dateheurefin; // Change to String
    private int participant_count;
    private int capacity;
    private int id_workshop;

    public session() {}

    public session(int id_session, Date date, String dateheuredeb, String dateheurefin,
                    int id_workshop) {
        this.id_session = id_session;
        this.date = date;
        this.dateheuredeb = dateheuredeb;
        this.dateheurefin = dateheurefin;
        this.id_workshop = id_workshop;
    }

    public session(int id_session, Date date, String dateheuredeb, String dateheurefin,
                   int participant_count, int capacity, int id_workshop) {
        this.id_session = id_session;
        this.date = date;
        this.dateheuredeb = dateheuredeb;
        this.dateheurefin = dateheurefin;
        this.participant_count = participant_count;
        this.capacity = capacity;
        this.id_workshop = id_workshop;
    }

    public int getId_session() {
        return id_session;
    }

    public void setId_session(int id_session) {
        this.id_session = id_session;
    }

    // Getters and Setters
    public int getId_workshop() {
        return id_workshop;
    }

    public void setId_workshop(int id_workshop) {
        this.id_workshop = id_workshop;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateheuredeb() {
        return dateheuredeb;
    }

    public void setDateheuredeb(String dateheuredeb) {
        this.dateheuredeb = dateheuredeb;
    }

    public int getParticipant_count() {
        return participant_count;
    }

    public void setParticipant_count(int participant_count) {
        this.participant_count = participant_count;
    }

    public String getDateheurefin() {
        return dateheurefin;
    }

    public void setDateheurefin(String dateheurefin) {
        this.dateheurefin = dateheurefin;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        session session = (session) o;
        return id_session == session.id_session && participant_count == session.participant_count && capacity == session.capacity && Objects.equals(dateheuredeb, session.dateheuredeb) && Objects.equals(dateheurefin, session.dateheurefin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_session, dateheuredeb, dateheurefin, participant_count, capacity);
    }

    @Override
    public String toString() {
        return "session{" +
                "id_session=" + id_session +
                ", dateheuredeb='" + dateheuredeb + '\'' +
                ", dateheurefin='" + dateheurefin + '\'' +
                ", participant_count=" + participant_count +
                ", capacity=" + capacity +
                '}';
    }

}
