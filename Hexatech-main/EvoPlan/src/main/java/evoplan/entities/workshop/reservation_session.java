package evoplan.entities.workshop;

import java.util.Objects;

public class reservation_session {
    private int id;
    private int id_session;
    private int id_participants;
    private String name; // Add this field
    private String email; // Keep this field
    private String participantName; // New
    private String participantEmail; // New

    public reservation_session() {}

    // Constructor
    public reservation_session(int id, int id_session, int id_participants, String name, String email) {
        this.id = id;
        this.id_session = id_session;
        this.id_participants = id_participants;
        this.name = name;
        this.email = email;
    }

    public reservation_session(int id, int id_session, int id_participants) {
        this.id = id;
        this.id_session = id_session;
        this.id_participants = id_participants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getId_session() {
        return id_session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId_session(int id_session) {
        this.id_session = id_session;
    }

    public int getId_participants() {
        return id_participants;
    }

    public void setId_participants(int id_participants) {
        this.id_participants = id_participants;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    public void setParticipantEmail(String participantEmail) {
        this.participantEmail = participantEmail;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        reservation_session that = (reservation_session) o;
        return id == that.id && id_session == that.id_session && id_participants == that.id_participants && Objects.equals(name, that.name) && Objects.equals(email, that.email) && Objects.equals(participantName, that.participantName) && Objects.equals(participantEmail, that.participantEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, id_session, id_participants, name, email, participantName, participantEmail);
    }


    @Override
    public String toString() {
        return "reservation_session{" +
                "id=" + id +
                ", id_session=" + id_session +
                ", id_participants=" + id_participants +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", participantName='" + participantName + '\'' +
                ", participantEmail='" + participantEmail + '\'' +
                '}';
    }
}
