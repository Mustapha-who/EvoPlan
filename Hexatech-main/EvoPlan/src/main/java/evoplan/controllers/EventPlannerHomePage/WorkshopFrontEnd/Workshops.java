package evoplan.controllers.EventPlannerHomePage.WorkshopFrontEnd;

import evoplan.entities.workshop.workshop;
import evoplan.services.workshop.workshopService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Workshops {

    @FXML
    private VBox workshopCardTemplate; // Workshop card template

    private final workshopService workshopService = new workshopService(); // Service for workshops

    @FXML
    private FlowPane workshopContainer; // Change from HBox to FlowPane

    private int eventId ; // Hardcoded event ID

    /**
     * Default constructor (required for FXML loading).
     */
    public Workshops() {
        // Default constructor
    }

    /**
     * Initializes the controller and loads workshops.
     */
    @FXML
    public void initialize() {
        if (eventId != 0) { // Ensure the event ID is set
            loadWorkshops(eventId); // Load workshops associated with the event
        }
    }

    /**
     * Sets the event details and loads workshops.
     *
     * @param eventId The ID of the event to load workshops for.
     */
    public void setEventDetails(int eventId) {
        this.eventId = eventId; // Store event ID
        loadWorkshops(eventId); // Load related workshops
    }

    /**
     * Loads workshops associated with the current event and displays them.
     *
     * @param eventId The ID of the event to load workshops for.
     */
    private void loadWorkshops(int eventId) {
        if (workshopContainer == null) {
            System.out.println("workshopContainer is null. Check FXML file and fx:id.");
            return;
        }
        workshopContainer.getChildren().clear(); // Clear existing workshop cards

        // Fetch workshops linked to the current event
        List<workshop> workshops = workshopService.getWorkshopsByEventId(eventId);

        // Display each workshop
        for (workshop w : workshops) {
            // Fetch the instructor's name for the workshop
            String instructorName = workshopService.getInstructorNameById(w.getInstructor());

            // Create a workshop card using the template
            VBox workshopCard = createWorkshopCard(w, instructorName);
            workshopContainer.getChildren().add(workshopCard);
        }
    }

    /**
     * Creates a workshop card UI component.
     *
     * @param w             The workshop to display.
     * @param instructorName The name of the instructor for the workshop.
     * @return A VBox representing the workshop card.
     */
    private VBox createWorkshopCard(workshop w, String instructorName) {
        // Create a new VBox for the workshop card
        VBox workshopCard = new VBox();
        workshopCard.getStyleClass().addAll(workshopCardTemplate.getStyleClass());
        workshopCard.setStyle(workshopCardTemplate.getStyle());
        workshopCard.setPrefWidth(220); // Set a preferred width for the card

        // Create new Label instances for each workshop detail
        Label titleLabel = new Label(w.getTitle());
        titleLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        Label dateLabel = new Label("Start Date: " + w.getDate().toString());
        Label endDateLabel = new Label("End Date: " + w.getEnddate().toString());
        Label instructorLabel = new Label("Instructor: " + instructorName);
        Label capacityLabel = new Label("Capacity: " + w.getCapacity());
        Label locationLabel = new Label("Location: " + w.getLocation());
        Label descriptionLabel = new Label("Description: " + w.getDescription());
        descriptionLabel.setWrapText(true); // Enable text wrapping
        descriptionLabel.setMaxWidth(200); // Set a maximum width for the description
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-padding: 5 0 0 0; -fx-font-style: italic;");
        descriptionLabel.setWrapText(true); // Enable text wrapping
        descriptionLabel.setMaxWidth(200); // Set a maximum width for the description
        descriptionLabel.setStyle("-fx-padding: 5 0 0 0;"); // Adjust padding to make it more compact

        // Add all components to the card
        workshopCard.getChildren().addAll(
                titleLabel, dateLabel, endDateLabel, instructorLabel, capacityLabel, locationLabel, descriptionLabel
        );

        // Store the workshop ID in the card (for use in the event handler)
        workshopCard.setUserData(w.getId_workshop());

        // Add a click event handler to the card
        workshopCard.setOnMouseClicked(event -> {
            // Retrieve the workshop ID from the card
            int workshopId = (int) workshopCard.getUserData();

            // Open the ReserveWorkshop pop-up window
            openReserveWorkshopWindow(workshopId);
        });

        return workshopCard;
    }

    private void openReserveWorkshopWindow(int workshopId) {
        try {
            // Load the ReserveWorkshop.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/WorkshopFrontEnd/ReserveWorkshop.fxml"));
            Parent root = loader.load();

            // Pass the workshop ID and details to the ReserveWorkshop controller
            ReserveWorkshop reserveController = loader.getController();

            // Fetch the workshop details using the workshopService
            workshop workshop = workshopService.getWorkshopById(workshopId);
            if (workshop != null) {
                // Pass the workshop details to the ReserveWorkshop controller
                reserveController.setWorkshopId(workshopId);
                reserveController.setWorkshopDetails(workshop.getDescription(), workshop.getLocation());
            } else {
                System.out.println("Workshop not found with ID: " + workshopId);
                return; // Exit if the workshop is not found
            }

            // Create a new stage for the pop-up window
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Reserve Workshop");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the "Back" button click.
     *
     * @param event The action event.
     */
    @FXML
    void RetourId(ActionEvent event) {
        // Close the current window
        ((Stage) workshopContainer.getScene().getWindow()).close();
    }
}