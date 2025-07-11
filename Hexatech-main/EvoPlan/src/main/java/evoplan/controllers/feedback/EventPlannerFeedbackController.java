package evoplan.controllers.feedback;

import evoplan.entities.feedback.Claim;
import evoplan.entities.feedback.Feedback;
import evoplan.services.feedback.ClaimService;
import evoplan.services.feedback.FeedbackService;
import evoplan.services.event.EventService;
import evoplan.entities.event.Event;
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

public class EventPlannerFeedbackController implements Initializable {
    @FXML private Label totalFeedbackLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label totalClaimsLabel;
    @FXML private Label pendingClaimsLabel;
    @FXML private Label resolvedClaimsLabel;

    // Feedback edit controls
    @FXML private TextArea feedbackCommentsEdit;
    @FXML private ComboBox<Integer> feedbackRatingEdit;
    @FXML private Button saveFeedbackButton;
    @FXML private Button cancelFeedbackButton;

    // Claim edit controls
    @FXML private ComboBox<Claim.ClaimType> claimTypeEdit;
    @FXML private ComboBox<Claim.ClaimStatus> claimStatusEdit;
    @FXML private TextArea claimDescriptionEdit;
    @FXML private Button saveClaimButton;
    @FXML private Button cancelClaimButton;

    // Search and filter controls
    @FXML private TextField feedbackSearchField;
    @FXML private ComboBox<Integer> feedbackRatingFilter;
    @FXML private Button clearFeedbackFiltersButton;

    @FXML private TextField claimSearchField;
    @FXML private ComboBox<Claim.ClaimType> claimTypeFilter;
    @FXML private ComboBox<Claim.ClaimStatus> claimStatusFilter;
    @FXML private Button clearClaimFiltersButton;

    // Tables and columns
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, String> feedbackClientColumn;
    @FXML private TableColumn<Feedback, String> feedbackCommentsColumn;
    @FXML private TableColumn<Feedback, Integer> feedbackRatingColumn;
    @FXML private TableColumn<Feedback, Void> feedbackActionsColumn;

    @FXML private TableView<Claim> claimTable;
    @FXML private TableColumn<Claim, String> claimClientColumn;
    @FXML private TableColumn<Claim, Date> claimDateColumn;
    @FXML private TableColumn<Claim, Claim.ClaimType> claimTypeColumn;
    @FXML private TableColumn<Claim, String> claimDescriptionColumn;
    @FXML private TableColumn<Claim, Claim.ClaimStatus> claimStatusColumn;
    @FXML private TableColumn<Claim, Void> claimActionsColumn;

    private final FeedbackService feedbackService = new FeedbackService();
    private final ClaimService claimService = new ClaimService();

    private ObservableList<Feedback> allFeedbacks;
    private FilteredList<Feedback> filteredFeedbacks;
    private ObservableList<Claim> allClaims;
    private FilteredList<Claim> filteredClaims;

