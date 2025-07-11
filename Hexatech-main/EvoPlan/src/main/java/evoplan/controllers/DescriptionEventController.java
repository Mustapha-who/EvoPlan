package evoplan.controllers;

import evoplan.controllers.EventPlannerHomePage.WorkshopFrontEnd.Workshops;
import evoplan.entities.Partner;
import evoplan.entities.event.Event;
import evoplan.entities.event.TypeStatus;
import evoplan.entities.workshop.workshop;
import evoplan.services.Partner.PartnerService;
import evoplan.services.user.AppSessionManager;
import evoplan.services.workshop.reservationSessionService;
import evoplan.services.workshop.workshopService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DescriptionEventController {

    @FXML
    private ImageView Imageveiwid; // Image view for the event

    @FXML
    private Label Localisation; // Label for event location

    @FXML
    private Label Nomdes; // Label for event name

    @FXML
    private Label date_Debut; // Label for event start date

    @FXML
    private Label descriptionid; // Label for event description

    @FXML
    private HBox partnerContainer;
    @FXML
    private Button getTicketButton;// Container for partner logos

    @FXML
    private VBox workshopContainer; // Container for workshop cards

    private Event currentEvent; // The current event being displayed
    private final PartnerService partnerService = new PartnerService(); // Service for partners
    private final workshopService workshopService = new workshopService(); // Service for workshops
    /**
     * Sets the event details and updates the UI.
     *
     * @param event The event to display.
     */
    public void setEventDetails(Event event) {
        this.currentEvent = event;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        System.out.println("Événement reçu : " + event);

        Nomdes.setText(event.getNom());
        date_Debut.setText(event.getDateDebut().format(formatter));
        descriptionid.setText(event.getDescription());
        Localisation.setText(event.getLieu().name());

        File file = new File(event.getImageEvent());
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            Imageveiwid.setImage(image);
        } else {
            System.out.println("Image introuvable : " + file.getAbsolutePath());
        }

        loadPartners();
        loadWorkshops();

        // 🔥 Désactiver le bouton si l'événement est complet
        if (event.getStatut() == TypeStatus.COMPLET) {
            getTicketButton.setDisable(true);
            getTicketButton.setText("Complet");
        } else {
            getTicketButton.setDisable(false);
        }
    }
    /**
     * Loads partners associated with the current event and displays their logos.
     */
    private void loadPartners() {
        partnerContainer.getChildren().clear(); // Clear existing partner logos
        if (currentEvent == null) return;

        // Fetch partners linked to the current event
        List<Partner> partners = partnerService.getPartnersByEvent(currentEvent.getIdEvent());

        // Display each partner's logo
        for (Partner partner : partners) {
            VBox vbox = new VBox(5);
            vbox.setStyle("-fx-alignment: center;");

            // Display the partner's logo
            ImageView partnerLogo = new ImageView();
            partnerLogo.setFitWidth(100);
            partnerLogo.setFitHeight(100);
            File file = new File(partner.getLogo());
            if (file.exists()) {
                partnerLogo.setImage(new Image(file.toURI().toString()));
            }

            vbox.getChildren().addAll(partnerLogo);
            partnerContainer.getChildren().add(vbox);
        }
    }

    /**
     * Loads workshops associated with the current event and displays them.
     */
    private void loadWorkshops() {
        if (workshopContainer == null) {
            System.out.println("workshopContainer is null. Check FXML file and fx:id.");
            return;
        }
        workshopContainer.getChildren().clear(); // Clear existing workshop cards
        if (currentEvent == null) return;

        // Fetch workshops linked to the current event
        List<workshop> workshops = workshopService.getWorkshopsByEventId(currentEvent.getIdEvent());

        // Display each workshop
        for (workshop w : workshops) {
            // Fetch the instructor's name for the workshop
            String instructorName = workshopService.getInstructorNameById(w.getInstructor());

            // Create a workshop card and add it to the container
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
        VBox card = new VBox(10); // Create a VBox with 10px spacing
        card.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");

        // Workshop title
        Label titleLabel = new Label(w.getTitle());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Workshop dates
        Label dateLabel = new Label("Date: " + w.getDate().toString());
        Label endDateLabel = new Label("End Date: " + w.getEnddate().toString());

        // Instructor name
        Label instructorLabel = new Label("Instructor: " + instructorName);

        // Workshop capacity
        Label capacityLabel = new Label("Capacity: " + w.getCapacity());

        // Workshop location
        Label locationLabel = new Label("Location: " + w.getLocation());

        // Workshop description
        Label descriptionLabel = new Label(w.getDescription());
        descriptionLabel.setWrapText(true); // Enable text wrapping

        // Add all components to the card
        card.getChildren().addAll(titleLabel, dateLabel, endDateLabel, instructorLabel, capacityLabel, locationLabel, descriptionLabel);

        return card;
    }

    /**
     * Handles the "Get Ticket" button click.
     *
     * @param event The action event.
     */
    @FXML
    void GetTicket(ActionEvent event) {
        // Récupérer l'ID du client connecté
        Integer clientId = AppSessionManager.getInstance().getCurrentUserId();
        if (clientId == null) {
            System.out.println("Aucun utilisateur connecté !");
            return;
        }

        // Créer une instance de reservationSessionService
        reservationSessionService service = new reservationSessionService();

        // Vérifier si l'utilisateur est déjà inscrit à l'événement
        boolean isAlreadyRegistered = service.isUserRegisteredForEvent(clientId, currentEvent.getIdEvent());

        if (isAlreadyRegistered) {
            // Afficher un message d'alerte si l'utilisateur est déjà inscrit
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation existante");
            alert.setHeaderText("Vous êtes déjà inscrit à cet événement");
            alert.setContentText("Vous ne pouvez réserver qu'une seule fois pour un même événement.");
            alert.showAndWait();
            return; // Sortir de la méthode si l'utilisateur est déjà inscrit
        }

        try {
            // Charger le formulaire de réservation
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/Events/AddReservation.fxml"));
            Parent root = loader.load();

            // Passer l'événement actuel au contrôleur de réservation
            ReservationClientController controller = loader.getController();
            controller.setEventDetails(currentEvent);

            // Afficher le formulaire de réservation
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mustapha: pop up window button to open all the available workshop in the specific event on top of that it test that the client must have joined the event before checking the workshop
    @FXML
    void JoinWorkshop(ActionEvent event) {
        // Retrieve the logged-in user ID
        Integer clientId = AppSessionManager.getInstance().getCurrentUserId();
        if (clientId == null) {
            System.out.println("No user logged in!");
            return;
        }

        // Create an instance of reservationSessionService
        reservationSessionService service = new reservationSessionService();

        // Check if the user is registered for the event
        boolean isRegistered = service.isUserRegisteredForEvent(clientId, currentEvent.getIdEvent());

        if (!isRegistered) {
            // Show a pop-up message if the user is not registered
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Registration Required");
            alert.setHeaderText("You need to register for the event first!");
            alert.setContentText("Please register for the event to join workshops.");
            alert.showAndWait();
            return; // Exit the method if the user is not registered
        }

        // If the user is registered, proceed to load the workshops
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/WorkshopFrontEnd/Workshops.fxml"));
            Parent root = loader.load();

            Workshops controller = loader.getController();
            controller.setEventDetails(currentEvent.getIdEvent());

            Stage stage = new Stage();
            stage.setTitle("Join Workshop");
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(false);

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
        ((Stage) Nomdes.getScene().getWindow()).close();
    }
}