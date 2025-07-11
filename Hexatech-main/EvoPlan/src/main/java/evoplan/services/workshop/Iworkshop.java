package evoplan.services.workshop;

import java.util.List;

public interface Iworkshop<T> {
    void afficherWorkshops();
    void addWorkshop(T t);
    void updateWorkshop(T t);
    void deleteWorkshop(int id);
    List<T> getAllWorkshops();
}
