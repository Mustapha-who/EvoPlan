package evoplan.controllers.userModule;

import evoplan.services.user.AppSessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class SideBarInst {
    public Button btnLogout;
    @FXML
    private AnchorPane contentPane; // Content area reference

    public void initialize() {
        loadPage("/Instructor.fxml"); // Load dashboard by default
    }

    private void loadPage(String fxml) {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource(fxml));
            contentPane.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void loadDashboard() { loadPage("/Instructor.fxml"); }

    @FXML private void loadProfilePage() { loadPage("/Profile.fxml"); }
    @FXML
    private void logout() {
        // Create a confirmation alert
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Logout Confirmation");
        confirmationAlert.setHeaderText("Are you sure you want to log out?");
        confirmationAlert.setContentText("Any unsaved changes will be lost.");

        // Show the alert and wait for user response
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // If the user confirms, proceed with logout
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Log out the user
            AppSessionManager.getInstance().logout();
            System.out.println("User logged out successfully.");

            // Navigate back to the login page
            navigateToPage((Stage) btnLogout.getScene().getWindow(), "/Login.fxml");
        } else {
            // User canceled the logout
            System.out.println("Logout canceled.");
        }
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
