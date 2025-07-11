package evoplan.controllers.EventPlannerHomePage.ReservationSession;

import evoplan.entities.user.User;
import evoplan.entities.workshop.reservation_session;
import evoplan.entities.workshop.session;
import evoplan.services.workshop.reservationSessionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class ReservationSessionController {
    @FXML
    private TableView<reservation_session> reservationTable;

    @FXML
    private TableColumn<reservation_session, Integer> idReservationColumn;

    @FXML
    private TableColumn<reservation_session, Integer> idSessionColumn;

    @FXML
    private TableColumn<reservation_session, String> nameColumn; // For name from user table

    @FXML
    private TableColumn<reservation_session, String> emailColumn; // For email from user table

    @FXML
    private TableColumn<reservation_session, Void> actionColumn;


    @FXML
    private TableColumn<reservation_session, Integer> idParticipantsColumn;

    private final reservationSessionService reservationService = new reservationSessionService();
    private session currentSession;
    private int currentSessionId; // Store the current session ID

    public void initialize() {
        setupTable();
        loadReservations();
        setupActionColumn(); // Set up the action column
    }


    private void openEditDialog(reservation_session reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Workshop/EditReservationSession.fxml"));
            Parent root = loader.load();

            // Get the controller from the FXML
            EditReservationSessionController editController = loader.getController();
            editController.setReservationSession(reservation, this); // Pass reservation and parent controller

            // Create and show the dialog
            Stage stage = new Stage();
            stage.setTitle("Edit Reservation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupActionColumn() {
        // Define a cell factory for the action column
        Callback<TableColumn<reservation_session, Void>, TableCell<reservation_session, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<reservation_session, Void> call(final TableColumn<reservation_session, Void> param) {
                        final TableCell<reservation_session, Void> cell = new TableCell<>() {
                            private final Button editButton = new Button("Edit");
                            private final Button deleteButton = new Button("Delete");

                            {
                                // Handle edit button click
                                editButton.setOnAction(event -> {
                                    reservation_session reservation = getTableView().getItems().get(getIndex());
                                    openEditDialog(reservation); // Open the edit dialog
                                });

                                // Handle delete button click
                                deleteButton.setOnAction(event -> {
                                    reservation_session reservation = getTableView().getItems().get(getIndex());
                                    deleteReservation(reservation.getId()); // Call delete method
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    // Add both buttons to an HBox
                                    HBox buttons = new HBox(5, editButton, deleteButton);
                                    setGraphic(buttons);
                                }
                            }
                        };
                        return cell;
                    }
                };

        actionColumn.setCellFactory(cellFactory);
    }

    // Use the confirmation dialog
    private void deleteReservation(int reservationId) {
        reservation_session reservation = reservationTable.getItems().stream()
                .filter(r -> r.getId() == reservationId)
                .findFirst()
                .orElse(null);

        if (reservation != null) {
            confirmAndDeleteReservation(reservation); // Show confirmation dialog
        }
    }

    private void confirmAndDeleteReservation(reservation_session reservation) {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this reservation?");
        alert.setContentText("This action cannot be undone.");

        // Show the confirmation dialog and wait for user input
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Delete the reservation from the database
                reservationService.deleteReservation(reservation.getId());

                // Remove the reservation from the TableView
                reservationTable.getItems().remove(reservation);
            }
        });
    }

    private void setupTable() {
        idReservationColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idSessionColumn.setCellValueFactory(new PropertyValueFactory<>("id_session"));
        idParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("id_participants"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name")); // Bind to name
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email")); // Bind to email
    }

    void loadReservations() {
        if (currentSession == null) {
            System.out.println("Current session is not set.");
            return;
        }

        System.out.println("Loading reservations for session ID: " + currentSessionId);  // Debugging line

        // Fetch reservations for the current session ID
        ObservableList<reservation_session> sessionReservations = reservationService.getAllReservations(currentSessionId);

        // Fetch user data for the current session ID
        ObservableList<String[]> userDataList = reservationService.getUserDataBySessionId(currentSessionId);




        // Combine the data
        for (int i = 0; i < sessionReservations.size(); i++) {
            reservation_session reservation = sessionReservations.get(i);
            String[] userData = userDataList.get(i);

            // Set the name and email from the user data
            reservation.setName(userData[0]); // Set name
            reservation.setEmail(userData[1]); // Set email
        }

        // Set the combined data in the table
        reservationTable.setItems(sessionReservations);
        reservationTable.refresh();
    }

    // This method came from sessioncontroller
    public void setSession(session session) {
        this.currentSession = session;
        this.currentSessionId = session.getId_session(); // Store the session ID
        populateSessionDetails();
        loadReservations(); // Reload reservations for the new session
    }

    private void populateSessionDetails() {
        // You can add logic here to update the UI with session details
    }
}