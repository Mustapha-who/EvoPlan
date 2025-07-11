package evoplan.controllers;

import evoplan.services.feedback.ClaimService;
import evoplan.services.feedback.FeedbackService;
import evoplan.services.event.EventService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML private Label totalEventsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label completedEventsLabel;
    @FXML private Label totalFeedbackLabel;
    @FXML private Label totalClaimsLabel;
    @FXML private TableView<?> recentEventsTable;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ClaimService claimService = new ClaimService();
    private final EventService eventService = new EventService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStatistics();
        loadRecentEvents();
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
            // Note: These methods need to be implemented in EventService
            int totalEvents = eventService.getTotalEvents();


            totalEventsLabel.setText(String.valueOf(totalEvents));


        } catch (Exception e) {
            e.printStackTrace();
            // Handle errors appropriately
            totalFeedbackLabel.setText("Error");
            totalClaimsLabel.setText("Error");
        }
    }

    private void loadRecentEvents() {
        // This will be implemented when the EventService is ready
        // It should load the most recent events into the table
    }

    // Method to refresh the dashboard
    public void refreshDashboard() {
        loadStatistics();
        loadRecentEvents();
    }
}