package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.services.event.EventService;
import evoplan.services.event.StatsService;
import evoplan.services.user.AppSessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class DisplayEventClientController {
    @FXML
    private Label Datedebidd;

    @FXML
    private ImageView Imageviewid;

    @FXML
    private Label Nomeventid;

    @FXML
    private Label StatusE;

    @FXML
    private Label prixidE;

    private Event currentEvent;
    private final EventService eventService = new EventService();
    private final StatsService statsService = new StatsService();

    public void setEventData(Event event) {
        this.currentEvent = event;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Nomeventid.setText(event.getNom());
        Datedebidd.setText(event.getDateDebut().format(formatter));
        prixidE.setText(event.getPrix() + " $");
        StatusE.setText(event.getStatut().name());

        if (event.getImageEvent() != null && !event.getImageEvent().isEmpty()) {
            File file = new File(event.getImageEvent());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                Imageviewid.setImage(image);
            }
        }

        // 🔥 On s'assure que l'événement est ajouté UNE SEULE FOIS
        Imageviewid.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) { // 🔥 Évite le double appel
                openEventDescription();
            }
        });
    }

    private void openEventDescription() {
        if (currentEvent == null) return;

        int clientId = AppSessionManager.getInstance().getCurrentUserId(); // 🔥 Récupérer l'ID du client connecté

        System.out.println("🔍 Enregistrement de la visite pour : " + currentEvent.getNom() + " | Client ID : " + clientId);

        // 🔥 Enregistrer la visite avec l'ID du client
        statsService.addVisit(currentEvent.getIdEvent(), clientId);

        eventService.incrementerVisite(currentEvent.getIdEvent());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/Events/DescriptionEvent.fxml"));
            Parent root = loader.load();

            DescriptionEventController controller = loader.getController();
            controller.setEventDetails(currentEvent);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
