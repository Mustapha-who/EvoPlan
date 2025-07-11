package evoplan.controllers.EventPlannerHomePage.Workshop;

import evoplan.controllers.EventPlannerHomePage.OpenRouterAPI;
import evoplan.entities.user.Instructor;
import evoplan.entities.workshop.workshop;
import evoplan.services.workshop.workshopService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Button;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;

public class AddWorkshopDialogController {

    // Workshop Form Fields
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> instructorComboBox;
    @FXML private ComboBox<String> eventComboBox;
    @FXML private TextField capacityField;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionField;
    @FXML private Button submitButton;

    // Error Labels
    @FXML private Label titleError;
    @FXML private Label dateError;
    @FXML private Label endDateError;
    @FXML private Label instructorError;
    @FXML private Label eventError;
    @FXML private Label capacityError;
    @FXML private Label locationError;
    @FXML private Label descriptionError;

    // Chatbot Fields
    @FXML private TextField chatInputField;
    @FXML private Button generateButton;
    @FXML private TextArea chatResponseArea;
    @FXML private ProgressIndicator loadingSpinner;


    private workshopService workshopService = new workshopService();

    @FXML
    private void initialize() {
        // Fetch instructors from the service
        List<Instructor> instructors = workshopService.getAllInstructors();

        // Format the data for display (e.g., "ID - Name")
        ObservableList<String> instructorOptions = FXCollections.observableArrayList();
        for (Instructor instructor : instructors) {
            String instructorName = workshopService.getInstructorNameById(instructor.getId());
            instructorOptions.add(instructor.getId() + " - " + instructorName);
        }

        // Populate the instructor ComboBox
        instructorComboBox.setItems(instructorOptions);

        // Fetch events from the service (assuming you have a method to get events)
        List<String> events = workshopService.getAllEvents(); // Replace with your method to fetch events

        // Format the data for display (e.g., "ID - Event Name")
        ObservableList<String> eventOptions = FXCollections.observableArrayList();
        for (String event : events) {
            eventOptions.add(event); // Adjust this based on your event data structure
        }

        // Populate the event ComboBox
        eventComboBox.setItems(eventOptions);
    }

