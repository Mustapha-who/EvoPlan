package evoplan.controllers;

import evoplan.services.user.AppSessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ClientHomePageController {
    @FXML
    private AnchorPane contentPane; // Zone de contenu

    public void initialize() {
        loadPage("/ClientHomePage/Events/DisplayEventsClient.fxml"); // Charger la page par défaut
    }

    private void loadPage(String fxml) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loadevents() {
        loadPage("/ClientHomePage/Events/DisplayEventsClient.fxml");
    }

    @FXML
    private void loadworkshops() {
        loadPage("/ClientHomePage/WorkshopFrontEnd/ChosenWorkshops.fxml"); // Load the Workshops page
    }

    @FXML
    private void loadfeedback() {
        loadPage("/ClientHomePage/Feedback/ClientFeedback.fxml");
    }

    @FXML
    private void loadreports() { /* loadPage("/EventPlannerHomePage/Partner/DisplayPartner.fxml"); */ }

    @FXML
    private void loadProfilePage() { loadPage("/Profile.fxml"); }

    @FXML
    private void loadlogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Confirmation de déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        // Boutons de confirmation
        ButtonType buttonYes = new ButtonType("Oui");
        ButtonType buttonNo = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        // Afficher l'alerte et attendre la réponse de l'utilisateur
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                // Déconnexion
                AppSessionManager.getInstance().logout();
                System.out.println("User logged out successfully.");

                // Fermer la fenêtre actuelle et aller à la page de connexion
                Stage currentStage = (Stage) contentPane.getScene().getWindow();
                navigateToPage(currentStage, "/Login.fxml");
            }
        });
    }

    private void navigateToPage(Stage currentStage, String fxmlFile) {
        try {
            // Load the desired FXML file based on the role
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Set the new scene and show it
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}