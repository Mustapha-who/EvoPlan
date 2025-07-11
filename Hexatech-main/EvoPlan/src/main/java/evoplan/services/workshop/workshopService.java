package evoplan.services.workshop;
import evoplan.entities.event.Event;
import evoplan.entities.event.TypeStatus;
import evoplan.entities.user.Instructor;
import evoplan.entities.workshop.session;
import evoplan.main.DatabaseConnection;
import java.sql.*;
import evoplan.entities.workshop.workshop;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class workshopService implements Iworkshop<workshop> {

    Connection cnx;

    // ➤ Connect with database
    public workshopService() {
        cnx = DatabaseConnection.instance.getCnx();
        if (cnx != null) {
            System.out.println("Database connection established successfully!");
        } else {
            System.out.println("Failed to establish database connection.");
        }
    }


    @Override
    // ➤ Display all workshops data
    public void afficherWorkshops() {
        List<workshop> workshops = getAllWorkshops();  // Assuming you have a method to get all workshops
        if (workshops.isEmpty()) {
            System.out.println("No workshops found.");
        } else {
            System.out.println("List of Workshops:");
            for (int i = 0; i < workshops.size(); i++) {
                workshop w = workshops.get(i);  // Get the workshop at index i
                System.out.println("ID : " + w.getId_workshop() +
                        ", Title : " + w.getTitle() +
                        ", Date : " + w.getDate() +
                        ", Instructor ID : " + w.getInstructor() +
                        ", Id event ID : " + w.getId_event() +
                        ", Capacity : " + w.getCapacity() +
                        ", Location : " + w.getLocation() +
                        ", Description : " + w.getDescription());
            }
        }
    }





    // ➤ Search workshops by instructor
    public List<Instructor> getAllInstructors() {
        List<Instructor> instructors = new ArrayList<>();
        String req = "SELECT id,certification, is_approved FROM instructor";  // Query fetching only the necessary columns

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                // Create an Instructor object with only the required columns
                Instructor instructor = new Instructor(
                        rs.getInt("id"),
                        rs.getString("certification"),
                        rs.getBoolean("is_approved")
                );
                instructors.add(instructor);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching instructors: " + e.getMessage());
        }

        return instructors;
    }

    // Get the Id and name of the event
    public List<String> getAllEvents() {
        List<String> eventStrings = new ArrayList<>();
        String req = "SELECT id_event, nom FROM event"; // Only fetch the fields you need

        try (PreparedStatement stm = cnx.prepareStatement(req);
             ResultSet rs = stm.executeQuery()) {

            while (rs.next()) {
                int idEvent = rs.getInt("id_event");
                String nom = rs.getString("nom");
                eventStrings.add(idEvent + " - " + nom); // Format as "ID - Event Name"
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching events: " + e.getMessage());
        }

        return eventStrings;
    }


    // Get all sessions data
    public List<session> getAllSessions() {
        List<session> sessions = new ArrayList<>();
        String req = "SELECT id_session, date,dateheuredeb, dateheurefin, participant_count, capacity,id_workshop FROM session";  // Adjust columns

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
                        rs.getInt("id_workshop")

                );
                sessions.add(session);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching sessions: " + e.getMessage());
        }

        return sessions;
    }

    @Override
    // ➤ Add a new workshop
    public void addWorkshop(workshop workshop) {
        String req = "INSERT INTO workshop (title, date, enddate, instructor, id_event, capacity, location, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, workshop.getTitle());
            stm.setDate(2, new java.sql.Date(workshop.getDate().getTime())); // Start date
            stm.setDate(3, new java.sql.Date(workshop.getEnddate().getTime())); // End date
            stm.setInt(4, workshop.getInstructor());
            stm.setInt(5, workshop.getId_event());
            stm.setInt(6, workshop.getCapacity());
            stm.setString(7, workshop.getLocation());
            stm.setString(8, workshop.getDescription());

            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding the workshop: " + e.getMessage());
        }
    }

    // Get the event date so I can test with it and put in the intervalle I need in my workshop
    public LocalDate[] getEventDates(int eventId) {
        String req = "SELECT date_debut, date_fin FROM event WHERE id_event = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, eventId);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                LocalDate startDate = rs.getDate("date_debut").toLocalDate();
                LocalDate endDate = rs.getDate("date_fin").toLocalDate();
                return new LocalDate[]{startDate, endDate};
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching event dates: " + e.getMessage());
        }
        return null; // Return null if no event is found
    }


    @Override
    // ➤ Update an existing workshop
    public void updateWorkshop(workshop workshop) {
        String req = "UPDATE workshop SET title = ?, date = ?, enddate = ?, instructor = ?, id_event = ?, capacity = ?, location = ?, description = ? WHERE id_workshop = ?";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, workshop.getTitle());
            stm.setDate(2, new java.sql.Date(workshop.getDate().getTime()));
            stm.setDate(3, new java.sql.Date(workshop.getEnddate().getTime()));// Use java.sql.Date for the date
            stm.setInt(4, workshop.getInstructor()); // Use the instructor ID from the workshop object
            stm.setInt(5, workshop.getId_event());   // Use the session ID from the workshop object
            stm.setInt(6, workshop.getCapacity());
            stm.setString(7, workshop.getLocation());
            stm.setString(8, workshop.getDescription());
            stm.setInt(9, workshop.getId_workshop()); // Use the workshop ID from the workshop object

            int rowsUpdated = stm.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Workshop updated successfully!");
            } else {
                System.out.println("No workshop found with ID: " + workshop.getId_workshop());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating the workshop: " + e.getMessage());
        }
    }

    @Override
    // ➤ Delete a Workshop by ID
    public void deleteWorkshop(int id) {
        String req = "DELETE FROM workshop WHERE id_workshop = ?";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting the workshop: " + e.getMessage());
        }
    }

    @Override
    // ➤ Retrieve all Workshops
    public List<workshop> getAllWorkshops() {
        List<workshop> workshops = new ArrayList<>();
        String req = "SELECT * FROM workshop";  // Assuming you have a 'workshop' table in your database

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                workshop w = new workshop( rs.getInt("id_workshop"),
                        rs.getString("title"),
                        rs.getDate("date"),
                        rs.getDate("enddate"),
                        rs.getInt("instructor"),
                        rs.getInt("id_event"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getString("description")
                );
                workshops.add(w);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving workshops: " + e.getMessage());
        }

        return workshops;
    }

    //This method will get the "instructor" name from the table "User" since the table "User" have the name attribute
    public String getInstructorNameById(int instructorId) {
        String query = "SELECT name FROM user WHERE id = (SELECT id FROM instructor WHERE id = ?)";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, instructorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching instructor name: " + e.getMessage());
        }
        return null;
    }

    //this will get the event "name"
    public String getEventNameById(int eventId) {
        String query = "SELECT nom FROM event WHERE id_event = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching event name: " + e.getMessage());
        }
        return null;
    }

    public List<workshop> getWorkshopsByEventId(int eventId) {
        List<workshop> workshops = new ArrayList<>();
        String req = "SELECT * FROM workshop WHERE id_event = ?";  // Fetch workshops for a specific event

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, eventId);  // Set the eventId parameter
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                workshop w = new workshop(
                        rs.getInt("id_workshop"),
                        rs.getString("title"),
                        rs.getDate("date"),
                        rs.getDate("enddate"),
                        rs.getInt("instructor"),
                        rs.getInt("id_event"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getString("description")
                );
                workshops.add(w);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving workshops for event: " + e.getMessage());
        }

        return workshops;
    }

    public Event getEventById(int eventId) {
        String req = "SELECT nom, description, date_debut, date_fin, lieu FROM event WHERE id_event = ?";
        Event event = null;

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, eventId);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                event = new Event(
                        eventId, // id_event
                        rs.getString("nom"), // event name
                        rs.getString("description") // event description
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching event details: " + e.getMessage());
        }

        return event;
    }

    public String getInstructorNameById2(int instructorId) {
        String query = "SELECT u.name FROM user u JOIN instructor i ON u.id = i.id WHERE i.id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, instructorId); // Set the instructorId parameter
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name"); // Return the instructor's name
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching instructor name: " + e.getMessage());
        }
        return null; // Return null if no instructor is found
    }



    /**
     * Retrieves a workshop by its ID.
     *
     * @param workshopId The ID of the workshop to retrieve.
     * @return The workshop object, or null if not found.
     */
    public workshop getWorkshopById(int workshopId) {
        String req = "SELECT * FROM workshop WHERE id_workshop = ?";  // Query to fetch a workshop by ID

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, workshopId);  // Set the workshopId parameter
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                // Create and return a workshop object
                return new workshop(
                        rs.getInt("id_workshop"),
                        rs.getString("title"),
                        rs.getDate("date"),
                        rs.getDate("enddate"),
                        rs.getInt("instructor"),
                        rs.getInt("id_event"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getString("description")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while retrieving workshop by ID: " + e.getMessage());
        }

        return null; // Return null if no workshop is found
    }



    public int getTotalWorkshops() {
        String query = "SELECT COUNT(*) AS total_workshops FROM workshop";
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_workshops");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching total workshops: " + e.getMessage());
        }
        return 0; // Return 0 if no workshops are found
    }


    public List<Map<String, Object>> getMostPopularWorkshops() {
        List<Map<String, Object>> popularWorkshops = new ArrayList<>();
        String query = "SELECT w.id_workshop, w.title, COUNT(rs.id_participants) AS user_count " +
                "FROM workshop w " +
                "JOIN session s ON w.id_workshop = s.id_workshop " +
                "JOIN reservation_session rs ON s.id_session = rs.id_session " +
                "GROUP BY w.id_workshop, w.title " +
                "ORDER BY user_count DESC " +
                "LIMIT 5";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> workshopData = new HashMap<>();
                workshopData.put("id_workshop", rs.getInt("id_workshop"));
                workshopData.put("title", rs.getString("title"));
                workshopData.put("user_count", rs.getInt("user_count"));
                popularWorkshops.add(workshopData);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching most popular workshops: " + e.getMessage());
        }
        return popularWorkshops;
    }


    public List<Map<String, Object>> getLeastPopularWorkshops() {
        List<Map<String, Object>> leastPopularWorkshops = new ArrayList<>();
        String query = "SELECT w.id_workshop, w.title, COUNT(rs.id_participants) AS user_count " +
                "FROM workshop w " +
                "JOIN session s ON w.id_workshop = s.id_workshop " +
                "JOIN reservation_session rs ON s.id_session = rs.id_session " +
                "GROUP BY w.id_workshop, w.title " +
                "ORDER BY user_count ASC " +
                "LIMIT 5";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> workshopData = new HashMap<>();
                workshopData.put("id_workshop", rs.getInt("id_workshop"));
                workshopData.put("title", rs.getString("title"));
                workshopData.put("user_count", rs.getInt("user_count"));
                leastPopularWorkshops.add(workshopData);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching least popular workshops: " + e.getMessage());
        }
        return leastPopularWorkshops;
    }


    public List<Map<String, Object>> getWorkshopAttendanceRates() {
        List<Map<String, Object>> attendanceRates = new ArrayList<>();
        String query = "SELECT w.id_workshop, w.title, " +
                "SUM(s.participant_count) AS total_participants, " +
                "SUM(s.capacity) AS total_capacity, " +
                "(SUM(s.participant_count) * 100.0 / SUM(s.capacity)) AS attendance_rate " +
                "FROM workshop w " +
                "JOIN session s ON w.id_workshop = s.id_workshop " +
                "GROUP BY w.id_workshop, w.title";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> workshopData = new HashMap<>();
                workshopData.put("id_workshop", rs.getInt("id_workshop"));
                workshopData.put("title", rs.getString("title"));
                workshopData.put("total_participants", rs.getInt("total_participants"));
                workshopData.put("total_capacity", rs.getInt("total_capacity"));
                workshopData.put("attendance_rate", rs.getDouble("attendance_rate"));
                attendanceRates.add(workshopData);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching workshop attendance rates: " + e.getMessage());
        }
        return attendanceRates;
    }



    public Map<String, Integer> getWorkshopsByLocation() {
        Map<String, Integer> workshopsByLocation = new HashMap<>();
        String query = "SELECT location, COUNT(*) AS workshop_count FROM workshop GROUP BY location";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String location = rs.getString("location");
                int count = rs.getInt("workshop_count");
                workshopsByLocation.put(location, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching workshops by location: " + e.getMessage());
        }
        return workshopsByLocation;
    }

    public List<workshop> getWorkshopsByInstructorId(int instructorId) {
        List<workshop> workshops = new ArrayList<>();
        String query = "SELECT w.id_workshop, w.title, w.date, w.enddate, w.instructor, w.id_event, w.capacity, w.location, w.description " +
                "FROM workshop w " +
                "JOIN instructor i ON w.instructor = i.id " +
                "WHERE i.id = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, instructorId); // Set the instructor ID parameter
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                workshop w = new workshop(
                        rs.getInt("id_workshop"),
                        rs.getString("title"),
                        rs.getDate("date"),
                        rs.getDate("enddate"),
                        rs.getInt("instructor"),
                        rs.getInt("id_event"),
                        rs.getInt("capacity"),
                        rs.getString("location"),
                        rs.getString("description")
                );
                workshops.add(w);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching workshops by instructor ID: " + e.getMessage());
        }

        return workshops;
    }


}
