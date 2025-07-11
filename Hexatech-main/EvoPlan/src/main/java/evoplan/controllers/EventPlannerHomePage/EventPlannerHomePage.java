package evoplan.controllers.EventPlannerHomePage;

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

public class EventPlannerHomePage {

    @FXML
    private AnchorPane contentPane; // Content area reference

    public void initialize() {
        loadPage("/EventPlannerHomePage/Dashboard.fxml"); // Load dashboard by default
    }

    private void loadPage(String fxml) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void loadDashboard() { loadPage("/EventPlannerHomePage/Dashboard.fxml"); }
    @FXML private void loadEvents() { loadPage("/EventPlannerHomePage/Event/DisplayEvents.fxml"); }
    @FXML private void loadWorkshops() { loadPage("/EventPlannerHomePage/Workshop/Workshops.fxml"); }
    @FXML private void loadPartners() { loadPage("/EventPlannerHomePage/Partner/DisplayPartner.fxml"); }

    @FXML private void loadRessources() { loadPage("/EventPlannerHomePage/ressource/AjouterRessource.fxml"); }
    @FXML private void loadFeedback() { loadPage("/EventPlannerHomePage/Feedback/EventPlannerFeedback.fxml"); }
    @FXML private void loadAPI() { loadPage("/EventPlannerHomePage/API/APIPage.fxml"); }
    @FXML private void loadClaims() { loadPage("/EventPlannerHomePage/Feedback/EventPlannerFeedback.fxml#claims"); }

  
    @FXML private void loadSettings() { loadPage("/EventPlannerHomePage/Settings.fxml"); }


    // Add this method to load partnership form
    public void loadDisplayPartnership() {
        loadPage("/EventPlannerHomePage/Partner/DisplayPartnership.fxml");
    }
    public void loadAddPartner() {
        loadPage("/EventPlannerHomePage/Partner/AjouterPartner.fxml");
    }
    public void loadDisplayPartner() {
        loadPage("/EventPlannerHomePage/Partner/DisplayPartner.fxml");
    }
    public void loadDisplayPartnershipHistory(){
        loadPage("/EventPlannerHomePage/Partner/PartnershipHistory.fxml");
    }
    public void loaddisplayContaract (){
        loadPage("/EventPlannerHomePage/Partner/DisplayContract.fxml");
    }
    @FXML
    private void loadprofile() { loadPage("/Profile.fxml"); }

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
    public void loadModifyContract (){
        loadPage("/EventPlannerHomePage/Partner/DisplayContract.fxml");
    }
}
