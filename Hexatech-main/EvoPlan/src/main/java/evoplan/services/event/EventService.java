package evoplan.services.event;

import evoplan.entities.event.Event;
import evoplan.entities.event.Regions;
import evoplan.entities.event.TypeStatus;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventService implements IEvent<Event> {

    Connection cnx;
    private ReservationService reservationService;

    public EventService() {
        cnx = DatabaseConnection.getInstance().getCnx();
        reservationService = new ReservationService();
    }

    @Override
    public void addEvent(Event event) {
        String req = "INSERT INTO event (nom, description, date_debut, date_fin, lieu, capacite, prix, statut, image_event) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, event.getNom());
            stm.setString(2, event.getDescription());
            stm.setTimestamp(3, Timestamp.valueOf(event.getDateDebut()));
            stm.setTimestamp(4, Timestamp.valueOf(event.getDateFin()));
            stm.setString(5, event.getLieu().name());  // Utilisation de l'enum Regions
            stm.setInt(6, event.getCapacite());
            stm.setDouble(7, event.getPrix());
            stm.setString(8, event.getStatut().name());
            stm.setString(9, event.getImageEvent());

            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'événement : " + e.getMessage());
        }
    }

    @Override
    public void updateEvent(Event event) {
        String req = "UPDATE event SET nom = ?, description = ?, date_debut = ?, date_fin = ?, lieu = ?, capacite = ?, prix = ?, statut = ?, image_event = ? WHERE id_event = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setString(1, event.getNom());
            stm.setString(2, event.getDescription());
            stm.setTimestamp(3, Timestamp.valueOf(event.getDateDebut()));
            stm.setTimestamp(4, Timestamp.valueOf(event.getDateFin()));
            stm.setString(5, event.getLieu().name());  // Conversion enum en String
            stm.setInt(6, event.getCapacite());
            stm.setDouble(7, event.getPrix());
            stm.setString(8, event.getStatut().name());
            stm.setString(9, event.getImageEvent());
            stm.setInt(10, event.getIdEvent());

            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'événement : " + e.getMessage());
        }
    }

    @Override
    public void deleteEvent(Event event) {
        // First delete related contracts
        String deleteContracts = "DELETE FROM contract WHERE id_partnership IN (SELECT id_partnership FROM partnership WHERE id_event = ?)";
        // Then delete related partnerships
        String deletePartnerships = "DELETE FROM partnership WHERE id_event = ?";
        // Then delete related reservations
        String deleteReservations = "DELETE FROM reservation WHERE id_event = ?";
        // Finally delete the event
        String deleteEvent = "DELETE FROM event WHERE id_event = ?";

        try {
            // Start transaction
            cnx.setAutoCommit(false);

            // Delete contracts first
            PreparedStatement stmContract = cnx.prepareStatement(deleteContracts);
            stmContract.setInt(1, event.getIdEvent());
            stmContract.executeUpdate();

            // Delete partnerships
            PreparedStatement stmPartnership = cnx.prepareStatement(deletePartnerships);
            stmPartnership.setInt(1, event.getIdEvent());
            stmPartnership.executeUpdate();

            // Delete reservations
            PreparedStatement stmReservation = cnx.prepareStatement(deleteReservations);
            stmReservation.setInt(1, event.getIdEvent());
            stmReservation.executeUpdate();

            // Delete event
            PreparedStatement stmEvent = cnx.prepareStatement(deleteEvent);
            stmEvent.setInt(1, event.getIdEvent());
            stmEvent.executeUpdate();

            // Commit transaction
            cnx.commit();

        } catch (SQLException e) {
            try {
                // Rollback in case of error
                cnx.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Error during rollback: " + ex.getMessage());
            }
            throw new RuntimeException("Erreur lors de la suppression de l'événement : " + e.getMessage());
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }


    @Override
    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM event";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("id_event"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        Regions.valueOf(rs.getString("lieu")),  // Conversion String -> Enum
                        rs.getInt("capacite"),
                        rs.getDouble("prix"),
                        TypeStatus.valueOf(rs.getString("statut")),
                        rs.getString("image_event")
                );
                events.add(event);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des événements : " + e.getMessage());
        }

        return events;
    }

    @Override
    public Event getEventById(int id) {
        String req = "SELECT * FROM event WHERE id_event = ?";
        Event event = null;

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                event = new Event(
                        rs.getInt("id_event"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        Regions.valueOf(rs.getString("lieu")),  // Conversion String -> Enum
                        rs.getInt("capacite"),
                        rs.getDouble("prix"),
                        TypeStatus.valueOf(rs.getString("statut")),
                        rs.getString("image_event")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'événement par ID : " + e.getMessage());
        }

        return event;
    }

    public int getTotalEvents() {
        String query = "SELECT COUNT(*) FROM event";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total events: " + e.getMessage());
        }
        return 0;
    }

    public int getNombreReservations(int eventId) {
        int totalReservations = 0;
        String query = "SELECT COUNT(*) FROM reservation WHERE id_event = ? AND statut = 'CONFIRMÉE'";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalReservations = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalReservations;
    }

    public void incrementerVisite(int eventId) {
        String query = "UPDATE event SET nombre_visites = nombre_visites + 1 WHERE id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getNombreVisites(int eventId) {
        int nombreVisites = 0;
        String query = "SELECT nombre_visites FROM event WHERE id_event = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombreVisites = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombreVisites;
    }

    public double getSommeVentes(int eventId) {
        double sommeVentes = 0.0;
        String prixQuery = "SELECT prix FROM event WHERE id_event = ?";
        String countQuery = "SELECT COUNT(*) FROM reservation WHERE id_event = ? AND statut = 'CONFIRMÉE'";

        try (PreparedStatement prixPs = cnx.prepareStatement(prixQuery);
             PreparedStatement countPs = cnx.prepareStatement(countQuery)) {

            // Récupérer le prix de l'événement
            prixPs.setInt(1, eventId);
            ResultSet prixRs = prixPs.executeQuery();
            double prixEvent = prixRs.next() ? prixRs.getDouble(1) : 0.0;

            // Récupérer le nombre de réservations confirmées
            countPs.setInt(1, eventId);
            ResultSet countRs = countPs.executeQuery();
            int nombreReservations = countRs.next() ? countRs.getInt(1) : 0;

            // Calculer la somme totale des ventes
            sommeVentes = nombreReservations * prixEvent;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sommeVentes;
    }

    public Map<String, Integer> getVisitsByDate(int eventId) {
        Map<String, Integer> visitsByDate = new HashMap<>();
        String query = "SELECT DATE(date) as day, COUNT(*) as visits FROM event_visits WHERE event_id = ? GROUP BY day";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                visitsByDate.put(rs.getString("day"), rs.getInt("visits"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return visitsByDate;
    }

    public void updateEventStatus(int eventId) {
        Event event = findEventById(eventId); // Utilise une fonction pour récupérer l'événement
        if (event == null) return;

        int nombreReservations = reservationService.countReservationsForEvent(eventId);

        if (nombreReservations >= event.getCapacite()) {
            event.setStatut(TypeStatus.COMPLET);
            updateEvent(event); // Met à jour l'événement dans la base de données
            System.out.println("🚨 L'événement est maintenant COMPLET !");
        }
    }

    public Event findEventById(int eventId) {
        String req = "SELECT * FROM event WHERE id_event = ?";
        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, eventId);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                return new Event(
                        rs.getInt("id_event"),
                        rs.getString("nom"),
                        rs.getInt("capacite"),
                        TypeStatus.valueOf(rs.getString("statut"))// Correction ici
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'événement : " + e.getMessage());
        }
        return null;
    }
}
