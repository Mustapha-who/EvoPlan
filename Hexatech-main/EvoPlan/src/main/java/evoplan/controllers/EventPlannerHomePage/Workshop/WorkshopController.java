package evoplan.controllers.EventPlannerHomePage.Workshop;

import evoplan.controllers.EventPlannerHomePage.Session.SessionController;
import evoplan.entities.event.Event;
import evoplan.entities.workshop.workshop;
import evoplan.services.event.EventService;
import evoplan.services.workshop.workshopService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class WorkshopController implements Initializable {

    @FXML
    private TableView<workshop> userTable; // TableView to display workshops

    @FXML
    private TableColumn<workshop, Date> enddateColumn;

    @FXML
    private TableColumn<workshop, Integer> idWorkshopColumn; // Column for ID Workshopp

    @FXML
    private TableColumn<workshop, String> titleColumn; // Column for Title

    @FXML
    private TableColumn<workshop, Date> dateColumn; // Column for Date

    @FXML
    private TableColumn<workshop, Integer> instructorColumn; // Column for Instructor

    @FXML
    private TableColumn<workshop, Integer> eventColumn; // Column for Session

    @FXML
    private TableColumn<workshop, Integer> capacityColumn; // Column for Capacity

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button searchButton; // Search Button to trigger filter

    @FXML
    private TableColumn<workshop, String> locationColumn; // Column for Location

    @FXML
    private TableColumn<workshop, String> descriptionColumn; // Column for Description

    @FXML
    private Pane chartContainer; // Add this Pane in your FXML file to hold the chart


    @FXML
    private TableColumn<workshop, Void> actionColumn; // Column for Edit/Delete actions

    private final workshopService workshopService = new workshopService(); // Service to fetch data
    private ObservableList<workshop> workshopList;
    private FilteredList<workshop> filteredWorkshops;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize filter ComboBox
        filterComboBox.setItems(FXCollections.observableArrayList("Title", "Capacity", "Location", "Description"));

        // Load workshops and set up the TableView
        loadWorkshops();
        idWorkshopColumn.setCellValueFactory(new PropertyValueFactory<>("id_workshop"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        enddateColumn.setCellValueFactory(new PropertyValueFactory<>("enddate"));
        instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        eventColumn.setCellValueFactory(new PropertyValueFactory<>("id_event"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        actionColumn.setCellFactory(param -> new TableCell<workshop, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox hbox = new HBox(10, editButton, deleteButton);

            {
                // Add action for Edit button
                editButton.setOnAction(event -> {
                    workshop selectedWorkshop = getTableView().getItems().get(getIndex());
                    openEditWorkshopForm(selectedWorkshop);
                });

                // Add action for Delete button
                deleteButton.setOnAction(event -> {
                    workshop selectedWorkshop = getTableView().getItems().get(getIndex());
                    confirmAndDeleteWorkshop(selectedWorkshop);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null); // Clear the cell if it's empty
                } else {
                    setGraphic(hbox); // Set the buttons if the cell is not empty
                }
            }
        });

        // Double-click to open sessions
        userTable.setRowFactory(tv -> {
            TableRow<workshop> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    workshop selectedWorkshop = row.getItem();
                    openSessionsTab(selectedWorkshop);
                }
            });
            return row;
        });

        // Bind search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterWorkshops();
        });

        filterComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filterWorkshops();
        });


        // Load workshops from the service and populate the TableView
        loadWorkshops();
    }

    private void openSessionsTab(workshop workshop) {
        try {
            // Load the Sessions.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/Sessions.fxml"));
            Parent root = loader.load();

            // Pass the selected workshop to the SessionsController
            SessionController sessionsController = loader.getController();
            sessionsController.setWorkshop(workshop); // Pass the selected workshop

            // Create a new Stage (window)
            Stage stage = new Stage();
            stage.setTitle("Sessions for " + workshop.getTitle());

            // Set the scene with a size of 1000x800
            Scene scene = new Scene(root, 1000, 800); // Width: 1000, Height: 800
            stage.setScene(scene);

            // Block interaction with the main window (optional)
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(userTable.getScene().getWindow());

            // Prevent resizing (optional)
            stage.setResizable(false);

            // Show the new window
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Sessions.fxml: " + e.getMessage());
        }
    }


    // Method to open the Add Workshop popup window
    @FXML
    private void openAddWorkShopForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/AddWorkshop.fxml"));
            Parent root = loader.load();

            // Create a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Add New Workshop");

            // Set a larger size (adjust as needed)
            Scene scene = new Scene(root, 700, 750); // Width: 700, Height: 800px
            stage.setScene(scene);

            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with main window
            stage.setResizable(true); // Prevent resizing
            stage.showAndWait(); // Wait until the form is closed

            // Refresh the table after adding a new workshop
            loadWorkshops();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to open the Edit Workshop popup window
    private void openEditWorkshopForm(workshop workshop) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/EditWorkshop.fxml"));

            Parent root = loader.load();

            // Pass the selected workshop to the controller
            EditWorkshopController controller = loader.getController();
            controller.setWorkshop(workshop,this);

            // Create a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Edit Workshop");

            // Set the scene
            Scene scene = new Scene(root, 700, 800); // Adjust size as needed
            stage.setScene(scene);

            // Block interaction with the main window
            stage.initModality(Modality.APPLICATION_MODAL);

            // Prevent resizing
            stage.setResizable(false);

            // Show the popup window and wait for it to close
            stage.showAndWait();

            // Refresh the table after editing
            loadWorkshops();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading EditWorkshop.fxml: " + e.getMessage());
        }
    }

    public void confirmAndDeleteWorkshop(workshop selectedWorkshop) {
        // Confirm deletion with a dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Workshop");
        alert.setHeaderText("Are you sure you want to delete this workshop?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Delete the workshop from the service and refresh the table
            workshopService.deleteWorkshop(selectedWorkshop.getId_workshop());

            // Remove the workshop from the ObservableList
            workshopList.remove(selectedWorkshop);

            userTable.setItems(filteredWorkshops); // Refresh table
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

    private void loadWorkshops() {
        // Fetch all workshops from the service
        List<workshop> workshops = workshopService.getAllWorkshops();

        // Convert the list of workshops to an ObservableList
        workshopList = FXCollections.observableArrayList(workshops);

        // Wrap the ObservableList in a FilteredList
        filteredWorkshops = new FilteredList<>(workshopList);

        // Set the items for the TableView
        userTable.setItems(filteredWorkshops);
    }

    public void refreshTable() {
        List<workshop> workshops = workshopService.getAllWorkshops();
        workshopList.clear();          // Clear existing data first
        workshopList.addAll(workshops); // Add new data
        userTable.refresh();           // Force UI update
    }



    @FXML
    private void handleClear(ActionEvent event) {
        searchField.clear();
        filterComboBox.getSelectionModel().clearSelection();
        loadWorkshops(); // Reload all workshops
    }



    @FXML
    private void handleCheckChart(ActionEvent event) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/WorkshopChart.fxml"));
            Parent chartRoot = loader.load();

            // Create a new Stage (popup window)
            Stage chartStage = new Stage();
            chartStage.setTitle("Workshop Statistics");

            // Set scene size to 1200x800
            Scene scene = new Scene(chartRoot, 1200, 700);
            chartStage.setScene(scene);

            // Show the popup0
            chartStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading WorkshopChart.fxml: " + e.getMessage());

            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load chart view");
            alert.setContentText("Error details: " + e.getMessage());
            alert.showAndWait();
        }
    }




}