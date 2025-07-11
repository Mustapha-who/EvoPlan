package evoplan.entities.user;

public class Administrator extends User {

    public Administrator() {
    }

    public Administrator(int id, String email, String password,String name) {
        super(id, email, password,name);
    }

    public Administrator(String email, String password,String name) {
        super(email, password,name);
    }


}
