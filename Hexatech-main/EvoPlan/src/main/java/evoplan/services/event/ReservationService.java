package evoplan.services.event;

import evoplan.entities.event.Reservation;
import evoplan.entities.event.TypeStatusRes;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationService implements IReservation<Reservation> {
    private Connection cnx;

    public ReservationService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void addReservation(Reservation reservation) {
        String countQuery = "SELECT COUNT(*) FROM reservation WHERE id_event = ?";
        String capacityQuery = "SELECT capacite FROM event WHERE id_event = ?";
        String updateEventStatus = "UPDATE event SET statut = 'COMPLET' WHERE id_event = ?";

        try (PreparedStatement countStmt = cnx.prepareStatement(countQuery);
             PreparedStatement capacityStmt = cnx.prepareStatement(capacityQuery);
             PreparedStatement updateStmt = cnx.prepareStatement(updateEventStatus)) {

            // Vérifier le nombre actuel de réservations
            countStmt.setInt(1, reservation.getIdEvent());
            ResultSet rsCount = countStmt.executeQuery();
            int currentReservations = rsCount.next() ? rsCount.getInt(1) : 0;

            // Récupérer la capacité de l'événement
            capacityStmt.setInt(1, reservation.getIdEvent());
            ResultSet rsCapacity = capacityStmt.executeQuery();
            int eventCapacity = rsCapacity.next() ? rsCapacity.getInt(1) : Integer.MAX_VALUE;

            // Vérifier si l'événement est déjà complet
            if (currentReservations == eventCapacity) {
                updateStmt.setInt(1, reservation.getIdEvent());
                updateStmt.executeUpdate();
                throw new RuntimeException("L'événement est complet !");
            }

            // Ajouter la réservation
            String insertQuery = "INSERT INTO reservation (id_client, id_event, statut) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = cnx.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, reservation.getIdClient());
                insertStmt.setInt(2, reservation.getIdEvent());
                insertStmt.setString(3, reservation.getStatus().name());
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de la réservation : " + e.getMessage());
        }
    }


    @Override
    public void updateReservation(Reservation reservation) {
        String req = "UPDATE reservation SET id_client=?, id_event=?, statut=? WHERE id_reservation=?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, reservation.getIdClient());
            stm.setInt(2, reservation.getIdEvent());
            stm.setString(3, reservation.getStatus().name());
            stm.setInt(4, reservation.getIdReservation());
            stm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la réservation : " + e.getMessage());
        }
    }

    @Override
    public void deleteReservation(int idReservation) {
        String req = "DELETE FROM reservation WHERE id_reservation = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, idReservation);
            int rowsDeleted = stm.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("✅ Réservation supprimée !");
            } else {
                System.out.println("❌ Aucune réservation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de la réservation : " + e.getMessage());
        }
    }

    @Override
    public List<Reservation> getAllReservation() {
        List<Reservation> reservations = new ArrayList<>();
        String req = "SELECT * FROM reservation";

        try (Statement stm = cnx.createStatement();
             ResultSet rs = stm.executeQuery(req)) {

            while (rs.next()) {
                Reservation r = new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getInt("id_event"),
                        rs.getInt("id_client"),
                        TypeStatusRes.valueOf(rs.getString("statut"))
                );
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des réservations : " + e.getMessage());
        }
        return reservations;
    }

    @Override
    public List<Reservation> getReservationsByEventId(int eventId) {
        List<Reservation> reservations = new ArrayList<>();
        String req = "SELECT * FROM reservation WHERE id_event = ?";

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, eventId);
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
                reservations.add(new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getInt("id_event"),
                        rs.getInt("id_client"),
                        TypeStatusRes.valueOf(rs.getString("statut"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réservations par événement : " + e.getMessage());
        }

        return reservations;
    }

    // ✅ Fonction qui compte les réservations pour un événement donné
    public int countReservationsForEvent(int eventId) {
        String req = "SELECT COUNT(*) AS total FROM reservation WHERE id_event = ?";
        int totalReservations = 0;

        try (PreparedStatement stm = cnx.prepareStatement(req)) {
            stm.setInt(1, eventId);
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
                totalReservations = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des réservations : " + e.getMessage());
        }

        return totalReservations;
    }

    // ✅ Vérifie si un client peut réserver (en comparant avec la capacité de l'événement)
    public boolean canReserve(int eventId, int capacite) {
        int nombreReservations = countReservationsForEvent(eventId);
        return nombreReservations < capacite;
    }
}
