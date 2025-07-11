package evoplan.controllers;

import evoplan.services.feedback.ClaimService;
import evoplan.services.feedback.FeedbackService;
import evoplan.services.event.EventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EventPlannerDashboardController implements Initializable {
    @FXML private Label totalEventsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label completedEventsLabel;
    @FXML private Label totalFeedbackLabel;
    @FXML private Label totalClaimsLabel;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ClaimService claimService = new ClaimService();
    private final EventService eventService = new EventService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Load feedback count
            int totalFeedback = feedbackService.getAllFeedbacks().size();
            totalFeedbackLabel.setText(String.valueOf(totalFeedback));

            // Load claims count
            int totalClaims = claimService.getAllClaims().size();
            totalClaimsLabel.setText(String.valueOf(totalClaims));

            // Load event statistics
            int totalEvents = eventService.getTotalEvents();


            totalEventsLabel.setText(String.valueOf(totalEvents));


        } catch (Exception e) {
            e.printStackTrace();
            // Handle errors appropriately
            totalFeedbackLabel.setText("Error");
            totalClaimsLabel.setText("Error");
            totalEventsLabel.setText("Error");
            upcomingEventsLabel.setText("Error");
            completedEventsLabel.setText("Error");
        }
    }

    @FXML
    private void handleManageFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Feedback/EventPlannerFeedback.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Feedback Management");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageClaims() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Feedback/EventPlannerFeedback.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Claims Management");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to refresh the dashboard
    public void refreshDashboard() {
        loadStatistics();
    }
}