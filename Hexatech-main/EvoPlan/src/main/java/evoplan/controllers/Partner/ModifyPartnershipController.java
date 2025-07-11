package evoplan.controllers.Partner;

import evoplan.entities.Partnership;
import evoplan.services.Partner.PartnershipService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModifyPartnershipController {

    @FXML
    private TextField PartnerId; // Hidden field
    @FXML
    private TextField EventId;   // Hidden field
    @FXML
    private DatePicker DateDebut;
    @FXML
    private DatePicker DateFin;
    @FXML
    private TextField Terms;

    private Partnership currentPartnership;
    private PartnershipService partnershipService;
    private DisplayPartnershipController displayController;


    public void setPartnership(Partnership partnership) {
        this.currentPartnership = partnership;

        if (currentPartnership != null) {
            // Set hidden fields
            PartnerId.setText(String.valueOf(currentPartnership.getId_partner()));
            EventId.setText(String.valueOf(currentPartnership.getId_event()));

            Terms.setText(currentPartnership.getTerms());

            if (currentPartnership.getDate_debut() != null && !currentPartnership.getDate_debut().isEmpty()) {
                DateDebut.setValue(LocalDate.parse(currentPartnership.getDate_debut()));
            }
            if (currentPartnership.getDate_fin() != null && !currentPartnership.getDate_fin().isEmpty()) {
                DateFin.setValue(LocalDate.parse(currentPartnership.getDate_fin()));
            }
        } else {
            System.out.println("Error: Partnership is null");
        }
    }

    public void setPartnershipService(PartnershipService service) {
        this.partnershipService = service;
    }

    public void setDisplayController(DisplayPartnershipController controller) {
        this.displayController = controller;
    }

    @FXML
    void ModifierPartnership(ActionEvent event) {
        if (!validateInputs()) {
            return; // Stop execution if validation fails
        }

        // Update existing partnership object instead of creating a new one
        currentPartnership.setDate_debut(DateDebut.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
        currentPartnership.setDate_fin(DateFin.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
        currentPartnership.setTerms(Terms.getText());

        partnershipService.updatePartnership(currentPartnership); // Update in database
        showAlert("Succès", "Partenariat modifié avec succès !");

        if (displayController != null) {
            displayController.refreshTable(); // Refresh table
        }

        closeWindow(); // Close modify window instead of creating a new scene
    }

    private boolean validateInputs() {
        if (Terms.getText().isEmpty() || DateDebut.getValue() == null || DateFin.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return false;
        }
        if (DateDebut.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert("Erreur", "select a valid date !");
            return false;
        }

        if (DateDebut.getValue().isAfter(DateFin.getValue())) {
            showAlert("Erreur", "La date de début doit être antérieure à la date de fin !");
            return false;
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) Terms.getScene().getWindow();
        stage.close();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
