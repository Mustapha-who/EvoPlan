package evoplan.controllers;

import evoplan.services.event.StatsService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.geometry.Side;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

public class StatsController {

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private PieChart pieChart;

    @FXML
    private ComboBox<String> periodComboBox;

    @FXML
    private Button refreshButton;

    @FXML
    private Label conversionRateLabel;

    @FXML
    private Label totalVisitsLabel;

    @FXML
    private Label totalReservationsLabel;

    @FXML
    private Button exportExcelButton;

    private final StatsService statsService = new StatsService();
    private int selectedEventId;
    private final DecimalFormat df = new DecimalFormat("#.##");

    @FXML
    public void initialize() {
        periodComboBox.setItems(FXCollections.observableArrayList(
                "Dernière semaine",
                "Dernier mois",
                "3 derniers mois"
        ));
        periodComboBox.setValue("Dernière semaine");

        lineChart.setTitle("Évolution des Visites et Réservations");
        lineChart.setAnimated(true);
        lineChart.setLegendSide(Side.BOTTOM);

        pieChart.setTitle("Taux de Conversion");
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setLabelsVisible(true);

        xAxis.setLabel("Date");
        yAxis.setLabel("Nombre");

        refreshButton.setOnAction(event -> loadStatistics(periodComboBox.getValue()));
        exportExcelButton.setOnAction(event -> exportToExcel());
    }

    public void setEventId(int eventId) {
        this.selectedEventId = eventId;
        loadStatistics(periodComboBox.getValue());
    }

    private void loadStatistics(String period) {
        if (selectedEventId == 0) return;

        Map<String, Integer> visitData = statsService.getVisitsOverTime(selectedEventId, period);
        Map<String, Integer> reservationData = statsService.getReservationsFromVisits(selectedEventId, period);

        updateLineChart(visitData, reservationData);
        updatePieChart(visitData, reservationData);
        updateSummaryLabels(visitData, reservationData);
    }

    private void updateLineChart(Map<String, Integer> visitData, Map<String, Integer> reservationData) {
        lineChart.getData().clear();

        XYChart.Series<String, Number> visitSeries = generateSeries("Visites", visitData);
        XYChart.Series<String, Number> reservationSeries = generateSeries("Réservations", reservationData);

        lineChart.getData().addAll(visitSeries, reservationSeries);
    }

    private void updatePieChart(Map<String, Integer> visitData, Map<String, Integer> reservationData) {
        pieChart.getData().clear();

        int totalVisits = visitData.values().stream().mapToInt(Integer::intValue).sum();
        int totalReservations = reservationData.values().stream().mapToInt(Integer::intValue).sum();
        int visitsWithoutReservation = totalVisits - totalReservations;

        double conversionRate = totalVisits > 0 ? (double) totalReservations / totalVisits * 100 : 0;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Réservations (" + df.format(conversionRate) + "%)", totalReservations),
                new PieChart.Data("Visites sans réservation (" + df.format(100 - conversionRate) + "%)", visitsWithoutReservation)
        );

