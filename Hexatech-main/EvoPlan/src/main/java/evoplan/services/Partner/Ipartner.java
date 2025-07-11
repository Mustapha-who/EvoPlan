package evoplan.services.Partner;

import java.util.List;

public interface Ipartner<T> {

    public void ajouter(T t);
    public void modifier(T t);
    public void supprimer(T t);
    public List<T> getall();
    public T getone();
    public List<T> getPartnersByEvent(int eventId);
}

