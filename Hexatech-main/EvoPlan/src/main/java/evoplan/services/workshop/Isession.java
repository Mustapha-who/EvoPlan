package evoplan.services.workshop;

import java.util.List;

public interface Isession<T> {
    void afficherSessions();
    void addSession(T t);
    void updateSession(T t);
    void deleteSession(int id);
    List<T> getAllSessions();
}