    private Feedback selectedFeedback;
    private Claim selectedClaim;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEditControls();
        initializeTableColumns();
        setupSearchAndFilters();
        setupActionColumns();
        loadAllData();
        updateStatistics();
    }

    private void setupEditControls() {
        // Setup feedback edit controls
        feedbackRatingEdit.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        saveFeedbackButton.setOnAction(e -> handleFeedbackSave());
        cancelFeedbackButton.setOnAction(e -> clearFeedbackEdit());

        // Setup claim edit controls
        claimTypeEdit.setItems(FXCollections.observableArrayList(Claim.ClaimType.values()));
        claimStatusEdit.setItems(FXCollections.observableArrayList(Claim.ClaimStatus.values()));
        saveClaimButton.setOnAction(e -> handleClaimSave());
        cancelClaimButton.setOnAction(e -> clearClaimEdit());
    }

    private void setupActionColumns() {
        // Setup feedback actions column
        feedbackActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(e -> handleFeedbackEdit(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleFeedbackDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        // Setup claim actions column
        claimActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(e -> handleClaimEdit(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleClaimDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void handleFeedbackEdit(Feedback feedback) {
        selectedFeedback = feedback;
        feedbackCommentsEdit.setText(feedback.getComments());
        feedbackRatingEdit.setValue(feedback.getRating());
    }

    private void handleFeedbackSave() {
        if (selectedFeedback != null) {
            selectedFeedback.setComments(feedbackCommentsEdit.getText());
            selectedFeedback.setRating(feedbackRatingEdit.getValue());

            feedbackService.updateFeedback(selectedFeedback);
            clearFeedbackEdit();
            loadAllData();
            updateStatistics();
        }
    }

    private void clearFeedbackEdit() {
        selectedFeedback = null;
        feedbackCommentsEdit.clear();
        feedbackRatingEdit.setValue(null);
    }

    private void handleClaimEdit(Claim claim) {
        selectedClaim = claim;
        claimTypeEdit.setValue(claim.getClaimType());
        claimStatusEdit.setValue(claim.getClaimStatus());
        claimDescriptionEdit.setText(claim.getDescription());
    }

    private void handleClaimSave() {
        if (selectedClaim != null) {
            selectedClaim.setClaimType(claimTypeEdit.getValue());
            selectedClaim.setClaimStatus(claimStatusEdit.getValue());
            selectedClaim.setDescription(claimDescriptionEdit.getText());

            claimService.updateClaim(selectedClaim);
            clearClaimEdit();
            loadAllData();
            updateStatistics();
        }
    }

    private void clearClaimEdit() {
        selectedClaim = null;
        claimTypeEdit.setValue(null);
        claimStatusEdit.setValue(null);
        claimDescriptionEdit.clear();
    }

    private void handleFeedbackDelete(Feedback feedback) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Feedback");
        alert.setHeaderText("Delete Feedback");
        alert.setContentText("Are you sure you want to delete this feedback?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            feedbackService.deleteFeedback(feedback.getId());
            loadAllData();
            updateStatistics();
        }
    }

    private void handleClaimDelete(Claim claim) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Claim");
        alert.setHeaderText("Delete Claim");
        alert.setContentText("Are you sure you want to delete this claim?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            claimService.deleteClaim(claim.getId());
            loadAllData();
            updateStatistics();
        }
    }

    private void setupSearchAndFilters() {
        // Setup feedback filters
        feedbackRatingFilter.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        clearFeedbackFiltersButton.setOnAction(e -> clearFeedbackFilters());

        // Setup claim filters
        claimTypeFilter.setItems(FXCollections.observableArrayList(Claim.ClaimType.values()));
        claimStatusFilter.setItems(FXCollections.observableArrayList(Claim.ClaimStatus.values()));
        clearClaimFiltersButton.setOnAction(e -> clearClaimFilters());

        // Setup search listeners
        feedbackSearchField.textProperty().addListener((obs, old, newValue) -> filterFeedbacks());
        feedbackRatingFilter.valueProperty().addListener((obs, old, newValue) -> filterFeedbacks());


        claimSearchField.textProperty().addListener((obs, old, newValue) -> filterClaims());
        claimTypeFilter.valueProperty().addListener((obs, old, newValue) -> filterClaims());
        claimStatusFilter.valueProperty().addListener((obs, old, newValue) -> filterClaims());

    }

    private void filterFeedbacks() {
        filteredFeedbacks.setPredicate(feedback -> {
            boolean matchesSearch = feedbackSearchField.getText() == null || feedbackSearchField.getText().isEmpty() ||
                    feedback.getComments().toLowerCase().contains(feedbackSearchField.getText().toLowerCase());

            boolean matchesRating = feedbackRatingFilter.getValue() == null ||
                    feedback.getRating() == feedbackRatingFilter.getValue();



            return matchesSearch && matchesRating ;
        });
    }

    private void filterClaims() {
        filteredClaims.setPredicate(claim -> {
            boolean matchesSearch = claimSearchField.getText() == null || claimSearchField.getText().isEmpty() ||
                    claim.getDescription().toLowerCase().contains(claimSearchField.getText().toLowerCase());

            boolean matchesType = claimTypeFilter.getValue() == null ||
                    claim.getClaimType() == claimTypeFilter.getValue();

            boolean matchesStatus = claimStatusFilter.getValue() == null ||
                    claim.getClaimStatus() == claimStatusFilter.getValue();


            return matchesSearch && matchesType && matchesStatus ;
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

    private void loadAllData() {
        // Load feedbacks
        allFeedbacks = FXCollections.observableArrayList(feedbackService.getAllFeedbacks());
        filteredFeedbacks = new FilteredList<>(allFeedbacks);
        feedbackTable.setItems(filteredFeedbacks);

        // Load claims
        allClaims = FXCollections.observableArrayList(claimService.getAllClaims());
        filteredClaims = new FilteredList<>(allClaims);
        claimTable.setItems(filteredClaims);


    }



    private void updateStatistics() {
        // Update feedback statistics
        totalFeedbackLabel.setText(String.valueOf(allFeedbacks.size()));
        double avgRating = allFeedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        avgRatingLabel.setText(String.format("Avg Rating: %.1f", avgRating));

        // Update claim statistics
        totalClaimsLabel.setText(String.valueOf(allClaims.size()));
        long pendingCount = allClaims.stream()
                .filter(c -> c.getClaimStatus() == Claim.ClaimStatus.PENDING)
                .count();
        long resolvedCount = allClaims.stream()
                .filter(c -> c.getClaimStatus() == Claim.ClaimStatus.RESOLVED)
                .count();
        pendingClaimsLabel.setText("Pending: " + pendingCount);
        resolvedClaimsLabel.setText("Resolved: " + resolvedCount);
    }

    private void initializeTableColumns() {
        // Initialize feedback table columns

        feedbackClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        feedbackCommentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        feedbackRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Initialize claim table columns

        claimClientColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        claimDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        claimTypeColumn.setCellValueFactory(new PropertyValueFactory<>("claimType"));
        claimDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        claimStatusColumn.setCellValueFactory(new PropertyValueFactory<>("claimStatus"));

        // Format date columns
        formatDateColumn(claimDateColumn);
    }

    private <S> void formatDateColumn(TableColumn<S, Date> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toString()); // You might want to use a DateTimeFormatter here
                }
            }
        });
    }
}