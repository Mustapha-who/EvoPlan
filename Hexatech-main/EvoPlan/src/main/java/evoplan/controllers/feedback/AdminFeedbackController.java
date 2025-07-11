package evoplan.controllers.feedback;

import evoplan.entities.feedback.Claim;
import evoplan.entities.feedback.Feedback;
import evoplan.services.feedback.ClaimService;
import evoplan.services.feedback.FeedbackService;
import evoplan.services.user.AppSessionManager;
import evoplan.services.user.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class AdminFeedbackController implements Initializable {

    @FXML private TextField feedbackSearchField;
    @FXML private ComboBox<Integer> feedbackRatingFilter;
    @FXML private ComboBox<String> feedbackClientFilter;
    @FXML private Button clearFeedbackFiltersButton;
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, String> feedbackClientColumn;
    @FXML private TableColumn<Feedback, Date> feedbackDateColumn;
    @FXML private TableColumn<Feedback, String> feedbackCommentsColumn;
    @FXML private TableColumn<Feedback, Integer> feedbackRatingColumn;
    @FXML private TableColumn<Feedback, Void> feedbackActionsColumn;

    @FXML private TextField claimSearchField;
    @FXML private ComboBox<Claim.ClaimType> claimTypeFilter;
    @FXML private ComboBox<Claim.ClaimStatus> claimStatusFilter;
    @FXML private ComboBox<String> claimClientFilter;
    @FXML private Button clearClaimFiltersButton;
    @FXML private TableView<Claim> claimTable;
    @FXML private TableColumn<Claim, String> claimClientColumn;
    @FXML private TableColumn<Claim, Date> claimDateColumn;
    @FXML private TableColumn<Claim, Claim.ClaimType> claimTypeColumn;
    @FXML private TableColumn<Claim, String> claimDescriptionColumn;
    @FXML private TableColumn<Claim, Claim.ClaimStatus> claimStatusColumn;
    @FXML private TableColumn<Claim, Void> claimActionsColumn;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ClaimService claimService = new ClaimService();
    private final UserService userService = new UserService();

    private ObservableList<Feedback> allFeedbacks;
    private FilteredList<Feedback> filteredFeedbacks;
    private ObservableList<Claim> allClaims;
    private FilteredList<Claim> filteredClaims;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize filters
        setupFilters();

        // Initialize table columns
        initializeTableColumns();

        // Set up search and filter listeners
        setupSearchAndFilters();

        // Load initial data
        loadAllData();
    }

    private void setupFilters() {
        // Setup rating filter
        feedbackRatingFilter.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 5).boxed().toList()));
        feedbackRatingFilter.setPromptText("All Ratings");

        // Setup claim type filter
        claimTypeFilter.setItems(FXCollections.observableArrayList(Claim.ClaimType.values()));
        claimTypeFilter.setPromptText("All Types");

        // Setup claim status filter
        claimStatusFilter.setItems(FXCollections.observableArrayList(Claim.ClaimStatus.values()));
        claimStatusFilter.setPromptText("All Statuses");

        // Load client names for filters
        List<String> clientNames = userService.getAllClientNames();
        feedbackClientFilter.setItems(FXCollections.observableArrayList(clientNames));
        claimClientFilter.setItems(FXCollections.observableArrayList(clientNames));
    }

    private void initializeTableColumns() {
        // Feedback table columns
        feedbackClientColumn.setCellValueFactory(cellData -> {
            int clientId = cellData.getValue().getClientId();
            String clientName = userService.getUserNameById(clientId);
            return javafx.beans.binding.Bindings.createStringBinding(() -> clientName);
        });
        feedbackDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        feedbackCommentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        feedbackRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        setupFeedbackActionsColumn();

        // Claims table columns
        claimClientColumn.setCellValueFactory(cellData -> {
            int clientId = cellData.getValue().getClientId();
            String clientName = userService.getUserNameById(clientId);
            return javafx.beans.binding.Bindings.createStringBinding(() -> clientName);
        });
        claimDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        claimTypeColumn.setCellValueFactory(new PropertyValueFactory<>("claimType"));
        claimDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        claimStatusColumn.setCellValueFactory(new PropertyValueFactory<>("claimStatus"));
        setupClaimActionsColumn();

        // Format date columns
        formatDateColumn(feedbackDateColumn);
        formatDateColumn(claimDateColumn);
    }

    private <S> void formatDateColumn(TableColumn<S, Date> column) {
        column.setCellFactory(col -> new TableCell<S, Date>() {
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
        // Feedback filters
        feedbackSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterFeedbacks());
        feedbackRatingFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterFeedbacks());
        feedbackClientFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterFeedbacks());
        clearFeedbackFiltersButton.setOnAction(e -> clearFeedbackFilters());

        // Claim filters
        claimSearchField.textProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        claimTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        claimStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        claimClientFilter.valueProperty().addListener((observable, oldValue, newValue) -> filterClaims());
        clearClaimFiltersButton.setOnAction(e -> clearClaimFilters());
    }

    private void setupFeedbackActionsColumn() {
        feedbackActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, deleteButton);

            {
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                deleteButton.setOnAction(event -> {
                    Feedback feedback = getTableView().getItems().get(getIndex());
                    handleFeedbackDelete(feedback);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupClaimActionsColumn() {
        claimActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<Claim.ClaimStatus> statusComboBox = new ComboBox<>();
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, statusComboBox, deleteButton);

            {
                statusComboBox.setItems(FXCollections.observableArrayList(Claim.ClaimStatus.values()));
                statusComboBox.setPrefWidth(120);

                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                statusComboBox.setOnAction(event -> {
                    Claim claim = getTableView().getItems().get(getIndex());
                    handleClaimStatusChange(claim, statusComboBox.getValue());
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
                    Claim claim = getTableView().getItems().get(getIndex());
                    statusComboBox.setValue(claim.getClaimStatus());
                    setGraphic(container);
                }
            }
        });
    }

    private void loadAllData() {
        try {
            // Load feedbacks
            List<Feedback> feedbacks = feedbackService.getAllFeedbacks();
            allFeedbacks = FXCollections.observableArrayList(feedbacks);
            filteredFeedbacks = new FilteredList<>(allFeedbacks);
            feedbackTable.setItems(filteredFeedbacks);

            // Show total count in table header
            feedbackTable.setPlaceholder(new Label("No feedback available"));
            String tableHeader = String.format("All Feedback (%d)", feedbacks.size());
            ((Label) ((VBox) feedbackTable.getParent()).getChildren().get(0)).setText(tableHeader);

            // Load claims
            List<Claim> claims = claimService.getAllClaims();
            allClaims = FXCollections.observableArrayList(claims);
            filteredClaims = new FilteredList<>(allClaims);
            claimTable.setItems(filteredClaims);

            // Show total count in table header
            claimTable.setPlaceholder(new Label("No claims available"));
            tableHeader = String.format("All Claims (%d)", claims.size());
            ((Label) ((VBox) claimTable.getParent()).getChildren().get(0)).setText(tableHeader);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterFeedbacks() {
        if (filteredFeedbacks == null) return;

        filteredFeedbacks.setPredicate(feedback -> {
            boolean matchesSearch = true;
            boolean matchesRating = true;
            boolean matchesClient = true;

            if (feedbackSearchField.getText() != null && !feedbackSearchField.getText().isEmpty()) {
                String searchText = feedbackSearchField.getText().toLowerCase();
                matchesSearch = feedback.getComments().toLowerCase().contains(searchText);
            }

            if (feedbackRatingFilter.getValue() != null) {
                matchesRating = feedback.getRating() == feedbackRatingFilter.getValue();
            }

            if (feedbackClientFilter.getValue() != null) {
                matchesClient = String.valueOf(feedback.getClientId()).equals(feedbackClientFilter.getValue());
            }

            return matchesSearch && matchesRating && matchesClient;
        });
    }

    private void filterClaims() {
        if (filteredClaims == null) return;

        filteredClaims.setPredicate(claim -> {
            boolean matchesSearch = true;
            boolean matchesType = true;
            boolean matchesStatus = true;
            boolean matchesClient = true;

            if (claimSearchField.getText() != null && !claimSearchField.getText().isEmpty()) {
                String searchText = claimSearchField.getText().toLowerCase();
                matchesSearch = claim.getDescription().toLowerCase().contains(searchText);
            }

            if (claimTypeFilter.getValue() != null) {
                matchesType = claim.getClaimType() == claimTypeFilter.getValue();
            }

            if (claimStatusFilter.getValue() != null) {
                matchesStatus = claim.getClaimStatus() == claimStatusFilter.getValue();
            }

            if (claimClientFilter.getValue() != null) {
                matchesClient = String.valueOf(claim.getClientId()).equals(claimClientFilter.getValue());
            }

            return matchesSearch && matchesType && matchesStatus && matchesClient;
        });
    }

    private void clearFeedbackFilters() {
        feedbackSearchField.clear();
        feedbackRatingFilter.setValue(null);
        feedbackClientFilter.setValue(null);
    }

    private void clearClaimFilters() {
        claimSearchField.clear();
        claimTypeFilter.setValue(null);
        claimStatusFilter.setValue(null);
        claimClientFilter.setValue(null);
    }

    private void handleFeedbackDelete(Feedback feedback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Feedback");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this feedback?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (feedbackService.deleteFeedback(feedback.getId())) {
                    loadAllData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Feedback deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete feedback.");
                }
            }
        });
    }

    private void handleClaimStatusChange(Claim claim, Claim.ClaimStatus newStatus) {
        if (newStatus != claim.getClaimStatus()) {
            claim.setClaimStatus(newStatus);
            try {
                claimService.updateClaim(claim);
                loadAllData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Claim status updated successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update claim status.");
                e.printStackTrace();
            }
        }
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
                    loadAllData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Claim deleted successfully!");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete claim.");
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}