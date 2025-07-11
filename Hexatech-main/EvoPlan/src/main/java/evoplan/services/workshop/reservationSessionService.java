package evoplan.services.workshop;

import evoplan.entities.user.Client;
import evoplan.entities.user.Instructor;
import evoplan.entities.user.User;
import evoplan.entities.workshop.WorkshopSessionDTO;
import evoplan.entities.workshop.reservation_session;
import evoplan.entities.workshop.session;
import evoplan.entities.workshop.workshop;
import evoplan.main.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class reservationSessionService {


    Connection cnx;
    // ➤ Connect with database
    public reservationSessionService() {
        cnx = DatabaseConnection.instance.getCnx();
        if (cnx != null) {
            System.out.println("Database connection established successfully!");
        } else {
            System.out.println("Failed to establish database connection.");
        }
    }



    public ObservableList<reservation_session> getAllReservations(int sessionId) {
        ObservableList<reservation_session> reservations = FXCollections.observableArrayList();
        String query = "SELECT id, id_session, id_participants FROM reservation_session WHERE id_session = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, sessionId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    reservation_session reservation = new reservation_session(
                            rs.getInt("id"),
                            rs.getInt("id_session"),
                            rs.getInt("id_participants"),
                            "", // Placeholder for name
                            ""  // Placeholder for email
                    );
                    reservations.add(reservation);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching reservations: " + e.getMessage());
        }

        return reservations;
    }


    public ObservableList<String[]> getUserDataBySessionId(int sessionId) {
        ObservableList<String[]> userDataList = FXCollections.observableArrayList();
        String query = "SELECT u.name, u.email " +
                "FROM user u " +
                "JOIN reservation_session rs ON u.id = rs.id_participants " +
                "WHERE rs.id_session = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, sessionId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    userDataList.add(new String[]{name, email});
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user data: " + e.getMessage());
        }

        return userDataList;
    }





    public void updateReservationSession(reservation_session reservationSession) {
        String query = "UPDATE user SET name = ?, email = ? WHERE id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, reservationSession.getName()); // Update name
            stmt.setString(2, reservationSession.getEmail()); // Update email
            stmt.setInt(3, reservationSession.getId_participants()); // Use id_participants to identify the user

            stmt.executeUpdate(); // Execute the update query
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions (e.g., log them)
        }
    }



    // ➤ Delete a reservation_session by ID and decresce the participant count by 1
    public void deleteReservation(int id) {
        String req = "DELETE FROM reservation_session WHERE id = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);

            // Fetch the session ID for the reservation before deleting it
            int sessionId = getSessionIdForReservation(id);

            // Delete the reservation
            int rowsDeleted = stm.executeUpdate();

            // If the reservation was successfully deleted, decrement the participant count
            if (rowsDeleted > 0 && sessionId != -1) {
                decrementParticipantCount(sessionId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting the reservation_session: " + e.getMessage());
        }
    }

    /**
     * Fetches the session ID for a given reservation.
     *
     * @param reservationId The ID of the reservation.
     * @return The session ID, or -1 if not found.
     */
    private int getSessionIdForReservation(int reservationId) {
        String query = "SELECT id_session FROM reservation_session WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_session");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching session ID for reservation: " + e.getMessage());
        }
        return -1; // Return -1 if the session ID is not found
    }

    /**
     * Decrements the participant count for a session.
     *
     * @param sessionId The ID of the session.
     */
    public void decrementParticipantCount(int sessionId) {
        String query = "UPDATE session SET participant_count = participant_count - 1 WHERE id_session = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error decrementing participant count: " + e.getMessage());
            throw new RuntimeException("Error decrementing participant count: " + e.getMessage());
        }
    }


    public void addReservation(int idSession, int idParticipant) {
        String insertReservationQuery = "INSERT INTO reservation_session (id_session, id_participants) VALUES (?, ?)";
        String incrementParticipantQuery = "UPDATE session SET participant_count = participant_count + 1 WHERE id_session = ?";

        try {
            // Disable auto-commit to start a transaction
            cnx.setAutoCommit(false);

            // Insert the reservation
            try (PreparedStatement insertStmt = cnx.prepareStatement(insertReservationQuery)) {
                insertStmt.setInt(1, idSession);
                insertStmt.setInt(2, idParticipant);
                insertStmt.executeUpdate();
            }

            // Increment the participant count
            try (PreparedStatement incrementStmt = cnx.prepareStatement(incrementParticipantQuery)) {
                incrementStmt.setInt(1, idSession);
                incrementStmt.executeUpdate();
            }

            // Commit the transaction
            cnx.commit();
            System.out.println("Reservation added and participant count incremented successfully.");
        } catch (SQLException e) {
            try {
                // Rollback the transaction in case of an error
                cnx.rollback();
                System.out.println("Transaction rolled back due to error: " + e.getMessage());
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            throw new RuntimeException("Error while adding the reservation: " + e.getMessage());
        } finally {
            try {
                // Re-enable auto-commit
                cnx.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error re-enabling auto-commit: " + e.getMessage());
            }
        }
    }


        // Method to check if a user is registered for an event
        public boolean isUserRegisteredForEvent(int idClient, int idEvent) {
            String query = "SELECT COUNT(*) FROM reservation WHERE id_client = ? AND id_event = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setInt(1, idClient);
                stmt.setInt(2, idEvent);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Returns true if the user is registered
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false; // Default to false if there's an error
        }


    public boolean isClientRegisteredForSession(int sessionId, int clientId) {
        String query = "SELECT COUNT(*) FROM reservation_session WHERE id_session = ? AND id_participants = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if the client is registered
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to false if there's an error
    }


    public boolean isSessionFull(int sessionId) {
        String query = "SELECT participant_count, capacity FROM session WHERE id_session = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int participantCount = rs.getInt("participant_count");
                int capacity = rs.getInt("capacity");
                return participantCount >= capacity; // Returns true if the session is full
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Default to false if there's an error
    }


    public String[] getClientNameAndEmailById(int clientId) {
        String query = "SELECT name, email FROM user WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                return new String[]{name, email};
            }
        } catch (SQLException e) {
            System.out.println("Error fetching user data: " + e.getMessage());
        }
        return null; // Return null if no user is found
    }




    /**
     * Retrieve all session IDs for a specific user.
     *
     * @param participantId The ID of the participant (user).
     * @return A list of session IDs.
     */
    public List<Integer> getSessionIdsForUser(int participantId) {
        List<Integer> sessionIds = new ArrayList<>();

        String query = "SELECT id_session FROM reservation_session WHERE id_participants = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, participantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessionIds.add(rs.getInt("id_session"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessionIds;
    }

    /**
     * Retrieve session details for a list of session IDs.
     *
     * @param sessionIds The list of session IDs.
     * @return A list of session objects.
     */
    public List<session> getSessionDetails(List<Integer> sessionIds) {
        List<session> sessions = new ArrayList<>();

        if (sessionIds.isEmpty()) {
            return sessions; // Return empty list if no session IDs are provided
        }

        // Create a comma-separated list of session IDs for the SQL query
        String sessionIdList = sessionIds.toString().replace("[", "").replace("]", "");

        String query = "SELECT id_session, date, dateheuredeb, dateheurefin, id_workshop FROM session WHERE id_session IN (" + sessionIdList + ")";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    session session = new session(
                            rs.getInt("id_session"),
                            rs.getDate("date"),
                            rs.getString("dateheuredeb"),
                            rs.getString("dateheurefin"),
                            rs.getInt("id_workshop")
                    );
                    sessions.add(session);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }

    /**
     * Retrieve workshop details for a list of workshop IDs.
     *
     * @param workshopIds The list of workshop IDs.
     * @return A list of workshop objects.
     */
    public List<workshop> getWorkshopDetails(List<Integer> workshopIds) {
        List<workshop> workshops = new ArrayList<>();

        if (workshopIds.isEmpty()) {
            return workshops; // Return empty list if no workshop IDs are provided
        }

        // Create a comma-separated list of workshop IDs for the SQL query
        String workshopIdList = workshopIds.toString().replace("[", "").replace("]", "");

        String query = "SELECT id_workshop, title, description, location FROM workshop WHERE id_workshop IN (" + workshopIdList + ")";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    workshop workshop = new workshop(
                            rs.getInt("id_workshop"),
                            rs.getString("title"),
                            rs.getString("location"),
                            rs.getString("description")
                    );
                    workshops.add(workshop);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return workshops;
    }






    /**
     * Delete a workshop session for a specific user and decrement the participant count.
     *
     * @param sessionId    The ID of the session to delete.
     * @param participantId The ID of the participant (user).
     * @return True if the deletion was successful, false otherwise.
     */
    public boolean deleteWorkshopSession(int sessionId, int participantId) {
        String deleteReservationQuery = "DELETE FROM reservation_session WHERE id_session = ? AND id_participants = ?";
        String decrementParticipantQuery = "UPDATE session SET participant_count = participant_count - 1 WHERE id_session = ?";

        try {
            // Disable auto-commit to start a transaction
            cnx.setAutoCommit(false);

            // Step 1: Delete the reservation
            try (PreparedStatement deleteStmt = cnx.prepareStatement(deleteReservationQuery)) {
                deleteStmt.setInt(1, sessionId);
                deleteStmt.setInt(2, participantId);
                int rowsDeleted = deleteStmt.executeUpdate();

                if (rowsDeleted == 0) {
                    // No rows were deleted (reservation not found)
                    cnx.rollback(); // Rollback the transaction
                    return false;
                }
            }

            // Step 2: Decrement the participant count
            try (PreparedStatement decrementStmt = cnx.prepareStatement(decrementParticipantQuery)) {
                decrementStmt.setInt(1, sessionId);
                decrementStmt.executeUpdate();
            }

            // Commit the transaction
            cnx.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error deleting workshop session: " + e.getMessage());
            e.printStackTrace();

            // Rollback the transaction in case of an error
            try {
                cnx.rollback();
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            // Re-enable auto-commit
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error re-enabling auto-commit: " + e.getMessage());
            }
        }
    }



}
