package evoplan.controllers.Partner;

import evoplan.entities.Contract;
import evoplan.entities.Partnership;
import evoplan.services.Partner.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;


import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import evoplan.entities.Partner;
import evoplan.entities.event.Event;
import java.io.IOException;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

public class AjouterPartnershipController {

    @FXML
    private TextField PartnerId;  // Hidden field
    @FXML
    private TextField EventId;    // Hidden field
    @FXML
    private DatePicker DateDebut;
    @FXML
    private DatePicker DateFin;
    @FXML
    private TextField Terms;

    private PartnershipService partnershipService;
    private Partner selectedPartner;
    private Event selectedEvent;
    private AnchorPane contentPane;
    private final String senderEmail = "yacineamrouche2512@gmail.com"; // Replace with your Gmail
    private final String senderPassword = "ujkd ylry kcza dvqj"; // Replace with your Gmail

    public AjouterPartnershipController() {
        partnershipService = new PartnershipService();

    }

    @FXML
    void AjouterPartnership(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        // Check if the partnership already exists
        if (partnershipService.partnershipExists(selectedPartner.getId_partner(), selectedEvent.getIdEvent())) {
            showAlert("Erreur", "Un partenariat avec cet événement existe déjà !");
            return;
        }

        // Create the partnership
        Partnership partnership = new Partnership(
                selectedPartner.getId_partner(),
                selectedEvent.getIdEvent(),
                DateDebut.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE),
                DateFin.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE),
                Terms.getText()
        );

        // Save the partnership
        partnershipService.ajouter(partnership);
        showAlert("Success", "Partnership added successfully!");

        // Generate and send the contract
        sendContractToPartner(partnership);

        // Switch back to DisplayPartnership view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/DisplayPartnership.fxml"));
            Parent partnershipPane = loader.load();

            // Get the controller and refresh the table
            DisplayPartnershipController controller = loader.getController();
            controller.refreshTable();

            // Get the parent BorderPane and set the new view
            BorderPane parent = (BorderPane) PartnerId.getScene().getRoot();
            parent.setCenter(partnershipPane);

        } catch (Exception e) {
            showAlert("Error", "Cannot load partnership page!");
            e.printStackTrace();
        }
    }
    /**
     * Sends a contract to the partner's email after creating a partnership.
     *
     * @param partnership The partnership that was created.
     */
    private void sendContractToPartner(Partnership partnership) {
        try {
            // Create a contract based on the partnership
            Contract contract = new Contract(
                    partnership.getId_partner(), // Partner ID
                    partnership.getId_event(),   // Event ID
                    partnership.getDate_debut(), // Start date
                    partnership.getDate_fin(),   // End date
                    partnership.getTerms(),      // Terms
                    "Active"                     // Default status
            );

            // Generate the PDF contract
            String pdfFilePath = "Contract_" + System.currentTimeMillis() + ".pdf"; // Use a unique name
            PDFExporter.exportToPDF(contract, pdfFilePath);

            // Send the contract via email
            String recipientEmail = selectedPartner.getEmail(); // Partner's email

            // Initialize the EmailSender service with fixed sender credentials
            EmailSender emailSender = new EmailSender(senderEmail, senderPassword);

            // Send the email
            emailSender.sendEmailWithAttachment(recipientEmail, "Contract for Partnership", "Voici votre Contract :) N'hesiter pas de notre contacter .", pdfFilePath);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send contract email.");
        }

    }



    private boolean validateInputs() {
        if (DateDebut.getValue() == null || DateFin.getValue() == null || Terms.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return false;
        }

        // New validation to check if DateDebut is before today
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


    private void switchToDisplayPartnershipScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/DisplayPartnership.fxml"));
            AnchorPane partnershipPane = loader.load();

            // Get the parent BorderPane (EventPlannerHomePage)
            BorderPane parent = (BorderPane) PartnerId.getScene().getRoot();
            parent.setCenter(partnershipPane);

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger la page des partenariats !");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initData(Partner partner, Event event) {
        this.selectedPartner = partner;
        this.selectedEvent = event;

        // Store IDs in hidden fields
        PartnerId.setText(String.valueOf(partner.getId_partner()));
        EventId.setText(String.valueOf(event.getIdEvent()));

        // Check if the partnership already exists
        if (partnershipService.partnershipExists(selectedPartner.getId_partner(), selectedEvent.getIdEvent())) {
            showAlert("Erreur", "Un partenariat avec cet événement existe déjà !");
        }
    }

    @FXML
    private void loadPartners() {
        System.out.println("Loading Partners...");
        loadPage("/EventPlannerHomePage/Partner/DisplayPartner.fxml");
    }

    private void loadPage(String fxml) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the page.");
        }
    }




}