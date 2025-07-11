package evoplan.controllers.EventPlannerHomePage.WorkshopFrontEnd;

import evoplan.services.user.AppSessionManager;
import evoplan.services.workshop.reservationSessionService;
import evoplan.entities.workshop.WorkshopSessionDTO;
import evoplan.entities.workshop.session;
import evoplan.entities.workshop.workshop;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ChosenWorkshops implements Initializable {

    @FXML
    private TableView<WorkshopSessionDTO> userTable;

    @FXML
    private TableColumn<WorkshopSessionDTO, String> titleColumn;

    @FXML
    private TableColumn<WorkshopSessionDTO, Void> actionColumn;

    @FXML
    private Pagination pagination;

    @FXML
    private TableColumn<WorkshopSessionDTO, String> descriptionColumn;

    @FXML
    private TableColumn<WorkshopSessionDTO, String> locationColumn;

    @FXML
    private TableColumn<WorkshopSessionDTO, String> dateColumn;

    @FXML
    private TableColumn<WorkshopSessionDTO, String> startTimeColumn;

    @FXML
    private TableColumn<WorkshopSessionDTO, String> endTimeColumn;

    @FXML
    private TextField searchField; // Search field for filtering

    @FXML
    private ComboBox<String> filterComboBox; // ComboBox for filter criteria

    @FXML
    private Button clearButton; // Clear button to reset filters

    private final reservationSessionService reservationService = new reservationSessionService();

    private int participantId;

    private static final int ITEMS_PER_PAGE = 10;
    private ObservableList<WorkshopSessionDTO> allWorkshopSessions = FXCollections.observableArrayList();
    private FilteredList<WorkshopSessionDTO> filteredWorkshops; // Filtered list for search functionality

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Controller initialized!");

        // Load user data
        loadUserData();

        // Configure the table columns
        setupTableColumns();

        // Load workshops data
        loadWorkshops();

        // Initialize the filtered list
        filteredWorkshops = new FilteredList<>(allWorkshopSessions);

        // Bind search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterWorkshops();
        });

        filterComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterWorkshops();
        });

        // Setup pagination
        setupPagination();

        // Manually load the first page
        updateTableItems(0);
    }

    @FXML
    private void handleClear(ActionEvent event) {
        searchField.clear();
        filterComboBox.getSelectionModel().clearSelection();
        loadWorkshops(); // Reload all workshops
    }


    /**
     * Filter workshops based on the search text and selected filter criteria.
     */
    private void filterWorkshops() {
        String searchText = searchField.getText().trim().toLowerCase();
        String filterCriteria = filterComboBox.getValue();

        Predicate<WorkshopSessionDTO> predicate = workshop -> {
            if (searchText.isEmpty() || filterCriteria == null) {
                return true; // No filter applied
            }
            switch (filterCriteria) {
                case "Title":
                    return workshop.getTitle().toLowerCase().contains(searchText);
                case "Location":
                    return workshop.getLocation().toLowerCase().contains(searchText);
                case "Description":
                    return workshop.getDescription().toLowerCase().contains(searchText);
                default:
                    return true; // No filter applied
            }
        };

        // Apply the predicate to the filtered list
        filteredWorkshops.setPredicate(predicate);

        // Update the pagination and table items
        setupPagination();
        updateTableItems(0);
    }

    /**
     * Clear the search field and reset the filter.
     */
    @FXML
    private void handleClear() {
        searchField.clear();
        filterComboBox.getSelectionModel().clearSelection();
        filterWorkshops(); // Reset the filter
    }

    /**
     * Update the table items based on the current page.
     */
    private void updateTableItems(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredWorkshops.size());

        // Update the table with the items for the current page
        userTable.setItems(FXCollections.observableArrayList(filteredWorkshops.subList(fromIndex, toIndex)));

        // Refresh the TableView to ensure cells are updated
        userTable.refresh();
    }

    /**
     * Setup the pagination control.
     */
    private void setupPagination() {
        // Calculate the total number of pages
        int totalPages = (int) Math.ceil((double) filteredWorkshops.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(totalPages);

        // Add a listener to update the table when the page changes
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int pageIndex = newIndex.intValue();
            updateTableItems(pageIndex);
        });

        // Manually trigger the first page load
        pagination.setCurrentPageIndex(0);
    }

    /**
     * Load the logged-in user's data (participant ID).
     */
    private void loadUserData() {
        Integer clientId = AppSessionManager.getInstance().getCurrentUserId();
        if (clientId == null) {
            System.out.println("No user logged in!");
            return;
        }
        this.participantId = clientId;
    }

    /**
     * Load workshops data from the service.
     */
    private void loadWorkshops() {
        List<Integer> sessionIds = reservationService.getSessionIdsForUser(participantId);
        List<session> sessions = reservationService.getSessionDetails(sessionIds);
        List<Integer> workshopIds = new ArrayList<>();
        for (session s : sessions) {
            workshopIds.add(s.getId_workshop());
        }
        List<workshop> workshops = reservationService.getWorkshopDetails(workshopIds);

        allWorkshopSessions.clear();
        for (session s : sessions) {
            for (workshop w : workshops) {
                if (s.getId_workshop() == w.getId_workshop()) {
                    WorkshopSessionDTO dto = new WorkshopSessionDTO(
                            w.getTitle(),
                            w.getDescription(),
                            w.getLocation(),
                            s.getDate().toString(),
                            s.getDateheuredeb(),
                            s.getDateheurefin(),
                            s.getId_session()
                    );
                    allWorkshopSessions.add(dto);
                }
            }
        }

        if (allWorkshopSessions.isEmpty()) {
            System.out.println("No data found for the user and event!");
        }
    }

    /**
     * Setup the table columns and their cell factories.
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Configure the action column
        actionColumn.setCellFactory(param -> new TableCell<WorkshopSessionDTO, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    WorkshopSessionDTO selectedWorkshop = getTableView().getItems().get(getIndex());
                    if (selectedWorkshop != null) {
                        confirmAndDeleteWorkshop(selectedWorkshop);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableView().getItems().isEmpty() || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); // Clear the cell if it's empty or invalid
                } else {
                    setGraphic(deleteButton); // Set the delete button if the cell is not empty
                }
            }
        });
    }

    /**
     * Confirm and delete a workshop session.
     */
    private void confirmAndDeleteWorkshop(WorkshopSessionDTO selectedWorkshop) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Delete Workshop Session");
        confirmationAlert.setHeaderText("Are you sure you want to delete this workshop session?");
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean isDeleted = reservationService.deleteWorkshopSession(selectedWorkshop.getSessionId(), participantId);

            if (isDeleted) {
                // Remove the deleted item from the list
                allWorkshopSessions.remove(selectedWorkshop);

                // Recalculate the total number of pages
                int totalPages = (int) Math.ceil((double) allWorkshopSessions.size() / ITEMS_PER_PAGE);
                pagination.setPageCount(totalPages);

                // Ensure the current page index is valid
                int currentPageIndex = pagination.getCurrentPageIndex();
                if (currentPageIndex >= totalPages) {
                    currentPageIndex = Math.max(0, totalPages - 1); // Move to the last page if necessary
                    pagination.setCurrentPageIndex(currentPageIndex);
                }

                // Refresh the TableView to reflect the changes
                updateTableItems(currentPageIndex);

                // Show a success popup
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("The session has been removed from your list.");
                successAlert.showAndWait();
            } else {
                // Show an error popup
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Failed to delete the workshop session.");
                errorAlert.showAndWait();
            }
        }
    }
}