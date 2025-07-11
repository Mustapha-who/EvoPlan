package evoplan.controllers.EventPlannerHomePage.WorkshopFrontEnd;

import evoplan.controllers.EventPlannerHomePage.OpenRouterAPI;
import evoplan.controllers.EventPlannerHomePage.TranslationUtil;
import evoplan.entities.workshop.session;
import evoplan.entities.workshop.workshop;
import evoplan.services.user.AppSessionManager;
import evoplan.services.workshop.reservationSessionService;
import evoplan.services.workshop.sessionService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReserveWorkshop {

    @FXML
    private Label workshopIdLabel; // Label to display the workshop ID

    @FXML
    private Button generateButton;

    @FXML
    private ProgressIndicator loadingSpinner; // ProgressIndicator for loading spinner

    @FXML
    private TextField chatInputField; // Chatbot input field

    @FXML
    private TextArea chatResponseArea; // Chatbot response area

    @FXML
    private TextField nameInput; // TextField for name (grayed out)

    @FXML
    private TextField emailInput; // TextField for email (grayed out)

    @FXML
    private ComboBox<String> translationLanguageComboBox; // ComboBox for selecting translation language

    @FXML
    private Button translateButton; // Button to trigger translation

    @FXML
    private TextArea translatedDescriptionArea; // TextArea to display translated content

    // Map to store language names and their codes
    private final Map<String, String> languageMap = new HashMap<>();

    @FXML
    private VBox sessionContainer; // Container for session checkboxes

    private int workshopId; // Store the workshop ID
    private final sessionService sessionService = new sessionService(); // Service for sessions
    private final reservationSessionService reservationSessionService = new reservationSessionService(); // Service for reservations
    private String workshopDescription;
    private String workshopLocation;


    @FXML
    public void initialize() {
        // Populate the language map with common languages
        populateLanguageMap();

        // Add languages to the ComboBox
        translationLanguageComboBox.getItems().addAll(languageMap.keySet());

        // Set default selection
        translationLanguageComboBox.setValue("English");
    }

    //All the languages we can use to translate
    private void populateLanguageMap() {
        // Add common languages and their codes
        languageMap.put("English", "en");
        languageMap.put("Spanish", "es");
        languageMap.put("French", "fr");
        languageMap.put("German", "de");
        languageMap.put("Italian", "it");
        languageMap.put("Portuguese", "pt");
        languageMap.put("Russian", "ru");
        languageMap.put("Chinese (Simplified)", "zh");
        languageMap.put("Japanese", "ja");
        languageMap.put("Korean", "ko");
        languageMap.put("Arabic", "ar");
        languageMap.put("Hindi", "hi");
        languageMap.put("Dutch", "nl");
        // Add more languages as needed
    }


    /**
     * Sets the workshop ID and updates the UI.
     *
     * @param workshopId The ID of the workshop to reserve.
     */
    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;

        // Load and display sessions for the workshop
        loadSessions(workshopId);

        // Fetch and display the logged-in user's data
        loadUserData();
    }

    /**
     * Loads and displays the logged-in user's data.
     */
    private void loadUserData() {
        // Retrieve the logged-in user ID
        Integer clientId = AppSessionManager.getInstance().getCurrentUserId();
        if (clientId == null) {
            System.out.println("No user logged in!");
            return;
        }

        // Fetch the user's name and email from the database
        String[] userData = reservationSessionService.getClientNameAndEmailById(clientId);
        if (userData != null) {
            nameInput.setText(userData[0]); // Set the name
            emailInput.setText(userData[1]); // Set the email
        } else {
            System.out.println("Failed to fetch user data.");
        }
    }

    /**
     * Loads and displays sessions for the given workshop ID.
     *
     * @param workshopId The ID of the workshop.
     */
    private void loadSessions(int workshopId) {
        // Fetch sessions linked to the current workshop
        List<session> sessions = sessionService.getSessionsByWorkshopId(workshopId);

        // Display each session as a checkbox with details
        for (session session : sessions) {
            // Create a checkbox for the session
            CheckBox sessionCheckbox = new CheckBox();
            sessionCheckbox.setUserData(session); // Store the session object in the checkbox
            sessionCheckbox.getStyleClass().add("check-box");

            // Create a label to display session details in a compact format
            String sessionDetails = String.format(
                    "Date: %s | Start: %s | End: %s | Participants: %d/%d",
                    session.getDate(),
                    session.getDateheuredeb(),
                    session.getDateheurefin(),
                    session.getParticipant_count(),
                    session.getCapacity()
            );
            Label sessionLabel = new Label(sessionDetails);
            sessionLabel.getStyleClass().add("session-details-label");
            sessionLabel.setWrapText(true); // Ensure text wraps within the label

            // Use an HBox to display the checkbox and label side by side
            HBox sessionRow = new HBox(10, sessionCheckbox, sessionLabel);
            sessionRow.setPadding(new Insets(5, 0, 5, 0));
            HBox.setHgrow(sessionLabel, Priority.ALWAYS); // Allow the label to expand

            // Add the HBox to the session container
            sessionContainer.getChildren().add(sessionRow);
        }
    }
    /**
     * Handles the reservation form submission.
     */
    @FXML
    private void handleReservation() {
        System.out.println("Reserve button clicked!"); // Debug statement

        // Retrieve the logged-in user ID
        Integer clientId = AppSessionManager.getInstance().getCurrentUserId();
        if (clientId == null) {
            System.out.println("No user logged in!");
            return;
        }

        // Collect selected sessions
        List<session> selectedSessions = new ArrayList<>();
        for (var node : sessionContainer.getChildren()) {
            if (node instanceof HBox) { // Check if the node is an HBox
                HBox sessionRow = (HBox) node;
                CheckBox sessionCheckbox = (CheckBox) sessionRow.getChildren().get(0); // Get the CheckBox from the HBox

                if (sessionCheckbox.isSelected()) {
                    session session = (session) sessionCheckbox.getUserData();
                    if (session != null) {
                        selectedSessions.add(session);
                    }
                }
            }
        }

        // Check if no sessions are selected
        if (selectedSessions.isEmpty()) {
            showAlert("No Sessions Selected", "You must select at least one session to reserve.");
            return; // Stop further processing
        }

        // Track if any reservations were successful
        boolean anyReservationSuccessful = false;

        // Track if any errors occurred
        boolean anyErrorsOccurred = false;

        // Process the selected sessions
        for (session session : selectedSessions) {
            int sessionId = session.getId_session();

            // Check if the client is already registered for the session
            if (reservationSessionService.isClientRegisteredForSession(sessionId, clientId)) {
                showAlert("Already Registered", "You have already joined the session on " + session.getDate() + ".");
                anyErrorsOccurred = true;
                continue; // Skip this session and move to the next one
            }

            // Check if the session is full
            if (reservationSessionService.isSessionFull(sessionId)) {
                showAlert("Session Full", "The session on " + session.getDate() + " is full. You cannot join.");
                anyErrorsOccurred = true;
                continue; // Skip this session and move to the next one
            }

            // Insert the reservation using the logged-in user's ID
            reservationSessionService.addReservation(sessionId, clientId);

            // Mark that at least one reservation was successful
            anyReservationSuccessful = true;

            System.out.println("Reservation added for session: " + sessionId);
        }

        // Show success message if at least one reservation was successful
        if (anyReservationSuccessful) {
            showAlert("Success", "You have successfully joined the selected sessions!");
        }

        // Reload sessions to update the UI
        sessionContainer.getChildren().clear();
        loadSessions(workshopId);

        // Close the pop-up window only if no errors occurred
        if (!anyErrorsOccurred) {
            closeWindow();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Closes the pop-up window.
     */
    private void closeWindow() {
        Stage stage = (Stage) workshopIdLabel.getScene().getWindow();
        stage.close();
    }


/**
 * Sets the workshop details (description and location) for the chatbot.
 *
 * @param description The workshop description.
 * @param location    The workshop location.
 */
    public void setWorkshopDetails(String description, String location) {
        // Store the workshop details
        this.workshopDescription = description;
        this.workshopLocation = location;
    }

    @FXML
    private void handleGenerateButtonClick() {
        // Get the user's input from the chatInputField
        final String userInput = chatInputField.getText(); // Ensure it is effectively final

        // Check if the user input is empty
        if (userInput.isEmpty()) {
            chatResponseArea.setText("Please enter a question.");
            return;
        }

        // Disable the button and show the loading spinner
        generateButton.setDisable(true);
        loadingSpinner.setVisible(true);

        // Step 1: Pre-check if the input is workshop-related
        String preCheckPrompt = "Does this user prompt ask about the details, benefits, schedule, or location of a workshop? Respond with 'yes' or 'no' only. User prompt: " + userInput;

        // Call the OpenRouter API for the pre-check
        CompletableFuture<String> preCheckResponse = OpenRouterAPI.sendRequest("deepseek/deepseek-chat:free", preCheckPrompt);

        // Handle the pre-check response
        preCheckResponse.thenAccept(response -> {
            javafx.application.Platform.runLater(() -> {
                final String normalizedResponse = response.trim().toLowerCase(); // Ensure it's final

                if (normalizedResponse.equals("yes")) {
                    // Step 2: Generate the workshop explanation
                    String explanationPrompt = "Explain the following workshop and make sure it 'doesn't surpass the 400 chars' mark please. Focus on its benefits and what the user will gain from attending. "
                            + "Workshop Description: " + workshopDescription + " "
                            + "Location: " + workshopLocation + " "
                            + "User Question: " + userInput;

                    CompletableFuture<String> apiResponse = OpenRouterAPI.sendRequest("deepseek/deepseek-chat:free", explanationPrompt);

                    // Handle the API response
                    apiResponse.thenAccept(explanation -> {
                        javafx.application.Platform.runLater(() -> {
                            chatResponseArea.setText(explanation);
                            loadingSpinner.setVisible(false);
                            generateButton.setDisable(false);
                        });
                    }).exceptionally(ex -> {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            chatResponseArea.setText("Error: Unable to generate response.");
                            loadingSpinner.setVisible(false);
                            generateButton.setDisable(false);
                        });
                        return null;
                    });

                } else if (normalizedResponse.equals("no")) {
                    chatResponseArea.setText("Sorry, I only answer workshop-related questions. How can I assist you with this workshop?");
                    loadingSpinner.setVisible(false);
                    generateButton.setDisable(false);
                } else {
                    chatResponseArea.setText("Error: Invalid response from the chatbot.");
                    loadingSpinner.setVisible(false);
                    generateButton.setDisable(false);
                }
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                chatResponseArea.setText("Error: Unable to perform the pre-check.");
                loadingSpinner.setVisible(false);
                generateButton.setDisable(false);
            });
            return null;
        });
    }




    /**
     * Handles the translate button click event.
     * Translates the content from the chatbot response area based on selected language.
     */
    @FXML
    private void handleTranslateButtonClick() {
        // Get the text from the chatbot response area
        String textToTranslate = chatResponseArea.getText();

        // Check if there's text to translate
        if (textToTranslate == null || textToTranslate.trim().isEmpty()) {
            translatedDescriptionArea.setText("No content to translate. Please generate chatbot response first.");
            return;
        }

        // Get the selected target language
        String targetLanguageName = translationLanguageComboBox.getValue();
        if (targetLanguageName == null) {
            translatedDescriptionArea.setText("Please select a target language.");
            return;
        }

        String targetLanguageCode = languageMap.get(targetLanguageName);

        try {
            // Show "Translating..." message
            translatedDescriptionArea.setText("Translating...");

            // Disable the translate button during translation
            translateButton.setDisable(true);

            // Run translation in background thread
            new Thread(() -> {
                try {
                    // Perform the translation (using English as source language)
                    String translatedText = TranslationUtil.translate(textToTranslate, "en", targetLanguageCode);

                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        translatedDescriptionArea.setText(translatedText);
                        translateButton.setDisable(false);
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                    // Update UI with error message
                    javafx.application.Platform.runLater(() -> {
                        translatedDescriptionArea.setText("Translation error: " + e.getMessage());
                        translateButton.setDisable(false);
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            translatedDescriptionArea.setText("Error: " + e.getMessage());
            translateButton.setDisable(false);
        }
    }
}


