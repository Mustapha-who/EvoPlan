package evoplan.services.user;

import evoplan.entities.user.Instructor;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static evoplan.services.user.PasswordEncryptor.hashPassword;
import static evoplan.services.user.PasswordValidator.isValidPassword;

public class InstructorService implements IUser<Instructor> {

    Connection cnx;
    InputValidator inputValidator = new InputValidator();
    public InstructorService() {
        cnx = DatabaseConnection.instance.getCnx();
    }

    @Override
    public List<Instructor> displayUsers() {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.password, i.certification, i.is_approved FROM user u INNER JOIN instructor i ON u.id = i.id";

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Instructor instructor = new Instructor(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("certification"),
                        rs.getBoolean("is_approved")
                );
                instructor.setId(rs.getInt("id"));  // Set the user id
                instructors.add(instructor);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instructors;
    }


    @Override
    public void addUser(Instructor instructor) {
        if (!InputValidator.isValidEmail(instructor.getEmail())) {
            System.out.println("❌ Invalid email format.");
            return;
        }
        if (inputValidator.isEmailExists(instructor.getEmail())) {
            System.out.println("❌ Email Exists.");
            return;
        }

        // Validate the password before proceeding
        if (!isValidPassword(instructor.getPassword())) {
            System.out.println("❌ Invalid password format.");
            return;
        }

        try {
            // Encrypt the password before storing
            String encryptedPassword = hashPassword(instructor.getPassword());

            cnx.setAutoCommit(false);  // Start transaction

            // Insert into User table
            String userSql = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, instructor.getName());
                userStmt.setString(2, instructor.getEmail());
                userStmt.setString(3, encryptedPassword);
                userStmt.executeUpdate();

                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    instructor.setId(generatedKeys.getInt(1));  // Assign the generated ID
                }
            }

            // Insert into Instructor table
            String instructorSql = "INSERT INTO instructor (id, certification, is_approved) VALUES (?, ?, ?)";
            try (PreparedStatement instructorStmt = cnx.prepareStatement(instructorSql)) {
                instructorStmt.setInt(1, instructor.getId());
                instructorStmt.setString(2, instructor.getCertification());
                instructorStmt.setBoolean(3, instructor.isApproved());
                instructorStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Instructor added successfully.");
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
    public void updateUser(Instructor instructor) {
        try {
            cnx.setAutoCommit(false);  // Start transaction

            // Build SQL dynamically for User table
            StringBuilder userSql = new StringBuilder("UPDATE user SET ");
            List<Object> params = new ArrayList<>();

            if (instructor.getName() != null && !instructor.getName().isEmpty()) {
                userSql.append("name = ?, ");
                params.add(instructor.getName());
            }

            if (instructor.getEmail() != null && !instructor.getEmail().isEmpty()) {
                userSql.append("email = ?, ");
                params.add(instructor.getEmail());
            }

            if (instructor.getPassword() != null && !instructor.getPassword().isEmpty()) {
                if (isValidPassword(instructor.getPassword())) {
                    String hashedPassword = hashPassword(instructor.getPassword());
                    userSql.append("password = ?, ");
                    params.add(hashedPassword);
                } else {
                    System.out.println("❌ Password is invalid.");
                    return;
                }
            }

            if (!params.isEmpty()) {
                userSql.setLength(userSql.length() - 2);
                userSql.append(" WHERE id = ?");
                params.add(instructor.getId());

                try (PreparedStatement userStmt = cnx.prepareStatement(userSql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        userStmt.setObject(i + 1, params.get(i));
                    }
                    userStmt.executeUpdate();
                }
            }

            // Build SQL dynamically for Instructor table
            StringBuilder instructorSql = new StringBuilder("UPDATE instructor SET ");
            List<Object> instructorParams = new ArrayList<>();

            if (instructor.getCertification() != null) {
                instructorSql.append("certification = ?, ");
                instructorParams.add(instructor.getCertification());
            }

            if (instructor.isApproved() != false) { // Only update if explicitly true
                instructorSql.append("is_approved = ?, ");
                instructorParams.add(instructor.isApproved());
            }

            if (!instructorParams.isEmpty()) {
                instructorSql.setLength(instructorSql.length() - 2);
                instructorSql.append(" WHERE id = ?");
                instructorParams.add(instructor.getId());

                try (PreparedStatement instructorStmt = cnx.prepareStatement(instructorSql.toString())) {
                    for (int i = 0; i < instructorParams.size(); i++) {
                        instructorStmt.setObject(i + 1, instructorParams.get(i));
                    }
                    instructorStmt.executeUpdate();
                }
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Instructor updated successfully.");
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

            // Delete from Instructor table
            String instructorSql = "DELETE FROM instructor WHERE id = ?";
            try (PreparedStatement instructorStmt = cnx.prepareStatement(instructorSql)) {
                instructorStmt.setInt(1, id);
                instructorStmt.executeUpdate();
            }

            // Delete from User table
            String userSql = "DELETE FROM user WHERE id = ?";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql)) {
                userStmt.setInt(1, id);
                userStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ Instructor deleted successfully.");
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
    public Instructor getUser(int id) {
        Instructor instructor = null;
        try {
            String sql = "SELECT u.id, u.name, u.email, u.password, i.certification, i.is_approved " +
                    "FROM user u JOIN instructor i ON u.id = i.id WHERE u.id = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    instructor = new Instructor(
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("name"),
                            rs.getString("certification"),
                            rs.getBoolean("is_approved")
                    );
                    instructor.setId(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instructor;
    }
}
