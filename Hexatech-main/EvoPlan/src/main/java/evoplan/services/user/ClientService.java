package evoplan.services.user;

import evoplan.entities.user.Client;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static evoplan.services.user.PasswordEncryptor.hashPassword;
import static evoplan.services.user.PasswordValidator.isValidPassword;

public class ClientService implements IUser<Client> {

    Connection cnx;
    InputValidator inputValidator = new InputValidator();
    public ClientService() {
        cnx = DatabaseConnection.instance.getCnx();
    }

    @Override
    public List<Client> displayUsers() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password, c.phone_number FROM user u INNER JOIN client c ON u.id = c.id";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Client client = new Client(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phone_number"),
                        rs.getString("name")
                );
                client.setId(rs.getInt("id"));  // Set the user id
                clients.add(client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }


    @Override
    public void addUser(Client client) {
        if (!InputValidator.isValidEmail(client.getEmail())) {
            System.out.println("❌ Invalid email format.");
            return;
        }
        if (inputValidator.isEmailExists(client.getEmail())) {
            System.out.println("❌ Email Exists.");
            return;
        }

        // Validate the password before proceeding
        if (!PasswordValidator.isValidPassword(client.getPassword())) {
            System.out.println("❌ Invalid password format.");
            return;
        }

        try {
            // Encrypt the password before storing
            String encryptedPassword = PasswordEncryptor.hashPassword(client.getPassword());

            cnx.setAutoCommit(false);  // Start transaction

            // Insert into User table
            String userSql = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, client.getName());
                userStmt.setString(2, client.getEmail());
                userStmt.setString(3, encryptedPassword);
                userStmt.executeUpdate();

                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    client.setId(generatedKeys.getInt(1));  // Assign the generated ID
                }
            }

            // Insert into Client table
            String clientSql = "INSERT INTO client (id, phone_number) VALUES (?, ?)";
            try (PreparedStatement clientStmt = cnx.prepareStatement(clientSql)) {
                clientStmt.setInt(1, client.getId());
                clientStmt.setString(2, client.getPhoneNumber());
                clientStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Client added successfully.");
        } catch (SQLException e) {
            try {
                cnx.rollback();  // Rollback on failure
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public void updateUser(Client client) {
        try {
            cnx.setAutoCommit(false);  // Start transaction

            // Start building the SQL for the User table
            StringBuilder userSql = new StringBuilder("UPDATE user SET ");
            List<Object> params = new ArrayList<>();

            // Check if name is provided
            if (client.getName() != null && !client.getName().isEmpty()) {
                userSql.append("name = ?, ");
                params.add(client.getName());
            }

            // Check if email is provided
            if (client.getEmail() != null && !client.getEmail().isEmpty()) {
                userSql.append("email = ?, ");
                params.add(client.getEmail());
            }

            // Check if password is provided
            if (client.getPassword() != null && !client.getPassword().isEmpty()) {
                if (isValidPassword(client.getPassword())) {
                    String hashedPassword = hashPassword(client.getPassword());
                    userSql.append("password = ?, ");
                    params.add(hashedPassword);
                } else {
                    System.out.println("❌ Password is invalid.");
                    return;
                }
            }

            // Remove the last comma and space from the SQL string
            userSql.setLength(userSql.length() - 2);
            userSql.append(" WHERE id = ?");
            params.add(client.getId());

            // Execute the update for the User table
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    userStmt.setObject(i + 1, params.get(i));
                }
                userStmt.executeUpdate();
            }

            // Update Client table (phone_number)
            if (client.getPhoneNumber() != null && !client.getPhoneNumber().isEmpty()) {
                String clientSql = "UPDATE client SET phone_number = ? WHERE id = ?";
                try (PreparedStatement clientStmt = cnx.prepareStatement(clientSql)) {
                    clientStmt.setString(1, client.getPhoneNumber());
                    clientStmt.setInt(2, client.getId());
                    clientStmt.executeUpdate();
                }
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Client updated successfully.");
        } catch (SQLException e) {
            try {
                cnx.rollback();  // Rollback on failure
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    public void deleteUser(int id) {
        try {
            cnx.setAutoCommit(false);  // Start transaction

            // Delete from Client table
            String clientSql = "DELETE FROM client WHERE id = ?";
            try (PreparedStatement clientStmt = cnx.prepareStatement(clientSql)) {
                clientStmt.setInt(1, id);
                clientStmt.executeUpdate();
            }

            // Delete from User table
            String userSql = "DELETE FROM user WHERE id = ?";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql)) {
                userStmt.setInt(1, id);
                userStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction

        } catch (SQLException e) {
            try {
                cnx.rollback();  // Rollback on failure
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Client getUser(int id) {
        Client client = null;
        try {
            String sql = "SELECT u.id, u.name, u.email, u.password, c.phone_number FROM user u JOIN client c ON u.id = c.id WHERE u.id = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    client = new Client(
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("phone_number"),
                            rs.getString("name")

                    );
                    client.setId(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return client;
    }
}
