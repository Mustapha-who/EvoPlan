package evoplan.services.Partner;


import java.util.List;

public interface IPartnership<T> {
    void ajouter(T t);
    void modifier(T t);
    void supprimer(T t);
    List<T> getAll();
    T getOne(int id);
    /*public List<Partnership> getPartnershipsForEvent(int eventId);*/

}

