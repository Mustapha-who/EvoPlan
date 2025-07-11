package evoplan.services.ressource;


import evoplan.entities.ressource.Venue;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenueService {
    private Connection conn;

    public VenueService() {
        this.conn = DatabaseConnection.getInstance().getCnx();
    }

    // Ajouter une venue (Lieu)
    public void addVenue(Venue venue) {
        String queryRessource = "INSERT INTO ressource (name, type, availability) VALUES (?, ?, ?)";
        String queryVenue = "INSERT INTO venue (id, address, capacity, ressource_id) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false); // Démarrer une transaction

            // Insérer la ressource et récupérer l'ID généré
            int ressourceId = -1;
            try (PreparedStatement pstmtR = conn.prepareStatement(queryRessource, Statement.RETURN_GENERATED_KEYS)) {
                pstmtR.setString(1, venue.getName());
                pstmtR.setString(2, venue.getType());
                pstmtR.setBoolean(3, venue.isAvailable());
                pstmtR.executeUpdate();

                try (ResultSet rs = pstmtR.getGeneratedKeys()) {
                    if (rs.next()) {
                        ressourceId = rs.getInt(1);
                    }
                }
            }

            if (ressourceId != -1) {
                // Insérer dans la table venue
                try (PreparedStatement pstmtV = conn.prepareStatement(queryVenue)) {
                    pstmtV.setInt(1, ressourceId); // ID = ressource_id
                    pstmtV.setString(2, venue.getAddress());
                    pstmtV.setInt(3, venue.getCapacity());
                    pstmtV.setInt(4, ressourceId); // Clé étrangère ressource_id
                    pstmtV.executeUpdate();
                }
                conn.commit(); // Valider la transaction
                System.out.println("✅ Lieu ajouté avec succès !");
            } else {
                conn.rollback();
                System.out.println("❌ Échec lors de l'ajout de la ressource.");
            }

        } catch (SQLException e) {
            try {
                conn.rollback(); // Annuler si erreur
            } catch (SQLException ex) {
                System.out.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.out.println("❌ Erreur lors de l'ajout du lieu : " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("⚠️ Impossible de réactiver l'auto-commit.");
            }
        }
    }

    // Modifier une venue
    public void updateVenue(Venue venue) {
        String queryRessource = "UPDATE ressource SET name=?, type=?, availability=? WHERE id=?";
        String queryVenue = "UPDATE venue SET address=?, capacity=? WHERE id=?";

        try {
            conn.setAutoCommit(false);

            // Mettre à jour la ressource
            try (PreparedStatement pstmtR = conn.prepareStatement(queryRessource)) {
                pstmtR.setString(1, venue.getName());
                pstmtR.setString(2, venue.getType());
                pstmtR.setBoolean(3, venue.isAvailable());
                pstmtR.setInt(4, venue.getId());
                pstmtR.executeUpdate();
            }

            // Mettre à jour la venue
            try (PreparedStatement pstmtV = conn.prepareStatement(queryVenue)) {
                pstmtV.setString(1, venue.getAddress());
                pstmtV.setInt(2, venue.getCapacity());
                pstmtV.setInt(3, venue.getId());
                pstmtV.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Lieu mis à jour avec succès !");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.out.println("❌ Erreur lors de la mise à jour du lieu : " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("⚠️ Impossible de réactiver l'auto-commit.");
            }
        }
    }

    // Supprimer une venue
    public void deleteVenue(int id) {
        String queryVenue = "DELETE FROM venue WHERE id=?";
        String queryRessource = "DELETE FROM ressource WHERE id=?";

        try {
            conn.setAutoCommit(false);

            // Supprimer la venue
            try (PreparedStatement pstmtV = conn.prepareStatement(queryVenue)) {
                pstmtV.setInt(1, id);
                pstmtV.executeUpdate();
            }

            // Supprimer la ressource associée
            try (PreparedStatement pstmtR = conn.prepareStatement(queryRessource)) {
                pstmtR.setInt(1, id);
                pstmtR.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Lieu supprimé avec succès !");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.out.println("❌ Erreur lors de la suppression du lieu : " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("⚠️ Impossible de réactiver l'auto-commit.");
            }
        }
    }

    // Récupérer toutes les venues
    public List<Venue> getAllVenues() {
        List<Venue> venues = new ArrayList<>();
        String query = "SELECT v.id, r.name, r.type, r.availability, v.address, v.capacity " +
                "FROM venue v INNER JOIN ressource r ON v.ressource_id = r.id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Venue venue = new Venue(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getBoolean("availability"),
                        rs.getString("address"),
                        rs.getInt("capacity")
                );
                venues.add(venue);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des lieux : " + e.getMessage());
        }
        return venues;
    }
}
