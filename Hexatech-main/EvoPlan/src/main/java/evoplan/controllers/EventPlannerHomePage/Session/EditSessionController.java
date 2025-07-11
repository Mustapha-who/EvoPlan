package evoplan.controllers.EventPlannerHomePage.Session;

import evoplan.entities.workshop.session;
import evoplan.services.workshop.sessionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EditSessionController {

    @FXML
    private TextField idSessionField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private ComboBox<String> startTimeComboBox;

    @FXML
    private ComboBox<String> endTimeComboBox;

    @FXML
    private TextField capacityField;

    @FXML
    private Label startDateError;

    @FXML
    private Label startDateTimeError;

    @FXML
    private Label endDateTimeError;

    @FXML
    private Label capacityError;

    @FXML
    private Button updateButton;

    @FXML
    private Button cancelButton;

    private session session;

    private final sessionService sessionService = new sessionService();

    private SessionController parentController;

    public void setSession(session session, SessionController parentController) {
        this.session = session;
        this.parentController = parentController;

        // Set the date from the session_date field
        if (session.getDate() != null) {
            startDatePicker.setValue(session.getDate().toLocalDate()); // Convert java.sql.Date to LocalDate
        } else {
            startDatePicker.setValue(LocalDate.now()); // Default to today's date if session_date is null
        }

        // Set the start time from the dateheuredeb field
        if (session.getDateheuredeb() != null) {
            String startTime = session.getDateheuredeb().substring(0, 5); // Extract "HH:mm" from "HH:mm:ss"
            startTimeComboBox.setValue(startTime);
        }

        // Set the end time from the dateheurefin field
        if (session.getDateheurefin() != null) {
            String endTime = session.getDateheurefin().substring(0, 5); // Extract "HH:mm" from "HH:mm:ss"
            endTimeComboBox.setValue(endTime);
        }

        // Set other fields
        capacityField.setText(String.valueOf(session.getCapacity()));

        // Populate time ComboBoxes
        populateTimeComboBox(startTimeComboBox);
        populateTimeComboBox(endTimeComboBox);
    }

    private void populateTimeComboBox(ComboBox<String> comboBox) {
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 30) { // Increment by 30 minutes (00 and 30)
                String time = String.format("%02d:%02d", h, m); // Format as "HH:mm"
                comboBox.getItems().add(time);
            }
        }
    }

    @FXML
    private void handleUpdate() {
        resetErrorMessages();
        boolean valid = true;

        // Retrieve selected date and time
        LocalDate startDate = startDatePicker.getValue();
        LocalTime startTime = LocalTime.parse(startTimeComboBox.getValue() + ":00"); // Append ":00" to match "HH:mm:ss" format
        LocalTime endTime = LocalTime.parse(endTimeComboBox.getValue() + ":00"); // Append ":00" to match "HH:mm:ss" format
        String capacity = capacityField.getText();

        // Validate Start Date and Time
        if (startDate == null || startTime == null) {
            startDateTimeError.setText("Start Date and Time are required.");
            startDateTimeError.setVisible(true);
            valid = false;
        }

        // Validate End Time
        if (endTime == null) {
            endDateTimeError.setText("End Time is required.");
            endDateTimeError.setVisible(true);
            valid = false;
        }

        // Validate Capacity
        if (capacity.trim().isEmpty()) {
            capacityError.setText("Capacity is required.");
            capacityError.setVisible(true);
            valid = false;
        } else if (!isValidNumber(capacity)) {
            capacityError.setText("Capacity must be a number.");
            capacityError.setVisible(true);
            valid = false;
        }

        // Validate Session Date Against Workshop Date Range
        if (startDate != null) {
            LocalDate[] workshopDateRange = sessionService.getWorkshopDateRange(session.getId_workshop());

            if (workshopDateRange != null && workshopDateRange.length == 2) {
                LocalDate workshopStartDate = workshopDateRange[0];
                LocalDate workshopEndDate = workshopDateRange[1];

                if (startDate.isBefore(workshopStartDate)) {
                    startDateError.setText("Session date cannot be before the workshop start date.");
                    startDateError.setVisible(true);
                    valid = false;
                } else if (startDate.isAfter(workshopEndDate)) {
                    startDateError.setText("Session date cannot be after the workshop end date.");
                    startDateError.setVisible(true);
                    valid = false;
                }
            } else {
                startDateError.setText("Workshop date range could not be retrieved.");
                startDateError.setVisible(true);
                valid = false;
            }
        }

        // Validate Start Time is before End Time
        if (startTime != null && endTime != null) {
            if (!startTime.isBefore(endTime)) {
                startDateTimeError.setText("Start Time must be before End Time.");
                startDateTimeError.setVisible(true);
                valid = false;
            }
        }

        // Validate Overlapping Sessions
        if (valid && startDate != null && startTime != null && endTime != null) {
            if (sessionService.isSessionOverlapping(session.getId_workshop(), startDate, startTime, endTime, session.getId_session())) {
                startDateTimeError.setText("Session time overlaps with an existing session.");
                startDateTimeError.setVisible(true);
                valid = false;
            }
        }

        // Validate Capacity
        if (valid && !capacity.isEmpty() && isValidNumber(capacity)) {
            int newSessionCapacity = Integer.parseInt(capacity);
            int totalSessionCapacity = sessionService.getTotalSessionCapacity(session.getId_workshop());
            int workshopCapacity = sessionService.getWorkshopCapacity(session.getId_workshop());

            // Subtract the old session capacity (if updating) and add the new capacity
            int oldSessionCapacity = session.getCapacity();
            int updatedTotalCapacity = totalSessionCapacity - oldSessionCapacity + newSessionCapacity;

            if (updatedTotalCapacity > workshopCapacity) {
                capacityError.setText("Total session capacity exceeds workshop capacity.");
                capacityError.setVisible(true);
                valid = false;
            }
        }

        // If there are validation errors, do not submit
        if (!valid) return;

        // Update session data
        this.session.setDate(Date.valueOf(startDate));
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(startDate, endTime);

        this.session.setDateheuredeb(startDateTime.format(timestampFormatter)); // Set as formatted String
        this.session.setDateheurefin(endDateTime.format(timestampFormatter)); // Set as formatted String
        this.session.setCapacity(Integer.parseInt(capacity));

        // Save the updated session
        sessionService.updateSession(this.session);

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
        startDateError.setVisible(false);
        startDateTimeError.setVisible(false);
        endDateTimeError.setVisible(false);
        capacityError.setVisible(false);
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