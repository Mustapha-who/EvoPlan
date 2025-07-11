package evoplan.controllers.EventPlannerHomePage.Session;

import evoplan.controllers.EventPlannerHomePage.ReservationSession.ReservationSessionController;
import evoplan.entities.workshop.session; // Assuming the Session entity exists
import evoplan.entities.workshop.workshop; // Workshop entity
import evoplan.services.workshop.sessionService; // Assuming you have a session service class
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class SessionController implements Initializable {

    @FXML
    private TableView<session> sessionTable; // TableView to display sessions

    @FXML
    private TableColumn<session, Date> DateColumn;


    @FXML
    private TableColumn<session, Integer> idSessionColumn; // Column for ID Session

    @FXML
    private TableColumn<session, Date> startDateTimeColumn; // Column for Start Date and Time

    @FXML
    private TableColumn<session, Date> endDateTimeColumn; // Column for End Date and Time

    @FXML
    private TableColumn<session, Integer> participantCountColumn; // Column for Participant Count

    @FXML
    private TableColumn<session, Integer> idWorkshopColumn;

    @FXML
    private TableColumn<session, Integer> capacityColumn; // Column for Capacity

    @FXML
    private TableColumn<session, Void> actionColumn; // Column for Edit/Delete actions

    private workshop workshop; // Selected workshop (optional, for future use)
    private ObservableList<session> sessionList;
    private sessionService sessionService = new sessionService(); // Service to fetch sessions


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize sessionList
        sessionList = FXCollections.observableArrayList();

        // Initialize table columns
        idSessionColumn.setCellValueFactory(new PropertyValueFactory<>("id_session"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("date")); // Bind to "date"
        startDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateheuredeb")); // Bind to "dateheuredeb"
        endDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateheurefin")); // Bind to "dateheurefin"
        participantCountColumn.setCellValueFactory(new PropertyValueFactory<>("participant_count"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        idWorkshopColumn.setCellValueFactory(new PropertyValueFactory<>("id_workshop"));

        // Set up the action column with Edit and Delete buttons
        actionColumn.setCellFactory(param -> new TableCell<session, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setOnAction(event -> {
                    session selectedSession = getTableView().getItems().get(getIndex());
                    openEditSessionForm(selectedSession);
                });

                deleteButton.setOnAction(event -> {
                    session selectedSession = getTableView().getItems().get(getIndex());
                    confirmAndDeleteSession(selectedSession);
                });

                HBox hbox = new HBox(10, editButton, deleteButton);
                setGraphic(hbox);
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, editButton, deleteButton));
                }
            }
        });



        // Add double-click event listener to the TableView
        sessionTable.setRowFactory(tv -> {
            TableRow<session> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    session selectedSession = row.getItem(); // Get the selected session
                    openSessionsTab(selectedSession); // Open the session details tab
                }
            });
            return row;
        });


        // Load all sessions into the table
        loadSessions();
    }



    private void openSessionsTab(session selectedSession) {
        try {
            // Load the Sessions.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/ReservationSession.fxml"));
            Parent root = loader.load();

            // Pass the selected session to the ReservationSessionController
            ReservationSessionController reservationSessionController = loader.getController();
            reservationSessionController.setSession(selectedSession); // Pass the selected session

            // Create a new Stage (window)
            Stage stage = new Stage();
            stage.setTitle("Session Details: " + selectedSession.getId_session());

            // Set the scene with a size of 800x600
            Scene scene = new Scene(root, 900, 700);
            stage.setScene(scene);

            // Block interaction with the main window (optional)
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(sessionTable.getScene().getWindow());

            // Prevent resizing (optional)
            stage.setResizable(false);

            // Show the new window
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading ReservationSession.fxml: " + e.getMessage());
        }
    }


    private void openEditSessionForm(session session) {
        try {
            // Load the EditSession.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/EditSession.fxml"));
            Parent root = loader.load();

            // Pass the selected session to the EditSessionController
            EditSessionController editSessionController = loader.getController();
            editSessionController.setSession(session, this);

            // Create a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Edit Session");

            // Set the scene with a size of 600x700
            Scene scene = new Scene(root, 600, 700); // Width: 600px, Height: 700px
            stage.setScene(scene);

            // Block interaction with the main window (optional)
            stage.initModality(Modality.APPLICATION_MODAL);

            // Prevent resizing (optional)
            stage.setResizable(false);

            // Show the popup window and wait for it to close
            stage.showAndWait();

            // Refresh the table after editing
            loadSessions();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading EditSession.fxml: " + e.getMessage());
        }
    }



    // Method to set the selected workshop
    public void setWorkshop(workshop workshop) {
        this.workshop = workshop;
        loadSessionsForWorkshop(); // Load sessions for the selected workshop
    }


    // Method to load sessions for the selected workshop
    private void loadSessionsForWorkshop() {
        if (workshop != null) {
            // Fetch sessions for the selected workshop
            List<session> sessions = sessionService.getSessionsByWorkshopId(workshop.getId_workshop());

            // Convert the list of sessions to an ObservableList
            ObservableList<session> sessionList = FXCollections.observableArrayList(sessions);

            // Set the items for the TableView
            sessionTable.setItems(sessionList);
        }
    }

    // Method to load all sessions
    private void loadSessions() {
        if (workshop != null) {
            // Fetch sessions for the selected workshop
            List<session> sessions = sessionService.getSessionsByWorkshopId(workshop.getId_workshop());

            // Update the sessionList with the new data
            sessionList.clear(); // Clear the existing data
            sessionList.addAll(sessions); // Add the new data

            // Set the items for the TableView
            sessionTable.setItems(sessionList);
        }
    }

    @FXML
    private void openAddSessionForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/AddSession.fxml"));
            Parent root = loader.load();

            // Pass the selected workshop ID to the AddSessionDialogController
            AddSessionDialogController addSessionController = loader.getController();
            addSessionController.setWorkshopId(workshop.getId_workshop()); // Pass the workshop ID

            // Create a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Add New Session");

            // Set a larger size (adjust as needed)
            Scene scene = new Scene(root, 600, 700); // Width: 600px, Height: 700px
            stage.setScene(scene);

            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with main window
            stage.setResizable(false); // Prevent resizing
            stage.showAndWait(); // Wait until the form is closed

            // Refresh the table after adding a new session
            refreshTable(); // Call refreshTable to update the table
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Method to confirm and delete a session
    private void confirmAndDeleteSession(session session) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this session?");
        alert.setContentText("This action cannot be undone.");

        // Show the confirmation dialog and wait for user input
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete the session from the database
                sessionService.deleteSession(session.getId_session());

                // Remove the session from the TableView
                sessionTable.getItems().remove(session);
            }
        });
    }


    public void refreshTable() {
        if (workshop != null) {
            // Fetch sessions for the selected workshop
            List<session> sessions = sessionService.getSessionsByWorkshopId(workshop.getId_workshop());

            // Update the sessionList with the new data
            sessionList.clear(); // Clear the existing data
            sessionList.addAll(sessions); // Add the new data

            // Refresh the TableView
            sessionTable.setItems(sessionList);
            sessionTable.refresh(); // Force the TableView to update
        }
    }

}