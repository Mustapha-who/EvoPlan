package evoplan.controllers.EventPlannerHomePage.Workshop;
import evoplan.entities.workshop.workshop;
import evoplan.services.user.AppSessionManager;
import evoplan.services.workshop.workshopService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
public class InstructorController {


        @FXML
        private TableView<workshop> workshopTable; // TableView to display workshops

        @FXML
        private TableColumn<workshop, Integer> idWorkshopColumn; // Column for Workshop ID

        @FXML
        private TableColumn<workshop, String> titleColumn; // Column for Workshop Title

        @FXML
        private TableColumn<workshop, Date> dateColumn; // Column for Workshop Date

        @FXML
        private TableColumn<workshop, Date> endDateColumn; // Column for Workshop End Date

        @FXML
        private TableColumn<workshop, Integer> instructorColumn; // Column for Instructor ID

        @FXML
        private TableColumn<workshop, Integer> eventColumn; // Column for Event ID

        @FXML
        private TableColumn<workshop, Integer> capacityColumn; // Column for Workshop Capacity

        @FXML
        private TableColumn<workshop, String> locationColumn; // Column for Workshop Location

        @FXML
        private TableColumn<workshop, String> descriptionColumn; // Column for Workshop Description

        @FXML
        private TextField searchField; // Search field for filtering

        @FXML
        private ComboBox<String> filterComboBox; // ComboBox for filter criteria

        private ObservableList<workshop> workshopList; // ObservableList to hold workshops
        private FilteredList<workshop> filteredWorkshops; // FilteredList for search functionality


        public void initialize(URL location, ResourceBundle resources) {
            // Set up the TableView columns
            idWorkshopColumn.setCellValueFactory(new PropertyValueFactory<>("id_workshop"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            endDateColumn.setCellValueFactory(new PropertyValueFactory<>("enddate"));
            instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
            eventColumn.setCellValueFactory(new PropertyValueFactory<>("id_event"));
            capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            // Initialize filter ComboBox
            filterComboBox.setItems(FXCollections.observableArrayList("Title", "Capacity", "Location", "Description"));

            // Load user data and display workshops
            loadUserData();

            // Bind search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterWorkshops();
            });

            filterComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                filterWorkshops();
            });
        }


        private void loadUserData() {
            // Retrieve the logged-in user ID
            Integer instructorId = AppSessionManager.getInstance().getCurrentUserId();
            if (instructorId == null) {
                System.out.println("No user logged in!");
                return;
            }

            // Fetch workshops related to the logged-in instructor
            workshopService workshopService = new workshopService();
            List<workshop> workshops = workshopService.getWorkshopsByInstructorId(instructorId);

            // Convert the list of workshops to an ObservableList
            workshopList = FXCollections.observableArrayList(workshops);

            // Wrap the ObservableList in a FilteredList
            filteredWorkshops = new FilteredList<>(workshopList);

            // Display the workshops in the TableView
            if (workshops.isEmpty()) {
                System.out.println("No workshops found for this instructor.");
            } else {
                System.out.println("Workshops for Instructor ID " + instructorId + ":");
                workshopTable.setItems(filteredWorkshops); // Populate the TableView
            }
        }

        private void filterWorkshops() {
            String searchText = searchField.getText().trim().toLowerCase();
            String filterCriteria = filterComboBox.getValue();

            Predicate<workshop> predicate = workshop -> {
                if (searchText.isEmpty() || filterCriteria == null) {
                    return true;
                }
                switch (filterCriteria) {
                    case "Title":
                        return workshop.getTitle().toLowerCase().contains(searchText);
                    case "Capacity":
                        return String.valueOf(workshop.getCapacity()).contains(searchText);
                    case "Location":
                        return workshop.getLocation().toLowerCase().contains(searchText);
                    case "Description":
                        return workshop.getDescription().toLowerCase().contains(searchText);
                    default:
                        return true;
                }
            };

            filteredWorkshops.setPredicate(predicate);
        }

        @FXML
        public void handleClear(javafx.event.ActionEvent actionEvent) {
            searchField.clear(); // Clear the search field
            filterComboBox.getSelectionModel().clearSelection(); // Reset the filter combo box
            loadUserData(); // Reload all workshops
        }

}
