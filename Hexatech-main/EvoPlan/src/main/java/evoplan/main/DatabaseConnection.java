package evoplan.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    Connection cnx;



    public static DatabaseConnection instance;

    public DatabaseConnection(){

        String Url="jdbc:mysql://localhost/evoplan";
     //String Url="jdbc:mysql://localhost/evoplan";
        String Username="root";
        String Password="";

        try {
            cnx= DriverManager.getConnection(Url,Username,Password);
            System.out.println("Connexion established");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } 
    }

    public static DatabaseConnection getInstance() {
        if(instance==null){
            instance=  new DatabaseConnection();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
