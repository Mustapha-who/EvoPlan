package evoplan.controllers.EventPlannerHomePage.Session;

import evoplan.entities.workshop.session;
import evoplan.services.workshop.sessionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AddSessionDialogController {

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private ComboBox<LocalTime> startTimeComboBox;

    @FXML
    private ComboBox<LocalTime> endTimeComboBox;

    @FXML
    private TextField capacityField;

    @FXML
    private Label startDateTimeError;

    @FXML
    private Label endDateTimeError;

    @FXML
    private Label capacityError;

    @FXML
    private Label startDateError; // New error label for date validation

    @FXML
    private Button submitButton;

    private int idWorkshop;
    private final sessionService sessionService = new sessionService();

    public void setWorkshopId(int idWorkshop) {
        this.idWorkshop = idWorkshop;
    }

    @FXML
    private void initialize() {
        populateTimeComboBox(startTimeComboBox);
        populateTimeComboBox(endTimeComboBox);
    }

    private void populateTimeComboBox(ComboBox<LocalTime> comboBox) {
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 30) {
                comboBox.getItems().add(LocalTime.of(h, m));
            }
        }
    }

    @FXML
    private void onSubmit() {
        LocalDate startDate = startDatePicker.getValue();
        LocalTime startTime = startTimeComboBox.getValue();
        LocalTime endTime = endTimeComboBox.getValue();
        String capacity = capacityField.getText();

        clearErrorStyles();
        boolean hasError = false;

        // Validate Start Date and Time
        if (startDate == null || startTime == null) {
            startDateTimeError.setText("Start Date and Time are required.");
            startDateTimeError.setVisible(true);
            hasError = true;
        }

        // Validate End Time
        if (endTime == null) {
            endDateTimeError.setText("End Time is required.");
            endDateTimeError.setVisible(true);
            hasError = true;
        }

        // Validate Capacity
        if (capacity.isEmpty() || !isValidNumber(capacity)) {
            capacityError.setText("Valid capacity required.");
            capacityError.setVisible(true);
            hasError = true;
        } else if (Integer.parseInt(capacity) <= 0) {
            capacityError.setText("Capacity must be a positive number.");
            capacityError.setVisible(true);
            hasError = true;
        }

        // Validate Session Date Against Workshop Date Range
        if (startDate != null) {
            LocalDate[] workshopDateRange = sessionService.getWorkshopDateRange(idWorkshop);

            if (workshopDateRange != null && workshopDateRange.length == 2) {
                LocalDate workshopStartDate = workshopDateRange[0];
                LocalDate workshopEndDate = workshopDateRange[1];

                if (startDate.isBefore(workshopStartDate) || startDate.isAfter(workshopEndDate)) {
                    startDateError.setText("Session date must be within the workshop date range.");
                    startDateError.setVisible(true);
                    hasError = true;
                }
            } else {
                startDateError.setText("Workshop date range could not be retrieved.");
                startDateError.setVisible(true);
                hasError = true;
            }
        }

        // Validate Start Time is before End Time
        if (startTime != null && endTime != null) {
            if (!startTime.isBefore(endTime)) {
                startDateTimeError.setText("Start Time must be before End Time.");
                startDateTimeError.setVisible(true);
                hasError = true;
            }
        }

        // Validate Overlapping Sessions
        if (!hasError && startDate != null && startTime != null && endTime != null) {
            if (sessionService.isSessionOverlapping(idWorkshop, startDate, startTime, endTime, 0)) {
                startDateTimeError.setText("Session time overlaps with an existing session.");
                startDateTimeError.setVisible(true);
                hasError = true;
            }
        }

        // Validate Capacity
        if (!hasError && !capacity.isEmpty() && isValidNumber(capacity)) {
            int newSessionCapacity = Integer.parseInt(capacity);
            int totalSessionCapacity = sessionService.getTotalSessionCapacity(idWorkshop);
            int workshopCapacity = sessionService.getWorkshopCapacity(idWorkshop);

            if (totalSessionCapacity + newSessionCapacity > workshopCapacity) {
                capacityError.setText("Total session capacity exceeds workshop capacity.");
                capacityError.setVisible(true);
                hasError = true;
            }
        }

        // If there are validation errors, do not submit
        if (hasError) {
            System.err.println("Validation errors detected. Submission aborted.");
            return;
        }

        // Create and save the new session
        session newSession = new session();
        newSession.setDate(Date.valueOf(startDate));

        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(startDate, endTime);

        newSession.setDateheuredeb(startDateTime.format(timestampFormatter)); // Set as formatted String
        newSession.setDateheurefin(endDateTime.format(timestampFormatter)); // Set as formatted String

        newSession.setCapacity(Integer.parseInt(capacity));
        newSession.setId_workshop(idWorkshop);

        sessionService.addSession(newSession);
        submitButton.getScene().getWindow().hide();
    }

    private void clearErrorStyles() {
        startDateTimeError.setVisible(false);
        endDateTimeError.setVisible(false);
        capacityError.setVisible(false);
        startDateError.setVisible(false); // Clear the new error label
    }

    private boolean isValidNumber(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}