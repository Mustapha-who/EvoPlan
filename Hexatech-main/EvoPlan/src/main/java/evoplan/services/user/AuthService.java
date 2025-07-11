package evoplan.services.user;

import evoplan.entities.user.User;
import evoplan.main.DatabaseConnection;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    Connection cnx;

    public AuthService() {
        this.cnx = DatabaseConnection.getInstance().getCnx();
    }

    public User login(String email, String password) {
        String query = "SELECT * FROM User WHERE email = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");

                if (PasswordEncryptor.checkPassword(password, hashedPassword)) {
                    int userId = rs.getInt("id");
                    String username = rs.getString("name");
                    User user = new User(userId, email, hashedPassword, username);

                    // Determine the role from the table
                    String role = getUserRole(userId);

                    // Store user and role in session
                    AppSessionManager.getInstance().login(user, role);

                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getUserIdByEmail(String email) {
        int userId = -1; // Default value if no user is found

        String query = "SELECT id FROM User WHERE email = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userId;
    }


    public User loginWithGoogle(String email) {
        String query = "SELECT * FROM User WHERE email = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("name");
                String hashedPassword = rs.getString("password");
                User user = new User(userId, email, hashedPassword, username); // No password needed

                // Determine the role from the table
                String role = getUserRole(userId);

                // Store user and role in session
                AppSessionManager.getInstance().login(user, role);

                System.out.println("User role: " + role);
                // After Google login, check session
                User loggedInUser = AppSessionManager.getInstance().getCurrentUser();
                System.out.println("Logged in user: " + loggedInUser);

                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getUserRole(int userId) {
        // Check in the role-specific tables to determine the user's role
        String role = null;

        String[] tables = {"administrator", "client", "instructor", "eventPlanner"};
        for (String table : tables) {
            String query = "SELECT * FROM " + table + " WHERE id = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    role = table.toUpperCase(); // Return the table name as the role (ADMIN, USER, etc.)
                    break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return role;
    }
}
