package evoplan.controllers.Ressource;

import evoplan.entities.ressource.Ressource;
import evoplan.main.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelController {

    // Méthode pour récupérer les ressources depuis la base de données
    private List<Ressource> fetchRessourcesFromDatabase() {
        List<Ressource> ressources = new ArrayList<>();

        // Requête SQL pour récupérer les ressources
        String query = "SELECT id, name, type, availability FROM ressource"; // Adaptez cette requête à votre schéma de base de données

        try (Connection conn = DatabaseConnection.getInstance().getCnx(); // Assurez-vous que cette méthode retourne une connexion valide
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Parcourir les résultats de la requête
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                boolean available = rs.getBoolean("availability");

                // Créer un objet Ressource et l'ajouter à la liste
                Ressource ressource = new Ressource(id, name, type, available);
                ressources.add(ressource);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Gérer l'exception (par exemple, logger l'erreur)
        }

        return ressources;
    }

    // Méthode pour exporter les données en Excel
    @FXML
    void onExportExcel() {
        System.out.println("Export button clicked!");

        try {
            // Générer le fichier Excel
            XSSFWorkbook workbook = new XSSFWorkbook(); // Utiliser XSSFWorkbook au lieu de Workbook

            // Récupérer les données de la base de données
            List<Ressource> ressources = fetchRessourcesFromDatabase();

            // 1. Calcul du Pourcentage de Disponibilité
            int countAvailable = (int) ressources.stream().filter(Ressource::isAvailable).count();
            double percentageAvailable = (double) countAvailable / ressources.size() * 100;

            // 2. Filtrage des Ressources par Type
            Map<String, List<Ressource>> ressourcesByType = ressources.stream()
                    .collect(Collectors.groupingBy(Ressource::getType));

            // Créer une feuille pour chaque type de ressource
            for (Map.Entry<String, List<Ressource>> entry : ressourcesByType.entrySet()) {
                String type = entry.getKey();
                List<Ressource> ressourcesOfType = entry.getValue();

                XSSFSheet typeSheet = workbook.createSheet(type); // Utiliser XSSFSheet au lieu de Sheet

                // Créer la ligne d'en-tête
                Row headerRow = typeSheet.createRow(0);
                String[] columns = {"ID", "Nom", "Type", "Disponible"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                }

                // Remplir les lignes avec les données de la base de données
                int rowNum = 1;
                for (Ressource ressource : ressourcesOfType) {
                    Row row = typeSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(ressource.getId());
                    row.createCell(1).setCellValue(ressource.getName());
                    row.createCell(2).setCellValue(ressource.getType());
                    row.createCell(3).setCellValue(ressource.isAvailable() ? "Oui" : "Non");
                }

                // Ajouter une ligne pour le pourcentage de disponibilité
                Row percentageRow = typeSheet.createRow(rowNum++);
                percentageRow.createCell(0).setCellValue("Pourcentage de disponibilité");
                percentageRow.createCell(1).setCellValue(String.format("%.2f%%", percentageAvailable));
            }

            // 3. Graphiques
            XSSFSheet chartSheet = workbook.createSheet("Graphiques"); // Utiliser XSSFSheet au lieu de Sheet

            // Créer un graphique
            XSSFDrawing drawing = chartSheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 0, 10, 20);
            XSSFChart chart = drawing.createChart(anchor);

            // Configurer les axes
            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
            leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

            // Créer les sources de données pour les axes X et Y
            XDDFNumericalDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(chartSheet, new CellRangeAddress(1, ressources.size(), 2, 2));
            XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(chartSheet, new CellRangeAddress(1, ressources.size(), 3, 3));

            // Créer les données du graphique
            XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
            XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(xs, ys);
            series.setTitle("Disponibilité", null);

            // Afficher le graphique
            chart.plot(data); // Utiliser uniquement les données du graphique

            // Ouvrir une boîte de dialogue pour choisir l'emplacement du fichier
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir un emplacement pour enregistrer le fichier Excel");
            File selectedDirectory = directoryChooser.showDialog(null);

            if (selectedDirectory != null) {
                // Enregistrer le fichier Excel dans l'emplacement choisi
                File outputFile = new File(selectedDirectory, "ressources_export.xlsx");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    workbook.write(fos);
                }

                // Afficher une boîte de dialogue pour informer l'utilisateur
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier Excel a été enregistré ici :\n" + outputFile.getAbsolutePath());
                alert.showAndWait();
            } else {
                // Afficher une boîte de dialogue si aucun emplacement n'a été choisi
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Avertissement");
                alert.setHeaderText(null);
                alert.setContentText("Aucun emplacement n'a été choisi. Le fichier n'a pas été enregistré.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();

            // Afficher une boîte de dialogue en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur s'est produite lors de la génération du fichier Excel.");
            alert.showAndWait();
        }
    }
}