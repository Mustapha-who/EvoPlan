package evoplan.controllers;

import evoplan.entities.event.Reservation;
import evoplan.entities.event.TypeStatusRes;
import evoplan.services.event.ReservationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateReservationController {


    @FXML
    private TextField idClientField;

    @FXML
    private TextField idEventField;

    @FXML
    private ComboBox<String> statusComboBox;



    private Reservation reservation;
    private final ReservationService reservationService = new ReservationService();
    private DisplayReservationController parentController;

    public void setReservationData(Reservation reservation, DisplayReservationController parentController) {
        this.reservation = reservation;
        this.parentController = parentController;

        // Remplir les champs avec les données de la réservation
        idClientField.setText(String.valueOf(reservation.getIdClient()));
        idEventField.setText(String.valueOf(reservation.getIdEvent()));

        // Configuration du ComboBox pour le statut
        statusComboBox.getItems().clear();
        statusComboBox.getItems().addAll("CONFIRMÉE", "ANNULÉE");
        statusComboBox.setValue(reservation.getStatus().name());
    }

    @FXML
    void updateButton(ActionEvent event) {
        if (this.reservation == null) return;

        // Mise à jour des données de la réservation
        this.reservation.setIdClient(Integer.parseInt(idClientField.getText()));
        this.reservation.setIdEvent(Integer.parseInt(idEventField.getText()));
        this.reservation.setStatus(TypeStatusRes.valueOf(statusComboBox.getValue()));

        // Appel du service pour mettre à jour la réservation
        reservationService.updateReservation(this.reservation);

        // Rafraîchir la table des réservations
        parentController.refreshTable();

        // Fermer la fenêtre
        ((Stage) idEventField.getScene().getWindow()).close();
    }

    @FXML
    void cancelButton(ActionEvent event) {
        // Fermer la fenêtre sans sauvegarder
        ((Stage) idEventField.getScene().getWindow()).close();
    }
}