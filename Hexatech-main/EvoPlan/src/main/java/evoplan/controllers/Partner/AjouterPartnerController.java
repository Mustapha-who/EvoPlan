package evoplan.controllers.Partner;

import evoplan.entities.Partner;
import evoplan.entities.PartnerType;
import evoplan.services.Partner.PartnerService;
import evoplan.controllers.EventPlannerHomePage.EventPlannerHomePage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class AjouterPartnerController {

    @FXML
    private TextField Email;

    @FXML
    private TextField PhoneNumber;

    @FXML
    private ComboBox<PartnerType> TypePartner;

    @FXML
    private Button image;

    @FXML
    private ImageView displayImage;

    private File selectedImage = null;

    public void initialize() {
        TypePartner.getItems().setAll(PartnerType.values());
    }

    @FXML
    void AjouterPartner(ActionEvent event) {
        System.out.println("Adding partner..."); // Debugging statement
        if (!validateInputs()) {
            return; // Stop execution if validation fails
        }

        // Check for uniqueness
        PartnerService partnerService = new PartnerService();
        String existingField = partnerService.partnerExists(Email.getText(), PhoneNumber.getText(), selectedImage != null ? selectedImage.getName() : null);

        if (existingField != null) {
            switch (existingField) {
                case "email":
                    showAlert("Erreur", "Un partenaire avec cet e-mail existe déjà !");
                    break;
                case "phone":
                    showAlert("Erreur", "Un partenaire avec ce numéro de téléphone existe déjà !");
                    break;
                case "logo":
                    showAlert("Erreur", "Un partenaire avec ce logo existe déjà !");
                    break;
            }
            return; // Stop execution if partner already exists
        }

        String imagePath = null;
        if (selectedImage != null) {
            try {
                File destinationFile = new File("uploads/" + selectedImage.getName());
                Files.copy(selectedImage.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imagePath = destinationFile.getPath(); // Store the path for database entry
            } catch (IOException e) {
                showAlert("Error", "Failed to upload the image!");
                return;
            }
        } else {
            showAlert("Error", "Please select an image!");
            return; // Ensure we don't proceed if no image is selected
        }

        Partner p = new Partner(
                TypePartner.getValue(),
                Email.getText(),
                PhoneNumber.getText(),
                imagePath // Store the path in the Partner object
        );

        partnerService.ajouter(p);
        showAlert("Success", "Partner added successfully!");

        switchToDisplayPartner();
    }

    private boolean validateInputs() {
        if (TypePartner.getValue() == null || Email.getText().isEmpty() || PhoneNumber.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return false;
        }

        if (!isValidEmail(Email.getText())) {
            showAlert("Erreur", "Adresse e-mail invalide !");
            return false;
        }

        if (!isValidPhoneNumber(PhoneNumber.getText())) {
            showAlert("Erreur", "Le numéro de téléphone doit être exactement 8 chiffres !");
            return false;
        }

        if (selectedImage == null) {
            showAlert("Erreur", "Veuillez sélectionner une image !");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Check if the phone number is exactly 8 digits
        return Pattern.matches("^\\d{8}$", phoneNumber);
    }

    private void switchToDisplayPartner() {
        try {
            // Get the current stage
            Stage stage = (Stage) Email.getScene().getWindow();

            // Load the main home page again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the main controller
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Display Partner" view inside the main page
            homePageController.loadDisplayPartner();

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page DisplayPartner !");
            e.printStackTrace();
        }
    }


    @FXML
    void UPimage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Logo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        selectedImage = fileChooser.showOpenDialog((Stage) image.getScene().getWindow());
        if (selectedImage != null) {
            Image img = new Image(selectedImage.toURI().toString());
            displayImage.setImage(img);
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


            // Get the parent of the current scene (which should be the main layout)
            BorderPane mainLayout = (BorderPane) stage.getScene().getRoot();

            // Set the new content in the center of the main layout
            // mainLayout.setCenter(partnerPane);
        } catch (Exception e) {

            showAlert("Error", "Cannot load previous page!");
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
}