    @FXML
    private void onSubmit() {
        // Retrieve input values
        String title = titleField.getText();
        LocalDate date = datePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String selectedInstructor = instructorComboBox.getValue();
        String selectedEvent = eventComboBox.getValue();
        String capacity = capacityField.getText();
        String location = locationField.getText();
        String description = descriptionField.getText();

        // Clear previous error messages
        clearErrorStyles();

        boolean hasError = false;

        // Validate Title
        if (title.isEmpty()) {
            titleError.setText("Title is required.");
            titleError.setVisible(true);
            hasError = true;
        }

        // Validate Date
        if (date == null) {
            dateError.setText("Date is required.");
            dateError.setVisible(true);
            hasError = true;
        }

        // Validate End Date
        if (endDate == null) {
            endDateError.setText("End Date is required.");
            endDateError.setVisible(true);
            hasError = true;
        }

        // Validate Instructor
        if (selectedInstructor == null || selectedInstructor.isEmpty()) {
            instructorError.setText("Please select an instructor.");
            instructorError.setVisible(true);
            hasError = true;
        }

        // Validate Event
        if (selectedEvent == null || selectedEvent.isEmpty()) {
            eventError.setText("Please select an event.");
            eventError.setVisible(true);
            hasError = true;
        }

        // Validate Capacity
        if (capacity.isEmpty()) {
            capacityError.setText("Capacity is required.");
            capacityError.setVisible(true);
            hasError = true;
        } else if (!isValidNumber(capacity)) {
            capacityError.setText("Capacity must be a number.");
            capacityError.setVisible(true);
            hasError = true;
        }

        // Validate Location
        if (location.isEmpty()) {
            locationError.setText("Location is required.");
            locationError.setVisible(true);
            hasError = true;
        }

        // Validate Description
        if (description.isEmpty()) {
            descriptionError.setText("Description is required.");
            descriptionError.setVisible(true);
            hasError = true;
        }

        // If there are validation errors, do not submit
        if (hasError) {
            return;
        }

        // Extract IDs from the selected values
        int instructorId = Integer.parseInt(selectedInstructor.split(" - ")[0]);
        int eventId = Integer.parseInt(selectedEvent.split(" - ")[0]);

        // Fetch event dates
        LocalDate[] eventDates = workshopService.getEventDates(eventId);
        if (eventDates == null) {
            eventError.setText("Event not found.");
            eventError.setVisible(true);
            return;
        }

        LocalDate eventStartDate = eventDates[0];
        LocalDate eventEndDate = eventDates[1];

        // Validate workshop dates against event dates
        if (date.isBefore(eventStartDate) || date.isAfter(eventEndDate)) {
            dateError.setText("Workshop date must be within the event's date range.");
            dateError.setVisible(true);
            hasError = true;
        }

        if (endDate.isBefore(eventStartDate) || endDate.isAfter(eventEndDate)) {
            endDateError.setText("Workshop end date must be within the event's date range.");
            endDateError.setVisible(true);
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Proceed with submission if no errors
        workshop newWorkshop = new workshop();
        newWorkshop.setTitle(title);
        newWorkshop.setDate(Date.valueOf(date)); // Convert LocalDate to SQL Date
        newWorkshop.setEnddate(endDate != null ? Date.valueOf(endDate) : null); // Handle NULL enddate
        newWorkshop.setInstructor(instructorId);
        newWorkshop.setId_event(eventId);
        newWorkshop.setCapacity(Integer.parseInt(capacity));
        newWorkshop.setLocation(location);
        newWorkshop.setDescription(description);

        // Add the workshop to the database
        workshopService.addWorkshop(newWorkshop);

        // Close the dialog
        submitButton.getScene().getWindow().hide();
    }

    private void clearErrorStyles() {
        // Reset error message visibility
        titleError.setVisible(false);
        dateError.setVisible(false);
        endDateError.setVisible(false);
        instructorError.setVisible(false);
        eventError.setVisible(false);
        capacityError.setVisible(false);
        locationError.setVisible(false);
        descriptionError.setVisible(false);
    }

    private boolean isValidNumber(String input) {
        try {
            Integer.parseInt(input); // Check if the input is a valid number
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }



    @FXML
    private void handleGenerateButtonClick() {
        String question = chatInputField.getText();
        if (question.isEmpty()) {
            chatResponseArea.setText("Please enter a question.");
            return;
        }

        // Show the loading spinner
        loadingSpinner.setVisible(true);
        generateButton.setDisable(true);

        // Check if the user wants to generate a random workshop
        String[] keywords = {"create", "generate", "suggest", "plan", "give", "workshop"};
        boolean shouldGenerateWorkshop = false;
        for (String keyword : keywords) {
            if (question.toLowerCase().contains(keyword)) {
                shouldGenerateWorkshop = true;
                break;
            }
        }

        if (shouldGenerateWorkshop) {
            // Generate random health-related workshop data
            generateRandomHealthWorkshop().thenAccept(randomWorkshop -> {
                // Auto-fill the form fields
                javafx.application.Platform.runLater(() -> {
                    autoFillForm(randomWorkshop);
                    chatResponseArea.setText("Health workshop data generated and form auto-filled!");
                    loadingSpinner.setVisible(false);
                    generateButton.setDisable(false);
                });
            }).exceptionally(ex -> {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    chatResponseArea.setText("Error: " + ex.getMessage());
                    loadingSpinner.setVisible(false);
                    generateButton.setDisable(false);
                });
                return null;
            });
        } else {
            // Call the OpenRouter API for other questions
            String model = "deepseek/deepseek-chat:free";
            CompletableFuture<String> apiResponse = OpenRouterAPI.sendRequest(model, question);

            // Handle the API response
            apiResponse.thenAccept(response -> {
                javafx.application.Platform.runLater(() -> {
                    chatResponseArea.setText(response);
                    loadingSpinner.setVisible(false);
                    generateButton.setDisable(false);
                });
            }).exceptionally(ex -> {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    chatResponseArea.setText("Error: " + ex.getMessage());
                    loadingSpinner.setVisible(false);
                    generateButton.setDisable(false);
                });
                return null;
            });
        }
    }



    private CompletableFuture<workshop> generateRandomHealthWorkshop() {
        workshop randomWorkshop = new workshop();

        // Get the user's input from the chatInputField
        String userInput = chatInputField.getText().trim();

        // Default prompt for generating title
        String defaultPrompt = "Generate a short, health-related workshop title (2-5 words maximum) with a focus on wellness and prevention.";

        // Combine the user's input with the default prompt
        String promptForTitle = userInput.isEmpty()
                ? defaultPrompt
                : "Generate a short, health-related workshop title (2-5 words maximum) based on the following: " + userInput;

        // Generate the title based on the combined prompt
        return retryApiCall(promptForTitle, 3) // Retry up to 3 times to generate a title
                .thenCompose(title -> {
                    // Clean up and format the title (capitalize first letter, etc.)
                    String formattedTitle = formatTitle(title);
                    randomWorkshop.setTitle(formattedTitle);  // Set the formatted title


                    // Generate random capacity (between 20 and 100)
                    randomWorkshop.setCapacity((int) (Math.random() * 80) + 20);

                    String[] cities = {
                            "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", "Gabès", "Ariana",
                            "Gafsa", "Monastir", "Nabeul", "Tataouine", "Tozeur", "Ben Arous",
                            "Sidi Bouzid", "Zaghouan", "Medenine", "Kasserine", "Siliana",
                            "Jendouba", "Manouba", "Béja", "Mahdia", "El Kef", "La Manouba",
                            "Nabeul", "Ghardimaou", "Djerba", "Mannouba", "Béja", "Kebili"
                    };
                    String[] venues = {
                            "Community Center", "Hospital Auditorium", "Conference Hall", "University Campus",
                            "Cultural Center", "Sports Complex", "Hotel Conference Room", "Online"
                    };
                    String location = cities[(int) (Math.random() * cities.length)] + " " + venues[(int) (Math.random() * venues.length)];
                    randomWorkshop.setLocation(location);

                    // Use OpenRouter API to generate a short and concise description based on the title
                    String descriptionPrompt = "Generate a short and concise description (2-3 lines maximum) for a health-related workshop titled: " + formattedTitle +" be creative";
                    return retryApiCall(descriptionPrompt, 3)
                            .thenApply(description -> {
                                randomWorkshop.setDescription(description);
                                return randomWorkshop;
                            });
                });
    }


    // Utility method to format the title (capitalize, trim, etc.)
    private String formatTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        String[] words = title.split(" ");
        StringBuilder formattedTitle = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formattedTitle.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return formattedTitle.toString().trim();
    }


    private CompletableFuture<String> retryApiCall(String prompt, int retries) {
        return OpenRouterAPI.sendRequest("deepseek/deepseek-chat:free", prompt)
                .thenCompose(response -> {
                    if (response == null || response.trim().isEmpty() || response.startsWith("Error")) {
                        if (retries > 0) {
                            // Retry the API call
                            return retryApiCall(prompt, retries - 1);
                        } else {
                            // Return a default description if all retries fail
                            return CompletableFuture.completedFuture("Join this workshop to learn valuable health-related skills.");
                        }
                    } else {
                        // Return the valid response
                        return CompletableFuture.completedFuture(response);
                    }
                });
    }



    private void autoFillForm(workshop workshop) {
        // Fill the title field
        titleField.setText(workshop.getTitle());



        // Fill the capacity field
        capacityField.setText(String.valueOf(workshop.getCapacity()));

        // Fill the location field
        locationField.setText(workshop.getLocation());

        // Fill the description field
        descriptionField.setText(workshop.getDescription());
    }

}