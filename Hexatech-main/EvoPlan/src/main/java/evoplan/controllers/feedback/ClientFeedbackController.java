package evoplan.controllers.feedback;

import evoplan.entities.feedback.Claim;
import evoplan.entities.feedback.Feedback;
import evoplan.services.feedback.ClaimService;
import evoplan.services.feedback.FeedbackService;
import evoplan.services.user.UserService;
import evoplan.services.user.AppSessionManager;
import evoplan.utils.ProfanityFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class ClientFeedbackController implements Initializable {

    @FXML private TextArea feedbackComments;
    @FXML private Button ratingButton;
    @FXML private Label ratingLabel;
    @FXML private Button submitFeedbackButton;
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, String> feedbackCommentsColumn;
    @FXML private TableColumn<Feedback, Integer> feedbackRatingColumn;
    @FXML private TableColumn<Feedback, String> feedbackClientColumn;
    @FXML private TableColumn<Feedback, Void> feedbackActionsColumn;

    // New feedback search and filter controls
    @FXML private TextField feedbackSearchField;
    @FXML private ComboBox<Integer> feedbackRatingFilter;
    @FXML private Button clearFeedbackFiltersButton;

    @FXML private ComboBox<Claim.ClaimType> claimTypeComboBox;
    @FXML private TextArea claimDescription;
    @FXML private Button submitClaimButton;
    @FXML private TableView<Claim> claimTable;
    @FXML private TableColumn<Claim, String> claimClientColumn;
    @FXML private TableColumn<Claim, Date> claimDateColumn;
    @FXML private TableColumn<Claim, Claim.ClaimType> claimTypeColumn;
    @FXML private TableColumn<Claim, String> claimDescriptionColumn;
    @FXML private TableColumn<Claim, Claim.ClaimStatus> claimStatusColumn;
    @FXML private TableColumn<Claim, Void> claimActionsColumn;

    // New claim search and filter controls
    @FXML private TextField claimSearchField;
    @FXML private ComboBox<Claim.ClaimType> claimTypeFilter;
    @FXML private ComboBox<Claim.ClaimStatus> claimStatusFilter;
    @FXML private Button clearClaimFiltersButton;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ClaimService claimService = new ClaimService();
    private final UserService userService = new UserService();
    private final AppSessionManager sessionManager = AppSessionManager.getInstance();

    private ObservableList<Feedback> allFeedbacks;
    private FilteredList<Feedback> filteredFeedbacks;
    private ObservableList<Claim> allClaims;
    private FilteredList<Claim> filteredClaims;

    private Integer currentRating = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize rating filter
        feedbackRatingFilter.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 5).boxed().toList()));
        feedbackRatingFilter.setPromptText("All Ratings");

        // Initialize claim type combo boxes
        claimTypeComboBox.setItems(FXCollections.observableArrayList(Claim.ClaimType.values()));
        claimTypeFilter.setItems(FXCollections.observableArrayList(Claim.ClaimType.values()));
        claimTypeFilter.setPromptText("All Types");

        // Initialize claim status filter
        claimStatusFilter.setItems(FXCollections.observableArrayList(Claim.ClaimStatus.values()));
        claimStatusFilter.setPromptText("All Statuses");

        // Initialize table columns
        initializeTableColumns();

        // Set up search and filter listeners
        setupSearchAndFilters();

        // Load initial data
        loadFeedbackHistory();
        loadClaimHistory();

        // Set up button handlers
        setupButtonHandlers();

        // Set up rating button handler
        ratingButton.setOnAction(e -> showRatingDialog());


    }

    private void initializeTableColumns() {
        feedbackCommentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        feedbackRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        setupFeedbackActionsColumn();

        claimDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        claimTypeColumn.setCellValueFactory(new PropertyValueFactory<>("claimType"));
        claimDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        claimStatusColumn.setCellValueFactory(new PropertyValueFactory<>("claimStatus"));

        setupClaimActionsColumn();

        // Format date column
        claimDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(date));
                }
            }
        });
    }

    private void setupSearchAndFilters() {
        // Feedback search and filter
        feedbackSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterFeedbacks());
        feedbackRatingFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterFeedbacks());
        clearFeedbackFiltersButton.setOnAction(e -> clearFeedbackFilters());

        // Claim search and filter
        claimSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        claimTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        claimStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        clearClaimFiltersButton.setOnAction(e -> clearClaimFilters());
    }

    private void setupButtonHandlers() {
        submitFeedbackButton.setOnAction(e -> handleFeedbackSubmission());
        submitClaimButton.setOnAction(e -> handleClaimSubmission());
    }

    private void filterFeedbacks() {
        if (filteredFeedbacks == null) return;

        filteredFeedbacks.setPredicate(feedback -> {
            boolean matchesSearch = true;
            boolean matchesRating = true;

            // Apply search filter
            if (feedbackSearchField.getText() != null && !feedbackSearchField.getText().isEmpty()) {
                String searchText = feedbackSearchField.getText().toLowerCase();
                matchesSearch = feedback.getComments().toLowerCase().contains(searchText);
            }

            // Apply rating filter
            if (feedbackRatingFilter.getValue() != null) {
                matchesRating = feedback.getRating() == feedbackRatingFilter.getValue();
            }

            return matchesSearch && matchesRating;
        });
    }

    private void filterClaims() {
        if (filteredClaims == null) return;

        filteredClaims.setPredicate(claim -> {
            boolean matchesSearch = true;
            boolean matchesType = true;
            boolean matchesStatus = true;

            // Apply search filter
            if (claimSearchField.getText() != null && !claimSearchField.getText().isEmpty()) {
                String searchText = claimSearchField.getText().toLowerCase();
                matchesSearch = claim.getDescription().toLowerCase().contains(searchText);
            }

            // Apply type filter
            if (claimTypeFilter.getValue() != null) {
                matchesType = claim.getClaimType() == claimTypeFilter.getValue();
            }

            // Apply status filter
            if (claimStatusFilter.getValue() != null) {
                matchesStatus = claim.getClaimStatus() == claimStatusFilter.getValue();
            }

            return matchesSearch && matchesType && matchesStatus;
        });
    }

    private void clearFeedbackFilters() {
        feedbackSearchField.clear();
        feedbackRatingFilter.setValue(null);
    }

    private void clearClaimFilters() {
        claimSearchField.clear();
        claimTypeFilter.setValue(null);
        claimStatusFilter.setValue(null);
    }

    private void loadFeedbackHistory() {
        try {
            List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
            int currentClientId = sessionManager.getCurrentUser().getId();
            allFeedbacks = FXCollections.observableArrayList(
                    feedbacks.stream()
                            .filter(f -> f.getClientId() == currentClientId)
                            .toList()
            );
            filteredFeedbacks = new FilteredList<>(allFeedbacks, p -> true);
            feedbackTable.setItems(filteredFeedbacks);
        } catch (Exception e) {
            System.out.println("Error loading feedback history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadClaimHistory() {
        try {
            List<Claim> claims = claimService.getAllClaims();
            int currentClientId = sessionManager.getCurrentUser().getId();
            allClaims = FXCollections.observableArrayList(
                    claims.stream()
                            .filter(c -> c.getClientId() == currentClientId)
                            .toList()
            );
            filteredClaims = new FilteredList<>(allClaims, p -> true);
            claimTable.setItems(filteredClaims);
        } catch (Exception e) {
            System.out.println("Error loading claim history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFeedbackActionsColumn() {
        feedbackActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button shareButton = new Button("Share");
            private final HBox container = new HBox(5, editButton, deleteButton, shareButton);

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                shareButton.setStyle("-fx-background-color: #3b5998; -fx-text-fill: white;"); // Facebook blue

                editButton.setOnAction(event -> {
                    Feedback feedback = getTableView().getItems().get(getIndex());
                    handleFeedbackEdit(feedback);
                });

                deleteButton.setOnAction(event -> {
                    Feedback feedback = getTableView().getItems().get(getIndex());
                    handleFeedbackDelete(feedback);
                });

                shareButton.setOnAction(event -> {
                    Feedback feedback = getTableView().getItems().get(getIndex());
                    handleFeedbackShare(feedback);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void setupClaimActionsColumn() {
        claimActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Claim claim = getTableView().getItems().get(getIndex());
                    handleClaimEdit(claim);
                });

                deleteButton.setOnAction(event -> {
                    Claim claim = getTableView().getItems().get(getIndex());
                    handleClaimDelete(claim);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void showRatingDialog() {
        RatingDialog dialog = new RatingDialog();
        dialog.showAndWait().ifPresent(rating -> {
            currentRating = rating;
            ratingLabel.setText(String.format("%d/5 stars", rating));
            ratingLabel.setStyle("-fx-font-style: normal; -fx-font-weight: bold;");
        });
    }

    private void handleFeedbackSubmission() {
        String comments = feedbackComments.getText().trim();

        if (comments.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter your feedback comments.");
            return;
        }

        if (currentRating == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please rate your experience.");
            return;
        }

        // Apply profanity filter
        String filteredComments = ProfanityFilter.filterText(comments);

        Feedback feedback = new Feedback(
                sessionManager.getCurrentUser().getId(),
                null, // eventId is null for app feedback
                filteredComments,
                currentRating
        );

        if (feedbackService.addFeedback(feedback)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your feedback has been submitted successfully!");
            feedbackComments.clear();
            currentRating = null;
            ratingLabel.setText("No rating yet");
            ratingLabel.setStyle("-fx-font-style: italic;");
            loadFeedbackHistory();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit feedback. Please try again.");
        }
    }

    private void handleClaimSubmission() {
        String description = claimDescription.getText().trim();
        Claim.ClaimType type = claimTypeComboBox.getValue();

        if (description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please describe your claim.");
            return;
        }

        if (type == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a claim type.");
            return;
        }

        try {
            // Apply profanity filter
            String filteredDescription = ProfanityFilter.filterText(description);

            // Generate a temporary ID - it will be replaced in the service
            String tempId = "TEMP_" + System.currentTimeMillis();

            Claim claim = new Claim(
                    tempId,
                    filteredDescription,
                    type,
                    new Date(), // Current date
                    Claim.ClaimStatus.PENDING, // Initial status is always PENDING
                    sessionManager.getCurrentUser().getId(),
                    null // eventId is null for app-related claims
            );

            claimService.addClaim(claim);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your claim has been submitted successfully!");
            claimDescription.clear();
            claimTypeComboBox.setValue(null);
            loadClaimHistory();
        } catch (Exception e) {
            System.out.println("Error submitting claim: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit claim. Please try again.");
        }
    }

    private void handleFeedbackEdit(Feedback feedback) {
        feedbackComments.setText(feedback.getComments());
        currentRating = feedback.getRating();
        ratingLabel.setText(String.format("%d/5 stars", currentRating));
        ratingLabel.setStyle("-fx-font-style: normal; -fx-font-weight: bold;");

        // Change submit button to update mode
        submitFeedbackButton.setText("Update Feedback");
        submitFeedbackButton.setOnAction(e -> {
            String comments = feedbackComments.getText().trim();

            // Apply profanity filter
            String filteredComments = ProfanityFilter.filterText(comments);

            feedback.setComments(filteredComments);
            feedback.setRating(currentRating);

            if (feedbackService.updateFeedback(feedback)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Feedback updated successfully!");
                feedbackComments.clear();
                currentRating = null;
                ratingLabel.setText("No rating yet");
                ratingLabel.setStyle("-fx-font-style: italic;");
                submitFeedbackButton.setText("Submit Feedback");
                submitFeedbackButton.setOnAction(ev -> handleFeedbackSubmission());
                loadFeedbackHistory();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update feedback.");
            }
        });
    }

    private void handleFeedbackDelete(Feedback feedback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Feedback");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this feedback?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (feedbackService.deleteFeedback(feedback.getId())) {
                    loadFeedbackHistory();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Feedback deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete feedback.");
                }
            }
        });
    }

    private void handleClaimEdit(Claim claim) {
        claimDescription.setText(claim.getDescription());
        claimTypeComboBox.setValue(claim.getClaimType());

        // Change submit button to update mode
        submitClaimButton.setText("Update Claim");
        submitClaimButton.setOnAction(e -> {
            String description = claimDescription.getText().trim();

            // Apply profanity filter
            String filteredDescription = ProfanityFilter.filterText(description);

            claim.setDescription(filteredDescription);
            claim.setClaimType(claimTypeComboBox.getValue());

            try {
                claimService.updateClaim(claim);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Claim updated successfully!");
                claimDescription.clear();
                claimTypeComboBox.setValue(null);
                submitClaimButton.setText("Submit Claim");
                submitClaimButton.setOnAction(ev -> handleClaimSubmission());
                loadClaimHistory();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update claim.");
            }
        });
    }

    private void handleClaimDelete(Claim claim) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Claim");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this claim?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    claimService.deleteClaim(claim.getId());
                    loadClaimHistory();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Claim deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete claim.");
                }
            }
        });
    }

    private void handleFeedbackShare(Feedback feedback) {
        try {
            // Create the sharing URL with the feedback content
            String shareText = String.format("I rated EvoPlan %d/5 stars! Here's my feedback: %s",
                    feedback.getRating(),
                    feedback.getComments());

            // URL encode the text for the Facebook share URL
            String encodedText = java.net.URLEncoder.encode(shareText, "UTF-8");
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=https://evoplan.com&quote=" + encodedText;

            // Open the default browser with the Facebook share dialog
            java.awt.Desktop.getDesktop().browse(new java.net.URI(facebookUrl));

            showAlert(Alert.AlertType.INFORMATION, "Share", "Opening Facebook sharing dialog in your browser...");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Facebook sharing dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}