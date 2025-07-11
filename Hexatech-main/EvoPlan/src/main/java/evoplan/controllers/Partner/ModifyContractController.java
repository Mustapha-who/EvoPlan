package evoplan.controllers.Partner;

import evoplan.entities.Contract;
import evoplan.services.Partner.ContractService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ModifyContractController {

    @FXML
    private TextField PartnershipId; // Hidden field
    @FXML
    private TextField PartnerId;     // Hidden field
    @FXML
    private DatePicker DateDebut;
    @FXML
    private DatePicker DateFin;
    @FXML
    private TextField Terms;
    @FXML
    private ComboBox<String> Status;

    private Contract contract;
    private ContractService contractService;
    private DisplayContractController displayController;

    @FXML
    public void initialize() {
        // Initialize the ComboBox with valid statuses
        Status.getItems().setAll("active", "expired", "suspended");
    }

    public void setContract(Contract contract) {
        this.contract = contract;
        populateFields();
    }

    public void setContractService(ContractService contractService) {
        this.contractService = contractService;
    }

    public void setDisplayController(DisplayContractController displayController) {
        this.displayController = displayController;
    }

    private void populateFields() {
        if (contract != null) {
            // Set hidden fields
            PartnershipId.setText(String.valueOf(contract.getId_partnership()));
            PartnerId.setText(String.valueOf(contract.getId_partner()));

            Terms.setText(contract.getTerms());
            Status.setValue(contract.getStatus());

            if (contract.getDate_debut() != null && !contract.getDate_debut().isEmpty()) {
                DateDebut.setValue(LocalDate.parse(contract.getDate_debut(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            if (contract.getDate_fin() != null && !contract.getDate_fin().isEmpty()) {
                DateFin.setValue(LocalDate.parse(contract.getDate_fin(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
    }

    @FXML
    private void updateContract() {
        if (!validateInputs()) {
            return; // Stop update if validation fails
        }

        try {
            // Update contract details
            contract.setDate_debut(DateDebut.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
            contract.setDate_fin(DateFin.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE));
            contract.setTerms(Terms.getText());
            contract.setStatus(Status.getValue());

            // Update contract in database
            contractService.modifier(contract);

            // Refresh the table in the display controller
            if (displayController != null) {
                displayController.refreshTable(); // Refresh table
                navigateBackToDisplayContract(); // Navigate back to the display contract view
            } else {
                System.out.println("Display controller is null.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            showAlert("Error", "An error occurred while updating the contract.");
        }
    }

    private boolean validateInputs() {
        if (DateDebut.getValue() == null || DateFin.getValue() == null || Terms.getText().trim().isEmpty() || Status.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return false;
        }
        if (DateDebut.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert("Erreur", "select a valid date !");
            return false;
        }

        if (DateDebut.getValue().isAfter(DateFin.getValue())) {
            showAlert("Erreur", "La date de début doit être antérieure ou égale à la date de fin !");
            return false;
        }


        if (!Terms.getText().matches("[A-Za-z0-9 .,!?()-]+")) {
            showAlert("Erreur", "Les termes du contrat ne doivent contenir que des caractères valides !");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void navigateBackToDisplayContract() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/DisplayContract.fxml"));
            Parent root = loader.load();
            BorderPane mainLayout = (BorderPane) PartnershipId.getScene().getRoot();
            mainLayout.setCenter(root); // Set the original display contract view back
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not navigate back to display contract view.");
        }
    }
}