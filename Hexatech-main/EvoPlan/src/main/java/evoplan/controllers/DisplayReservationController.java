package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.entities.event.Reservation;
import evoplan.services.event.EventService;
import evoplan.services.event.ReservationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DisplayReservationController {

    @FXML
    private TableView<Reservation> reservationTable;

    @FXML
    private TableColumn<Reservation, Integer> idColumn;
    @FXML
    private TableColumn<Reservation, Integer> ClientColumn;
    @FXML
    private TableColumn<Reservation, Integer> eventColumn;
    @FXML
    private TableColumn<Reservation, String> statutColumn;

    @FXML
    private Button deleteButton;
    @FXML
    private Button editButton;
    @FXML
    private Label capaciteid;
    @FXML
    private Label sommeVentesLabel;
    @FXML
    private Label nombreReservationsLabel;
    @FXML
    private Label nombreVisitesLabel;

    private final ReservationService reservationService = new ReservationService();
    private final EventService eventService = new EventService();
    private ObservableList<Reservation> reservationList;
    private int currentEventId;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idReservation"));
        ClientColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        eventColumn.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        deleteButton.setOnAction(event -> deleteSelectedReservation());
        editButton.setOnAction(event -> editSelectedReservation());

        reservationTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editSelectedReservation();
            }
        });
    }

    // Setter pour récupérer l'événement sélectionné
    public void setEvent(Event event) {
        if (event != null) {
            this.currentEventId = event.getIdEvent();
            loadReservationsForEvent(currentEventId);
            updateDashboard(currentEventId);
            this.currentEventId = event.getCapacite();// ❌ On ne touche pas aux visites ici
            capaciteid.setText(String.valueOf(currentEventId));
        }
    }

    private void editSelectedReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune réservation sélectionnée",
                    "Veuillez sélectionner une réservation à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/UpdateReservation.fxml"));
            Scene scene = new Scene(loader.load());

            UpdateReservationController controller = loader.getController();
            controller.setReservationData(selectedReservation, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier la réservation");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de modification.");
        }
    }

    private void deleteSelectedReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        if (selectedReservation == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune réservation sélectionnée",
                    "Veuillez sélectionner une réservation à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la réservation");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reservationService.deleteReservation(selectedReservation.getIdReservation());
            reservationList.remove(selectedReservation);
            updateDashboard(currentEventId);
        }
    }

    public void updateDashboard(int eventId) {
        double ventes = eventService.getSommeVentes(eventId);
        int reservations = eventService.getNombreReservations(eventId);
        int visites = eventService.getNombreVisites(eventId);

        sommeVentesLabel.setText(String.format("%.2f TND", ventes));
        nombreReservationsLabel.setText(String.valueOf(reservations));
        nombreVisitesLabel.setText(String.valueOf(visites));

    }

    public void loadReservationsForEvent(int eventId) {
        List<Reservation> reservations = reservationService.getReservationsByEventId(eventId);
        reservationList = FXCollections.observableArrayList(reservations);
        reservationTable.setItems(reservationList);
    }

    public void refreshTable() {
        loadReservationsForEvent(currentEventId);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
