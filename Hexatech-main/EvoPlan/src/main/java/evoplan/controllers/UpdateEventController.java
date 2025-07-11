package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.entities.event.TypeStatus;
import evoplan.entities.event.Regions; // Import de l'énumération Regions
import evoplan.services.event.EventService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class UpdateEventController {

    @FXML
    private Spinner<Integer> Capaciteid;

    @FXML
    private DatePicker DateDebutid;

    @FXML
    private DatePicker DatefinId;


    @FXML
    private TextArea Descriptionid;

    @FXML
    private ComboBox<String> Lieuid; // Modification ici

    @FXML
    private TextField Nomid;

    @FXML
    private Spinner<Double> Prixid;

    @FXML
    private ComboBox<String> Statusid;

    @FXML
    private ComboBox<String> heureDebid;

    @FXML
    private ComboBox<String> heureFinid;
    @FXML
    private ImageView eventImageView;

    private File selectedImageFile;

    private Event event;
    private final EventService eventService = new EventService();
    private DisplayEventsController parentController;

    public void setEventData(Event event, DisplayEventsController parentController) {
        this.event = event;
        this.parentController = parentController;
        if (event.getImageEvent() != null && !event.getImageEvent().isEmpty()) {
            eventImageView.setImage(new Image(new File(event.getImageEvent()).toURI().toString()));
        }
        String heureDebut = String.format("%02d:%02d", event.getDateDebut().getHour(), event.getDateDebut().getMinute());
        String heureFin = String.format("%02d:%02d", event.getDateFin().getHour(), event.getDateFin().getMinute());

        Nomid.setText(event.getNom());
        Descriptionid.setText(event.getDescription());
        DateDebutid.setValue(event.getDateDebut().toLocalDate());
        DatefinId.setValue(event.getDateFin().toLocalDate());

        // Initialisation des heures
        if (heureDebid != null) {
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m += 30) {
                    String time = String.format("%02d:%02d", h, m);
                    heureDebid.getItems().add(time);
                    heureFinid.getItems().add(time);
                }
            }
        }

        heureDebid.setValue(heureDebut);
        heureFinid.setValue(heureFin);

        // Initialisation de la liste des régions
        Lieuid.getItems().addAll("Tunis", "Ariana", "Beja", "BenArous", "Bizerte", "Gabès", "Gafsa", "Jendouba"
                , "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba", "Médenine", "Monastir",
                "Nabeul", "Sfax", "SidiBouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Zaghouan");

        // Sélection de la région actuelle de l'événement
        try {
            Lieuid.setValue(event.getLieu().name());
        } catch (IllegalArgumentException e) {
            Lieuid.setValue(null);
        }

        // Configuration des spinners
        if (Capaciteid.getValueFactory() == null) {
            Capaciteid.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 1));
        }
        Capaciteid.getValueFactory().setValue(event.getCapacite());

        if (Prixid.getValueFactory() == null) {
            Prixid.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000, 1));
        }
        Prixid.getValueFactory().setValue(event.getPrix());
        Statusid.getItems().addAll("DISPONIBLE","COMPLET");

        // Sélection de la région actuelle de l'événement
        try {
            Statusid.setValue(event.getStatut().name());
        } catch (IllegalArgumentException e) {
            Statusid.setValue(null);
        }
        Statusid.setValue(event.getStatut().name());
        DateDebutid.valueProperty().addListener((obs, oldDate, newDate) -> validateDateDebut());
    }
    @FXML
    void validateDateDebut() {
        if (DateDebutid.getValue() != null && DateDebutid.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert("Erreur", "La date de début ne peut pas être antérieure à aujourd'hui !");
            DateDebutid.setValue(null);
        }
    }
    @FXML
    void UpdateE(ActionEvent event) {
        if (this.event == null) return;

        // Vérification des champs obligatoires
        if (Nomid.getText().isEmpty() || Descriptionid.getText().isEmpty() || DateDebutid.getValue() == null ||
                DatefinId.getValue() == null || heureDebid.getValue() == null ||
                heureFinid.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires !");
            return;
        }

        this.event.setNom(Nomid.getText());
        this.event.setDescription(Descriptionid.getText());
        this.event.setDateDebut(LocalDateTime.of(DateDebutid.getValue(), LocalTime.parse(heureDebid.getValue())));
        this.event.setDateFin(LocalDateTime.of(DatefinId.getValue(), LocalTime.parse(heureFinid.getValue())));
        this.event.setLieu(Regions.valueOf(Lieuid.getValue()));
        this.event.setCapacite(Capaciteid.getValue());
        this.event.setPrix(Prixid.getValue());
        this.event.setStatut(TypeStatus.valueOf(Statusid.getValue()));

        // Mise à jour de l’image
        if (selectedImageFile != null) {
            this.event.setImageEvent(selectedImageFile.getAbsolutePath());
        }
        if (DateDebutid.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert("Erreur", "La date de début ne peut pas être antérieure à aujourd'hui !");
            return;
        }

        eventService.updateEvent(this.event);
        parentController.refreshTable();

        // Fermer la fenêtre
        ((Stage) Nomid.getScene().getWindow()).close();
    }

    @FXML
    void Uploadid(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(((Stage) Nomid.getScene().getWindow()));

        if (file != null) {
            selectedImageFile = file;
            eventImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    void Annuler(ActionEvent event) {
        ((Stage) Nomid.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
