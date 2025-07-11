package evoplan.entities.user;

import java.util.Objects;

public class Client extends User {
    private String phoneNumber;


    public Client() {
    }

    public Client(int id, String email, String password,String name, String phoneNumber) {
        super(id, email, password,name);
        this.phoneNumber = phoneNumber;

    }

    public Client(String email, String password, String phoneNumber,String name) {
        super(email, password,name);
        this.phoneNumber = phoneNumber;

    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Client client = (Client) o;
        return Objects.equals(phoneNumber, client.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), phoneNumber);
    }

    @Override
    public String toString() {
        return "Client{" +
                "phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
