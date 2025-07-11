package evoplan.services.ressource;

import evoplan.entities.ressource.Equipment;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentService {
    private Connection conn;

    public EquipmentService() {
        this.conn = DatabaseConnection.getInstance().getCnx();
    }

    // Ajouter un équipement
    public void addEquipment(Equipment equipment) {
        String queryRessource = "INSERT INTO ressource (name, type, availability) VALUES (?, ?, ?)";
        String queryEquipment = "INSERT INTO equipment (id, equipmentType, quantity, ressource_id) VALUES (?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            // Insérer dans la table ressource et récupérer l'ID généré
            int ressourceId = -1;
            try (PreparedStatement pstmtR = conn.prepareStatement(queryRessource, Statement.RETURN_GENERATED_KEYS)) {
                pstmtR.setString(1, equipment.getName());
                pstmtR.setString(2, equipment.getType());
                pstmtR.setBoolean(3, equipment.isAvailable());
                pstmtR.executeUpdate();

                try (ResultSet rs = pstmtR.getGeneratedKeys()) {
                    if (rs.next()) {
                        ressourceId = rs.getInt(1);
                    }
                }
            }

            if (ressourceId != -1) {
                // Insérer dans la table equipment
                try (PreparedStatement pstmtE = conn.prepareStatement(queryEquipment)) {
                    pstmtE.setInt(1, ressourceId);
                    pstmtE.setString(2, equipment.getEquipmentType());
                    pstmtE.setInt(3, equipment.getQuantity());
                    pstmtE.setInt(4, ressourceId);
                    pstmtE.executeUpdate();
                }

                conn.commit();
                System.out.println("✅ Équipement ajouté avec succès !");
            } else {
                conn.rollback();
                System.out.println("❌ Échec lors de l'ajout de la ressource.");
            }

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.out.println("❌ Erreur lors de l'ajout de l'équipement : " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("⚠️ Impossible de réactiver l'auto-commit.");
            }
        }
    }

    // Modifier un équipement
    public void updateEquipment(Equipment equipment) {
        String queryRessource = "UPDATE ressource SET name=?, type=?, availability=? WHERE id=?";
        String queryEquipment = "UPDATE equipment SET equipmentType=?, quantity=? WHERE id=?";

        try {
            conn.setAutoCommit(false);

            // Mettre à jour la ressource
            try (PreparedStatement pstmtR = conn.prepareStatement(queryRessource)) {
                pstmtR.setString(1, equipment.getName());
                pstmtR.setString(2, equipment.getType());
                pstmtR.setBoolean(3, equipment.isAvailable());
                pstmtR.setInt(4, equipment.getId());
                pstmtR.executeUpdate();
            }

            // Mettre à jour l'équipement
            try (PreparedStatement pstmtE = conn.prepareStatement(queryEquipment)) {
                pstmtE.setString(1, equipment.getEquipmentType());
                pstmtE.setInt(2, equipment.getQuantity());
                pstmtE.setInt(3, equipment.getId());
                pstmtE.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Équipement mis à jour avec succès !");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.out.println("❌ Erreur lors de la mise à jour de l'équipement : " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("⚠️ Impossible de réactiver l'auto-commit.");
            }
        }
    }

    // Supprimer un équipement
    public void deleteEquipment(int id) {
        String queryEquipment = "DELETE FROM equipment WHERE id=?";
        String queryRessource = "DELETE FROM ressource WHERE id=?";

        try {
            conn.setAutoCommit(false);

            // Supprimer l'équipement
            try (PreparedStatement pstmtE = conn.prepareStatement(queryEquipment)) {
                pstmtE.setInt(1, id);
                pstmtE.executeUpdate();
            }

            // Supprimer la ressource associée
            try (PreparedStatement pstmtR = conn.prepareStatement(queryRessource)) {
                pstmtR.setInt(1, id);
                pstmtR.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Équipement supprimé avec succès !");

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("❌ Erreur lors du rollback : " + ex.getMessage());
            }
            System.out.println("❌ Erreur lors de la suppression de l'équipement : " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("⚠️ Impossible de réactiver l'auto-commit.");
            }
        }
    }

    // Récupérer tous les équipements
    public List<Equipment> getAllEquipment() {
        List<Equipment> equipments = new ArrayList<>();
        String query = "SELECT e.id, r.name, r.type, r.availability, e.equipmentType, e.quantity " +
                "FROM equipment e INNER JOIN ressource r ON e.ressource_id = r.id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Equipment equipment = new Equipment(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getBoolean("availability"),
                        rs.getString("equipmentType"),
                        rs.getInt("quantity")
                );
                equipments.add(equipment);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des équipements : " + e.getMessage());
        }
        return equipments;
    }
}
