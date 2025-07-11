package evoplan.services.user;

import java.util.List;

public interface IUser<T> {
    List<T> displayUsers();
    void addUser(T t);
    void updateUser(T t);
    void deleteUser(int id);
    T getUser(int id);
}
