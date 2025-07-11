package evoplan.controllers.EventPlannerHomePage.ReservationSession;

import evoplan.entities.workshop.reservation_session;
import evoplan.services.workshop.reservationSessionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class EditReservationSessionController {

    @FXML
    private TextField nameField; // Updated from firstNameField
    @FXML
    private Label nameError; // Updated from firstNameError

    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;

    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;

    private reservation_session reservation_session;

    private final reservationSessionService reservationSessionService = new reservationSessionService();
    private ReservationSessionController parentController;

    // Initialize with reservation session and parent controller
    public void setReservationSession(reservation_session reservationSession, ReservationSessionController parentController) {
        this.reservation_session = reservationSession;
        this.parentController = parentController;

        // Initialize fields with reservation session data

        nameField.setText(reservationSession.getName()); // Updated from firstNameField

        emailField.setText(reservationSession.getEmail());
    }

    boolean valid = true;

    @FXML
    private void handleUpdate() {
        resetErrorMessages(); // Clear previous error messages
        valid = true; // Reset validation flag

        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            nameError.setText("Name is required");
            nameError.setVisible(true);
            valid = false;
        }

        // Email validation
        if (emailField.getText().trim().isEmpty()) {
            emailError.setText("Email is required");
            emailError.setVisible(true);
            valid = false;
        } else if (!isValidEmail(emailField.getText())) {
            emailError.setText("Invalid email format");
            emailError.setVisible(true);
            valid = false;
        }

        if (!valid) return; // Stop execution if validation fails

        // Update reservation session data

        this.reservation_session.setName(nameField.getText()); // Updated from firstNameField

        this.reservation_session.setEmail(emailField.getText());

        // Save the updated reservation session
        reservationSessionService.updateReservationSession(this.reservation_session);

        // Refresh the parent controller's table
        parentController.loadReservations(); // Reload reservations

        // Close the dialog
        ((Stage) updateButton.getScene().getWindow()).close();
    }


    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    // Reset error messages
    private void resetErrorMessages() {
        nameError.setVisible(false); // Updated from firstNameError
        emailError.setVisible(false);
    }

    // Validate email format
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}