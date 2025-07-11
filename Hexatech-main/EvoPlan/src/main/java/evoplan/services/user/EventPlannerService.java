package evoplan.services.user;

import evoplan.entities.user.EventPlanner;
import evoplan.entities.user.EventPlannerModule;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static evoplan.services.user.PasswordEncryptor.hashPassword;
import static evoplan.services.user.PasswordValidator.isValidPassword;

public class EventPlannerService implements IUser<EventPlanner> {

    Connection cnx;
    InputValidator inputValidator = new InputValidator();
    public EventPlannerService() {
        cnx = DatabaseConnection.instance.getCnx();
    }

    @Override
    public List<EventPlanner> displayUsers() {
        List<EventPlanner> eventPlanners = new ArrayList<>();
        String query = "SELECT u.id, u.name, u.email, u.password, e.specialization, e.assigned_module FROM user u " +
                "JOIN eventplanner e ON u.id = e.id";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                EventPlannerModule module = (rs.getString("assigned_module") != null) ?
                        EventPlannerModule.valueOf(rs.getString("assigned_module").toUpperCase()) : null;

                EventPlanner planner = new EventPlanner(
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        module
                );
                planner.setId(rs.getInt("id"));  // Set the user id
                eventPlanners.add(planner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventPlanners;
    }



//    @Override
//    public void addUser(EventPlanner eventPlanner) {
//
//    }



    public void addUser(EventPlanner eventPlanner) {
        if (!InputValidator.isValidEmail(eventPlanner.getEmail())) {
            System.out.println("❌ Invalid email format.");
            return;
        }
        if (inputValidator.isEmailExists(eventPlanner.getEmail())) {
            System.out.println("❌ Email Exists.");
            return;
        }

        // Validate the password before proceeding
        if (!isValidPassword(eventPlanner.getPassword())) {
            System.out.println("❌ Invalid password format.");
            return;
        }

        try {
            // Encrypt the password before storing
            String encryptedPassword = hashPassword(eventPlanner.getPassword());

            cnx.setAutoCommit(false);  // Start transaction

            // Insert into User table
            String userSql = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, eventPlanner.getName());
                userStmt.setString(2, eventPlanner.getEmail());
                userStmt.setString(3, encryptedPassword);
                userStmt.executeUpdate();

                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    eventPlanner.setId(generatedKeys.getInt(1));  // Assign the generated ID
                }
            }

            // Insert into EventPlanner table
            String plannerSql = "INSERT INTO eventplanner (id, specialization, assigned_module) VALUES (?, ?, ?)";
            try (PreparedStatement plannerStmt = cnx.prepareStatement(plannerSql)) {
                plannerStmt.setInt(1, eventPlanner.getId());
                plannerStmt.setString(2, eventPlanner.getSpecialization());

                // Handle nullable module
                if (eventPlanner.getAssignedModule() != null) {
                    plannerStmt.setString(3, eventPlanner.getAssignedModule().name().toUpperCase());
                } else {
                    plannerStmt.setNull(3, Types.VARCHAR);
                }

                plannerStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ EventPlanner added successfully.");
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


    public void updateUser(EventPlanner eventPlanner) {
        try {
            cnx.setAutoCommit(false);  // Start transaction

            // Start building the SQL for the User table
            StringBuilder userSql = new StringBuilder("UPDATE user SET ");
            List<Object> params = new ArrayList<>();

            // Check if name is provided
            if (eventPlanner.getName() != null && !eventPlanner.getName().isEmpty()) {
                userSql.append("name = ?, ");
                params.add(eventPlanner.getName());
            }

            // Check if email is provided
            if (eventPlanner.getEmail() != null && !eventPlanner.getEmail().isEmpty()) {
                userSql.append("email = ?, ");
                params.add(eventPlanner.getEmail());
            }

            // Check if password is provided
            if (eventPlanner.getPassword() != null && !eventPlanner.getPassword().isEmpty()) {
                if (isValidPassword(eventPlanner.getPassword())) {
                    String hashedPassword = hashPassword(eventPlanner.getPassword());
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
            params.add(eventPlanner.getId());

            // Execute the update for the User table
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    userStmt.setObject(i + 1, params.get(i));
                }
                userStmt.executeUpdate();
            }

            // Update EventPlanner table (specialization, assigned_module)
            StringBuilder plannerSql = new StringBuilder("UPDATE eventplanner SET ");
            List<Object> plannerParams = new ArrayList<>();

            // Check if specialization is provided
            if (eventPlanner.getSpecialization() != null && !eventPlanner.getSpecialization().isEmpty()) {
                plannerSql.append("specialization = ?, ");
                plannerParams.add(eventPlanner.getSpecialization());
            }

            // Check if assigned_module is provided
            if (eventPlanner.getAssignedModule() != null) {
                plannerSql.append("assigned_module = ?, ");
                plannerParams.add(eventPlanner.getAssignedModule().name().toUpperCase());
            }

            // Remove the last comma and space from the SQL string
            if (plannerParams.size() > 0) {
                plannerSql.setLength(plannerSql.length() - 2);
                plannerSql.append(" WHERE id = ?");
                plannerParams.add(eventPlanner.getId());

                // Execute the update for the EventPlanner table
                try (PreparedStatement plannerStmt = cnx.prepareStatement(plannerSql.toString())) {
                    for (int i = 0; i < plannerParams.size(); i++) {
                        plannerStmt.setObject(i + 1, plannerParams.get(i));
                    }
                    plannerStmt.executeUpdate();
                }
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ EventPlanner updated successfully.");
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


    public void deleteUser(int id) {
        try {
            cnx.setAutoCommit(false);  // Start transaction

            // Delete from EventPlanner table
            String plannerSql = "DELETE FROM eventplanner WHERE id = ?";
            try (PreparedStatement plannerStmt = cnx.prepareStatement(plannerSql)) {
                plannerStmt.setInt(1, id);
                plannerStmt.executeUpdate();
            }

            // Delete from User table
            String userSql = "DELETE FROM user WHERE id = ?";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql)) {
                userStmt.setInt(1, id);
                userStmt.executeUpdate();
            }

            cnx.commit();  // Commit transaction
            System.out.println("✅ EventPlanner deleted successfully.");
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

    public EventPlanner getUser(int id) {
        EventPlanner eventPlanner = null;
        try {
            String userSql = "SELECT name, email, password FROM user WHERE id = ?";
            try (PreparedStatement userStmt = cnx.prepareStatement(userSql)) {
                userStmt.setInt(1, id);
                ResultSet userResult = userStmt.executeQuery();

                if (userResult.next()) {
                    // Create EventPlanner object
                    eventPlanner = new EventPlanner();
                    eventPlanner.setId(id);
                    eventPlanner.setName(userResult.getString("name"));
                    eventPlanner.setEmail(userResult.getString("email"));
                    eventPlanner.setPassword(userResult.getString("password"));

                    // Fetch EventPlanner specific data
                    String plannerSql = "SELECT specialization, assigned_module FROM eventplanner WHERE id = ?";
                    try (PreparedStatement plannerStmt = cnx.prepareStatement(plannerSql)) {
                        plannerStmt.setInt(1, id);
                        ResultSet plannerResult = plannerStmt.executeQuery();

                        if (plannerResult.next()) {
                            eventPlanner.setSpecialization(plannerResult.getString("specialization"));
                            String module = plannerResult.getString("assigned_module");
                            if (module != null) {
                                eventPlanner.setAssignedModule(EventPlannerModule.valueOf(module.toUpperCase()));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventPlanner;
    }


}
