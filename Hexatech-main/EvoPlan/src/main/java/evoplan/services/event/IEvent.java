package evoplan.services.event;
import java.util.List;

public interface IEvent<T> {
    void addEvent(T t);
    void updateEvent(T t);
    void deleteEvent(T t);
    List<T> getAllEvents();
    T getEventById(int id);

}
