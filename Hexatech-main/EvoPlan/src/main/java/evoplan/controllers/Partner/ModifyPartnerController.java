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
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public class ModifyPartnerController {

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
    private static final String UPLOAD_DIR = "uploads/";

    private Partner currentPartner;
    private PartnerService partnerService;
    private DisplayPartnerController displayController;
    private BorderPane parentBorderPane;

    private String uploadDirPath;

    public void initialize() {
        TypePartner.getItems().setAll(PartnerType.values());
    }

    public void setPartner(Partner partner) {
        this.currentPartner = partner;
        if (partner != null) {
            Email.setText(partner.getEmail());
            PhoneNumber.setText(partner.getPhone_Number());
            TypePartner.setValue(partner.getType_partner());

            if (partner.getLogo() != null) {
                File file = new File(partner.getLogo());
                if (file.exists()) {
                    displayImage.setImage(new Image(file.toURI().toString()));
                } else {
                    System.out.println("Image file not found: " + partner.getLogo());
                    showAlert("Image Error", "The selected image file does not exist.");
                }
            }
        }
    }

    public void setPartnerService(PartnerService service) {
        this.partnerService = service;
    }

    public void setDisplayController(DisplayPartnerController displayController) {
        this.displayController = displayController;
    }

    public void setParentBorderPane(BorderPane parent) {
        this.parentBorderPane = parent;
    }

    @FXML
    void ModifierPartner(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        String imagePath = (selectedImage != null) ? uploadImage(selectedImage) : currentPartner.getLogo();

        if (imagePath == null) {
            showAlert("Erreur", "Échec du téléchargement de l'image !");
            return;
        }

        Partner updatedPartner = new Partner(
                currentPartner.getId_partner(),
                TypePartner.getValue(),
                Email.getText(),
                PhoneNumber.getText(),
                imagePath
        );

        partnerService.modifier(updatedPartner);
        showAlert("Succès", "Partenaire modifié avec succès !");

        returnToDisplayPartner();
    }

    private String uploadImage(File file) {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdir();
            File destination = new File(uploadDir, file.getName());
            Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destination.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

        return true;
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return Pattern.matches("^\\d{8}$", phoneNumber);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void UPimage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImage = file;
            displayImage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    void chooseUploadDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Upload Directory");
        File selectedDirectory = directoryChooser.showDialog((Stage) image.getScene().getWindow());
        if (selectedDirectory != null) {
            uploadDirPath = selectedDirectory.getAbsolutePath();
            System.out.println("Upload directory set to: " + uploadDirPath);
        }
    }

    public void returnToDisplayPartner() {
        try {
            // Get the current stage
            Stage stage = (Stage) parentBorderPane.getScene().getWindow();

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


    public FlowPane getCardsContainer() {
        FlowPane cardsContainer = new FlowPane();
        return cardsContainer;
    }
}
