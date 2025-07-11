package evoplan.services.event;

import java.util.List;

public interface IReservation<T> {
    void addReservation(T t);
    void updateReservation(T t);
    void deleteReservation(int id);
    List<T> getAllReservation();
    List<T> getReservationsByEventId(int eventId); // Correction : retourne une liste et non une seule réservation
}
