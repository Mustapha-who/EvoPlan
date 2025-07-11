package evoplan.controllers.feedback;

import evoplan.services.feedback.FeedbackService;
import evoplan.entities.feedback.Feedback;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

public class CrudFeedbackController {

    @FXML
    private TextField clientIdField, commentsField, ratingField;
    @FXML
    private TableView<Feedback> feedbackTable;
    @FXML
    private TableColumn<Feedback, Integer> idColumn, clientIdColumn, ratingColumn;
    @FXML
    private TableColumn<Feedback, String> commentsColumn;
    @FXML
    private Button addButton, updateButton, deleteButton;

    private FeedbackService feedbackService = new FeedbackService();
    private ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadFeedbacks();
    }

    private void loadFeedbacks() {
        List<Feedback> list = feedbackService.getAllFeedbacks();
        feedbackList.setAll(list);
        feedbackTable.setItems(feedbackList);
    }

    @FXML
    private void addFeedback() {
        try {
            int clientId = Integer.parseInt(clientIdField.getText());
            String comments = commentsField.getText().trim();

            // Vérification que le champ de description n'est pas vide
            if (comments.isEmpty()) {
                showErrorAlert("Description cannot be empty.");
                return;
            }

            // Le rating est optionnel, donc nous vérifions s'il est vide ou non
            Integer rating = null;
            if (!ratingField.getText().trim().isEmpty()) {
                rating = Integer.parseInt(ratingField.getText());
            }

            Feedback feedback = new Feedback(
                    clientId,
                    null, // eventId is null for app feedback
                    comments,
                    rating
            );

            feedbackService.addFeedback(feedback);
            loadFeedbacks();

            // Clear fields after successful addition
            clientIdField.clear();
            commentsField.clear();
            ratingField.clear();
        } catch (NumberFormatException e) {
            showErrorAlert("Please enter valid numbers for Client ID and Rating.");
        }
    }

    @FXML
    private void updateFeedback() {
        Feedback selected = feedbackTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                int clientId = Integer.parseInt(clientIdField.getText());
                String comments = commentsField.getText().trim();

                // Vérification que le champ de description n'est pas vide
                if (comments.isEmpty()) {
                    showErrorAlert("Description cannot be empty.");
                    return;
                }

                // Le rating est optionnel, donc nous vérifions s'il est vide ou non
                Integer rating = null;
                if (!ratingField.getText().trim().isEmpty()) {
                    rating = Integer.parseInt(ratingField.getText());
                }

                selected.setClientId(clientId);
                selected.setComments(comments);
                selected.setRating(rating);
                // Keep the existing eventId

                feedbackService.updateFeedback(selected);
                loadFeedbacks();

                // Clear fields after successful update
                clientIdField.clear();
                commentsField.clear();
                ratingField.clear();
            } catch (NumberFormatException e) {
                showErrorAlert("Please enter valid numbers for Client ID and Rating.");
            }
        }
    }

    @FXML
    private void deleteFeedback() {
        Feedback selected = feedbackTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            feedbackService.deleteFeedback(selected.getId());
            loadFeedbacks();

            // Clear fields after successful deletion
            clientIdField.clear();
            commentsField.clear();
            ratingField.clear();
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}