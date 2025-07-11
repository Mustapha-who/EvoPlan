package evoplan.entities.event;

public class Reservation {
    private int idReservation;
    private int idEvent;
    private int idClient;
    private TypeStatusRes status;

    public Reservation() {}

    public Reservation(int idReservation, int idEvent, int idClient, TypeStatusRes status) {
        this.idReservation = idReservation;
        this.idEvent = idEvent;
        this.idClient = idClient;
        this.status = status;
    }

    public Reservation(int idEvent, int idClient, TypeStatusRes status) {
        this.idEvent = idEvent;
        this.idClient = idClient;
        this.status = status;
    }

    // Getters & Setters
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getIdEvent() { return idEvent; }
    public void setIdEvent(int idEvent) { this.idEvent = idEvent; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public TypeStatusRes getStatus() { return status; }
    public void setStatus(TypeStatusRes status) { this.status = status; }
}