        pieChart.setData(pieChartData);
    }

    private void updateSummaryLabels(Map<String, Integer> visitData, Map<String, Integer> reservationData) {
        int totalVisits = visitData.values().stream().mapToInt(Integer::intValue).sum();
        int totalReservations = reservationData.values().stream().mapToInt(Integer::intValue).sum();
        double conversionRate = totalVisits > 0 ? (double) totalReservations / totalVisits * 100 : 0;

        totalVisitsLabel.setText("Total des visites : " + totalVisits);
        totalReservationsLabel.setText("Total des réservations : " + totalReservations);
        conversionRateLabel.setText("Taux de conversion : " + df.format(conversionRate) + "%");
    }

    private XYChart.Series<String, Number> generateSeries(String name, Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        data.forEach((date, count) -> series.getData().add(new XYChart.Data<>(date, count)));
        return series;
    }

    private void exportToExcel() {
        if (selectedEventId == 0) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
        fileChooser.setTitle("Enregistrer le fichier Excel");

        File file = fileChooser.showSaveDialog(new Stage());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Statistiques");

            // Création de l'en-tête
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "Visites", "Réservations"};

            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(style);
            }

            // Récupération des statistiques
            Map<String, Integer> visitData = statsService.getVisitsOverTime(selectedEventId, periodComboBox.getValue());
            Map<String, Integer> reservationData = statsService.getReservationsFromVisits(selectedEventId, periodComboBox.getValue());

            int rowNum = 1;
            int totalVisits = 0;
            int totalReservations = 0;
            String topVisitDay = "";
            String topReservationDay = "";
            int maxVisits = 0;
            int maxReservations = 0;

            for (String date : visitData.keySet()) {
                int visits = visitData.getOrDefault(date, 0);
                int reservations = reservationData.getOrDefault(date, 0);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(date);
                row.createCell(1).setCellValue(visits);
                row.createCell(2).setCellValue(reservations);

                totalVisits += visits;
                totalReservations += reservations;

                if (visits > maxVisits) {
                    maxVisits = visits;
                    topVisitDay = date;
                }

                if (reservations > maxReservations) {
                    maxReservations = reservations;
                    topReservationDay = date;
                }
            }

            int totalDays = visitData.size();
            double averageVisits = totalDays > 0 ? (double) totalVisits / totalDays : 0;
            double averageReservations = totalDays > 0 ? (double) totalReservations / totalDays : 0;
            double conversionRate = totalVisits > 0 ? (double) totalReservations / totalVisits * 100 : 0;

            // Ajout des statistiques générales
            int summaryRowNum = rowNum + 2;
            Row summaryRow = sheet.createRow(summaryRowNum);
            summaryRow.createCell(0).setCellValue("Statistiques Générales");

            Row totalVisitsRow = sheet.createRow(summaryRowNum + 1);
            totalVisitsRow.createCell(0).setCellValue("Total des Visites");
            totalVisitsRow.createCell(1).setCellValue(totalVisits);

            Row totalReservationsRow = sheet.createRow(summaryRowNum + 2);
            totalReservationsRow.createCell(0).setCellValue("Total des Réservations");
            totalReservationsRow.createCell(1).setCellValue(totalReservations);

            Row avgVisitsRow = sheet.createRow(summaryRowNum + 3);
            avgVisitsRow.createCell(0).setCellValue("Moyenne des Visites par Jour");
            avgVisitsRow.createCell(1).setCellValue(averageVisits);

            Row avgReservationsRow = sheet.createRow(summaryRowNum + 4);
            avgReservationsRow.createCell(0).setCellValue("Moyenne des Réservations par Jour");
            avgReservationsRow.createCell(1).setCellValue(averageReservations);

            Row topVisitDayRow = sheet.createRow(summaryRowNum + 5);
            topVisitDayRow.createCell(0).setCellValue("Jour avec le Plus de Visites");
            topVisitDayRow.createCell(1).setCellValue(topVisitDay + " (" + maxVisits + " visites)");

            Row topReservationDayRow = sheet.createRow(summaryRowNum + 6);
            topReservationDayRow.createCell(0).setCellValue("Jour avec le Plus de Réservations");
            topReservationDayRow.createCell(1).setCellValue(topReservationDay + " (" + maxReservations + " réservations)");

            Row conversionRateRow = sheet.createRow(summaryRowNum + 7);
            conversionRateRow.createCell(0).setCellValue("Taux de Conversion (%)");
            conversionRateRow.createCell(1).setCellValue(conversionRate);

            // Ajustement de la taille des colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            // Sauvegarde du fichier
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            // 🔔 Affichage de l'alerte de succès 🔔
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportation réussie");
            alert.setHeaderText(null);
            alert.setContentText("Le fichier Excel a été exporté avec succès !");
            alert.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
