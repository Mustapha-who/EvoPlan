package evoplan.services.user;

import evoplan.entities.user.Client;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection conn;

    public UserService() {
        conn = DatabaseConnection.getInstance().getCnx();
    }

    public String getUserNameById(int userId) {
        String query = "SELECT name FROM user WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String firstName = rs.getString("name");

                return firstName;
            }
            return "Unknown User";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Unknown User";
        }
    }

    public List<String> getAllClientNames() {
        List<String> clientNames = new ArrayList<>();
        String query = "SELECT u.id, u.name FROM user u INNER JOIN client c ON u.id = c.id";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String firstName = rs.getString("name");
                clientNames.add(firstName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientNames;
    }


}