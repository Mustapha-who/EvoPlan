package evoplan.controllers.EventPlannerHomePage.Workshop;

import evoplan.controllers.DisplayEventsController;
import evoplan.entities.workshop.workshop;
import evoplan.services.event.EventService;
import evoplan.services.workshop.workshopService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class EditWorkshopController {

    @FXML
    private TextField titleField;
    @FXML
    private Label titleError;

    @FXML
    private DatePicker datePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label dateError;

    @FXML
    private Label endDateError;

    @FXML
    private TextField instructorField;
    @FXML
    private Label instructorError;

    @FXML
    private TextField eventField; // Changed from sessionField to eventField
    @FXML
    private Label eventError; // Changed from sessionError to eventError


    @FXML
    private TextField capacityField;
    @FXML
    private Label capacityError;

    @FXML
    private TextField locationField;
    @FXML
    private Label locationError;

    @FXML
    private TextField descriptionField;
    @FXML
    private Label descriptionError;

    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;

    private workshop workshop;

    private final workshopService workshopService = new workshopService();

    private WorkshopController parentController;
    public void setWorkshop(workshop workshop, WorkshopController parentController) {
        this.workshop = workshop;
        this.parentController = parentController;

        // Convert java.sql.Date to LocalDate for end date
        java.sql.Date sqlDate = new java.sql.Date(workshop.getDate().getTime());
        LocalDate localDate = sqlDate.toLocalDate();

        // Convert java.sql.Date to LocalDate for end date
        java.sql.Date sqlEndDate = new java.sql.Date(workshop.getEnddate().getTime());
        LocalDate endLocalDate = sqlEndDate.toLocalDate();

        // Initialize fields with workshop data
        titleField.setText(workshop.getTitle());
        datePicker.setValue(localDate);
        endDatePicker.setValue(endLocalDate);
        instructorField.setText(String.valueOf(workshop.getInstructor()));
        eventField.setText(String.valueOf(workshop.getId_event())); // Changed sessionField to eventField
        capacityField.setText(String.valueOf(workshop.getCapacity()));
        locationField.setText(workshop.getLocation());
        descriptionField.setText(workshop.getDescription());
    }

    boolean valid = true;
    @FXML
    private void handleUpdate() {
        resetErrorMessages(); // Clear previous error messages
        valid = true; // Reset validation flag

        // Retrieve selected date
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            dateError.setText("Date is required.");
            dateError.setVisible(true);
            valid = false;
        }

        // Retrieve selected end date
        LocalDate selectedEndDate = endDatePicker.getValue();
        if (selectedEndDate == null) {
            endDateError.setText("End Date is required.");
            endDateError.setVisible(true);
            valid = false;
        }

        // Validate that end date is after start date
        if (selectedDate != null && selectedEndDate != null && selectedEndDate.isBefore(selectedDate)) {
            endDateError.setText("End Date must be after Start Date.");
            endDateError.setVisible(true);
            valid = false;
        }

        // Instructor validation
        if (instructorField.getText().trim().isEmpty()) {
            instructorError.setText("Instructor ID is required");
            instructorError.setVisible(true);
            valid = false;
        } else if (!isValidNumber(instructorField.getText())) {
            instructorError.setText("Instructor ID must be a number");
            instructorError.setVisible(true);
            valid = false;
        }

        // Event validation
        if (eventField.getText().trim().isEmpty()) {
            eventError.setText("Event ID is required");
            eventError.setVisible(true);
            valid = false;
        } else if (!isValidNumber(eventField.getText())) {
            eventError.setText("Event ID must be a number");
            eventError.setVisible(true);
            valid = false;
        }


        // Capacity validation
        if (capacityField.getText().trim().isEmpty()) {
            capacityError.setText("Capacity is required");
            capacityError.setVisible(true);
            valid = false;
        } else if (!isValidNumber(capacityField.getText())) {
            capacityError.setText("Capacity must be a number");
            capacityError.setVisible(true);
            valid = false;
        }

        // Location validation
        if (locationField.getText().trim().isEmpty()) {
            locationError.setText("Location is required");
            locationError.setVisible(true);
            valid = false;
        }

        // Description validation
        if (descriptionField.getText().trim().isEmpty()) {
            descriptionError.setText("Description is required");
            descriptionError.setVisible(true);
            valid = false;
        }

        // Fetch event dates
        int eventId = Integer.parseInt(eventField.getText());
        LocalDate[] eventDates = workshopService.getEventDates(eventId);
        if (eventDates == null) {
            eventError.setText("Event not found.");
            eventError.setVisible(true);
            valid = false;
        } else {
            LocalDate eventStartDate = eventDates[0];
            LocalDate eventEndDate = eventDates[1];

            // Validate workshop dates against event dates
            if (selectedDate != null && (selectedDate.isBefore(eventStartDate) || selectedDate.isAfter(eventEndDate))) {
                dateError.setText("Workshop date must be within the event's date range.");
                dateError.setVisible(true);
                valid = false;
            }

            if (selectedEndDate != null && (selectedEndDate.isBefore(eventStartDate) || selectedEndDate.isAfter(eventEndDate))) {
                endDateError.setText("Workshop end date must be within the event's date range.");
                endDateError.setVisible(true);
                valid = false;
            }
        }

        if (!valid) return; // Stop execution if validation fails

        // Update workshop data
        this.workshop.setTitle(titleField.getText());

        // Convert LocalDate to java.sql.Date
        java.sql.Date sqlDate = java.sql.Date.valueOf(selectedDate);
        java.sql.Date sqlEndDate = java.sql.Date.valueOf(selectedEndDate);

        // Update workshop object
        this.workshop.setDate(sqlDate);
        this.workshop.setEnddate(sqlEndDate);
        this.workshop.setInstructor(Integer.parseInt(instructorField.getText()));
        this.workshop.setId_event(eventId);
        this.workshop.setCapacity(Integer.parseInt(capacityField.getText()));
        this.workshop.setLocation(locationField.getText());
        this.workshop.setDescription(descriptionField.getText());

        // Save the updated workshop
        workshopService.updateWorkshop(this.workshop);

        // Refresh the parent controller's table
        parentController.refreshTable();

        // Close the dialog
        ((Stage) updateButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void resetErrorMessages() {
        titleError.setVisible(false);
        dateError.setVisible(false);
        endDateError.setVisible(false);
        instructorError.setVisible(false);
        eventError.setVisible(false); // Changed from sessionError to eventError
        capacityError.setVisible(false);
        locationError.setVisible(false);
        descriptionError.setVisible(false);
    }

    private boolean isValidDate(String date) {
        try {
            Date.valueOf(date); // Check if the date is valid
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isValidNumber(String input) {
        try {
            Integer.parseInt(input); // Check if the input is a valid number
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
