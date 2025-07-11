package evoplan.services.ressource;

import evoplan.entities.ressource.Ressource;
import evoplan.entities.ressource.Equipment;
import evoplan.entities.ressource.Venue;
import evoplan.main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class RessourceService {
    private Connection conn;

    public RessourceService() {
        try {
            conn = DatabaseConnection.getInstance().getCnx();
        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion : " + e.getMessage());
        }
    }

    // 🔹 Ajouter une ressource (Ressource, Equipment ou Venue)
    public void addRessource(Ressource res) {
        String query = "INSERT INTO ressource (name, type, availability) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, res.getName());
            pstmt.setString(2, res.getType());
            pstmt.setBoolean(3, res.isAvailable());
            pstmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int ressourceId = rs.getInt(1); // Récupère l'ID généré
                    res.setId(ressourceId); // Affecte l'ID à l'objet Ressource
                    System.out.println("✅ Ressource ajoutée avec l'ID : " + ressourceId);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }
    // 🔹 Lire toutes les ressources
    public List<Ressource> getAllRessources() {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT * FROM ressource";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Ressource ressource = new Ressource(
                        rs.getString("name"),       // Utilisez le constructeur sans id
                        rs.getString("type"),
                        rs.getBoolean("availability")
                );
                ressource.setId(rs.getInt("id")); // Définir l'ID manuellement
                ressources.add(ressource);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
        }
        return ressources;
    }

    // 🔹 Mettre à jour une ressource
    public void updateRessource(Ressource res) {
        String query = "UPDATE ressource SET name = ?, type = ?, availability = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, res.getName());
            pstmt.setString(2, res.getType());
            pstmt.setBoolean(3, res.isAvailable());
            pstmt.setInt(4, res.getId());
            pstmt.executeUpdate();
            System.out.println("✅ Ressource mise à jour !");

            // Mettre à jour les détails spécifiques
            if (res instanceof Equipment) {
                Equipment eq = (Equipment) res;
                String eqQuery = "UPDATE equipment SET equipmentType = ?, quantity = ? WHERE ressource_id = ?";
                try (PreparedStatement eqStmt = conn.prepareStatement(eqQuery)) {
                    eqStmt.setString(1, eq.getEquipmentType());
                    eqStmt.setInt(2, eq.getQuantity());
                    eqStmt.setInt(3, eq.getId());
                    eqStmt.executeUpdate();
                }
            } else if (res instanceof Venue) {
                Venue venue = (Venue) res;
                String venueQuery = "UPDATE venue SET address = ?, capacity = ? WHERE ressource_id = ?";
                try (PreparedStatement venueStmt = conn.prepareStatement(venueQuery)) {
                    venueStmt.setString(1, venue.getAddress());
                    venueStmt.setInt(2, venue.getCapacity());
                    venueStmt.setInt(3, venue.getId());
                    venueStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    // 🔹 Supprimer une ressource
    public void deleteRessource(int id) {
        // Supprimer d'abord les entrées associées dans Equipment et Venue
        String deleteEquipment = "DELETE FROM equipment WHERE ressource_id = ?";
        String deleteVenue = "DELETE FROM venue WHERE ressource_id = ?";
        String deleteRessource = "DELETE FROM ressource WHERE id = ?";

        try {
            // Suppression dans Equipment
            try (PreparedStatement pstmt = conn.prepareStatement(deleteEquipment)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            // Suppression dans Venue
            try (PreparedStatement pstmt = conn.prepareStatement(deleteVenue)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
            // Suppression de la ressource principale
            try (PreparedStatement pstmt = conn.prepareStatement(deleteRessource)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("✅ Ressource supprimée !");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public ByteArrayResource generateExcel() {
        List<Ressource> ressources = getAllRessources(); // Récupère les ressources depuis la BD

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ressources");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Nom");
            headerRow.createCell(2).setCellValue("Type");
            headerRow.createCell(3).setCellValue("Disponibilité");

            int rowNum = 1;
            for (Ressource res : ressources) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(res.getId());
                row.createCell(1).setCellValue(res.getName());
                row.createCell(2).setCellValue(res.getType());
                row.createCell(3).setCellValue(res.isAvailable() ? "Disponible" : "Indisponible");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}