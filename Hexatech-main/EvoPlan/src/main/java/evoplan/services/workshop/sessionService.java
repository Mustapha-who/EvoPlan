package evoplan.services.workshop;
import evoplan.entities.user.Instructor;
import evoplan.entities.workshop.session;
import evoplan.main.DatabaseConnection;
import evoplan.services.workshop.Isession;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class sessionService implements Isession<session> {

    Connection cnx;

    // ➤ Connect with database
    public sessionService() {
        cnx = DatabaseConnection.instance.getCnx();
        if (cnx != null) {
            System.out.println("Database connection established successfully!");
        } else {
            System.out.println("Failed to establish database connection.");
        }
    }

    @Override
    // ➤ Display all sessions data
    public void afficherSessions() {
        List<session> sessions = getAllSessions();
        if (sessions.isEmpty()) {
            System.out.println("No sessions found.");
        } else {
            System.out.println("List of Sessions:");
            for (session s : sessions) {
                System.out.println("ID : " + s.getId_session() +
                        ", Start Time : " + s.getDateheuredeb() +
                        ", End Time : " + s.getDateheurefin() +
                        ", Participants : " + s.getParticipant_count() +
                        ", Capacity : " + s.getCapacity());
            }
        }
    }


    public List<session> getSessionsByWorkshopId(int idWorkshop) {
        List<session> sessions = new ArrayList<>();
        String req = "SELECT id_session, date,dateheuredeb, dateheurefin, participant_count, capacity, id_workshop FROM session WHERE id_workshop = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, idWorkshop);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                session session = new session(
                        rs.getInt("id_session"),
                        rs.getDate("date"),
                        rs.getString("dateheuredeb"),
                        rs.getString("dateheurefin"),
                        rs.getInt("participant_count"),
                        rs.getInt("capacity"),
                        rs.getInt("id_workshop")
                );
                sessions.add(session);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching sessions by workshop ID: " + e.getMessage());
        }

        return sessions;
    }



    public List<session> getAllSessions() {
        List<session> sessions = new ArrayList<>();
        String req = "SELECT id_session,date, dateheuredeb, dateheurefin, participant_count, capacity, id_workshop FROM session"; // Add id_workshop to the query

        try (PreparedStatement stm = cnx.prepareStatement(req);
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                session session = new session(
                        rs.getInt("id_session"),
                        rs.getDate("date"),
                        rs.getString("dateheuredeb"),
                        rs.getString("dateheurefin"),
                        rs.getInt("participant_count"),
                        rs.getInt("capacity"),
                        rs.getInt("id_workshop") // Add this line
                );
                sessions.add(session);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching sessions: " + e.getMessage());
        }

        return sessions;
    }





    @Override
    // ➤ Add a new session
    public void addSession(session session) {
        String req = "INSERT INTO session (date,dateheuredeb, dateheurefin, participant_count, capacity,id_workshop) VALUES (?,?, ?, ?, ?,?)";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setDate(1, new java.sql.Date(session.getDate().getTime()));
            stm.setTimestamp(2, java.sql.Timestamp.valueOf(session.getDateheuredeb()));
            stm.setTimestamp(3, java.sql.Timestamp.valueOf(session.getDateheurefin()));
            stm.setInt(4, 0);
            stm.setInt(5, session.getCapacity());
            stm.setInt(6, session.getId_workshop());

            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding the session: " + e.getMessage());
        }
    }


    public int getWorkshopCapacity(int workshopId) {
        String req = "SELECT capacity FROM workshop WHERE id_workshop = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, workshopId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getInt("capacity");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching workshop capacity: " + e.getMessage());
        }
        return 0; // Return 0 if no workshop is found
    }


    public boolean isSessionOverlapping(int workshopId, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, int sessionIdToExclude) {
        String req = "SELECT * FROM session WHERE id_workshop = ? AND date = ? AND " +
                "((dateheuredeb < ? AND dateheurefin > ?) OR " + // Overlap condition
                "(dateheuredeb < ? AND dateheurefin > ?))";

        if (sessionIdToExclude != 0) { // Exclude the current session when updating
            req += " AND id_session != ?";
        }

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, workshopId);
            stm.setDate(2, Date.valueOf(sessionDate));
            stm.setTime(3, Time.valueOf(endTime)); // Check if existing session ends after new session starts
            stm.setTime(4, Time.valueOf(startTime)); // Check if existing session starts before new session ends
            stm.setTime(5, Time.valueOf(endTime)); // Check if existing session ends after new session starts
            stm.setTime(6, Time.valueOf(startTime)); // Check if existing session starts before new session ends

            if (sessionIdToExclude != 0) { // Exclude the current session when updating
                stm.setInt(7, sessionIdToExclude);
            }

            ResultSet rs = stm.executeQuery();
            return rs.next(); // If a record is found, there is an overlap
        } catch (SQLException e) {
            throw new RuntimeException("Error checking for overlapping sessions: " + e.getMessage());
        }
    }


    public int getTotalSessionCapacity(int workshopId) {
        String req = "SELECT SUM(capacity) AS total_capacity FROM session WHERE id_workshop = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, workshopId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_capacity");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching total session capacity: " + e.getMessage());
        }
        return 0; // Return 0 if no sessions are found
    }


    @Override
    // ➤ Update an existing session
    public void updateSession(session session) {
        String req = "UPDATE session SET  date = ?,dateheuredeb = ?, dateheurefin = ?, capacity = ? WHERE id_session = ?";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setDate(1, new java.sql.Date(session.getDate().getTime()));
            stm.setTimestamp(2, java.sql.Timestamp.valueOf(session.getDateheuredeb()));
            stm.setTimestamp(3, java.sql.Timestamp.valueOf(session.getDateheurefin()));
            stm.setInt(4, session.getCapacity());
            stm.setInt(5, session.getId_session());

            int rowsUpdated = stm.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Session updated successfully!");
            } else {
                System.out.println("No session found with ID: " + session.getId_session());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating the session: " + e.getMessage());
        }
    }

    // Method to retrieve workshop date range
    public LocalDate[] getWorkshopDateRange(int workshopId) {
        String req = "SELECT date, enddate FROM workshop WHERE id_workshop  = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, workshopId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                LocalDate startDate = rs.getDate("date").toLocalDate();
                LocalDate endDate = rs.getDate("enddate").toLocalDate();
                return new LocalDate[]{startDate, endDate};
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching workshop date range: " + e.getMessage());
        }
        return null; // Return null if no workshop is found
    }




    @Override
    // ➤ Delete a session by ID
    public void deleteSession(int id) {
        String req = "DELETE FROM session WHERE id_session = ?";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting the session: " + e.getMessage());
        }
    }
}
