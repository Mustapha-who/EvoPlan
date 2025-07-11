package evoplan.services.workshop;

import java.util.List;

public interface IreservationSessionService<T> {
    void afficherreservationSession();
    void addreservationSession(T t);
    void updatereservationSession(T t);
    void deletereservationSession(int id);
    List<T> getAllreservationSession();
}
