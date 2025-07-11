package evoplan.services.user;

import evoplan.entities.user.Administrator;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static evoplan.services.user.PasswordEncryptor.hashPassword;
import static evoplan.services.user.PasswordValidator.isValidPassword;

public class AdministratorService implements IUser<Administrator> {

    Connection cnx;

    public AdministratorService() {
        cnx = DatabaseConnection.instance.getCnx();
    }

    @Override
    public List<Administrator> displayUsers() {
        List<Administrator> administrators = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password FROM user u INNER JOIN administrator a ON u.id = a.id";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Administrator admin = new Administrator(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("name")
                );
                admin.setId(rs.getInt("id"));
                administrators.add(admin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return administrators;
    }

    @Override
    public void addUser(Administrator administrator) {
        if (!InputValidator.isValidEmail(administrator.getEmail())) {
            System.out.println("❌ Invalid email format.");
            return;
        }

        // Validate the password before proceeding
        if (!isValidPassword(administrator.getPassword())) {
            System.out.println("❌ Invalid password format.");
            return;
        }

        try {
            // Encrypt the password before storing
            String encryptedPassword = hashPassword(administrator.getPassword());

            cnx.setAutoCommit(false);  // Start transaction

            // Insert into User table
            String userSql = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, administrator.getName());
                userStmt.setString(2, administrator.getEmail());
                userStmt.setString(3, encryptedPassword);
                userStmt.executeUpdate();

                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    administrator.setId(generatedKeys.getInt(1));  // Assign the generated ID
                }
            }

            // Insert into Administrator table
            String adminSql = "INSERT INTO administrator (id) VALUES (?)";
            try (PreparedStatement adminStmt = cnx.prepareStatement(adminSql)) {
                adminStmt.setInt(1, administrator.getId());
                adminStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Administrator added successfully.");
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
    public void updateUser(Administrator administrator) {
        try {
            cnx.setAutoCommit(false);  // Start transaction

            // Start building the SQL for the User table
            StringBuilder userSql = new StringBuilder("UPDATE user SET ");
            List<Object> params = new ArrayList<>();

            // Check if name is provided
            if (administrator.getName() != null && !administrator.getName().isEmpty()) {
                userSql.append("name = ?, ");
                params.add(administrator.getName());
            }

            // Check if email is provided
            if (administrator.getEmail() != null && !administrator.getEmail().isEmpty()) {
                userSql.append("email = ?, ");
                params.add(administrator.getEmail());
            }

            // Check if password is provided
            if (administrator.getPassword() != null && !administrator.getPassword().isEmpty()) {
                if (isValidPassword(administrator.getPassword())) {
                    String hashedPassword = hashPassword(administrator.getPassword());
                    userSql.append("password = ?, ");
                    params.add(hashedPassword);
                } else {
                    System.out.println("❌ Password is invalid.");
                    return;
                }
            }

            // Ensure at least one field is updated
            if (params.isEmpty()) {
                System.out.println("⚠️ No fields provided for update.");
                return;
            }

            // Remove the last comma and space from the SQL string
            userSql.setLength(userSql.length() - 2);
            userSql.append(" WHERE id = ?");
            params.add(administrator.getId());

            // Execute the update for the User table
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    userStmt.setObject(i + 1, params.get(i));
                }
                userStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Administrator updated successfully.");
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

            // Delete from Administrator table
            String adminSql = "DELETE FROM administrator WHERE id = ?";
            try (PreparedStatement adminStmt = cnx.prepareStatement(adminSql)) {
                adminStmt.setInt(1, id);
                adminStmt.executeUpdate();
            }

            // Delete from User table
            String userSql = "DELETE FROM user WHERE id = ?";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql)) {
                userStmt.setInt(1, id);
                userStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Administrator deleted successfully.");
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
    public Administrator getUser(int id) {
        Administrator administrator = null;
        try {
            String sql = "SELECT u.id, u.name, u.email, u.password " +
                    "FROM user u JOIN administrator a ON u.id = a.id WHERE u.id = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    administrator = new Administrator(
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("name")
                    );
                    administrator.setId(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return administrator;
    }
}
