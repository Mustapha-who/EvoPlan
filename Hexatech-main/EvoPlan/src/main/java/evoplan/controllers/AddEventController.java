package evoplan.controllers;

import evoplan.entities.event.TypeStatus;
import evoplan.entities.event.Regions; // Import de l'énumération Regions
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import evoplan.entities.event.Event;
import evoplan.services.event.EventService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AddEventController {
    @FXML
    private DatePicker DateDebutId;
    @FXML
    private DatePicker DateFinId;
    @FXML
    private Spinner<Integer> capaciteid;
    @FXML
    private TextArea descriptionid;
    @FXML
    private ComboBox<String> heuredebid;
    @FXML
    private ComboBox<String> heurefinid;
    @FXML
    private ComboBox<String> lieuid; // Modification ici
    @FXML
    private TextField nomid;
    @FXML
    private Spinner<Double> prixid;
    @FXML
    private ComboBox<String> statusid;

    private String imageEvent = "";
    private DisplayEventsController displayEventsController; // Référence au contrôleur parent

    public void setDisplayEventsController(DisplayEventsController controller) {
        this.displayEventsController = controller;
    }

    @FXML
    void initialize() {
        // Initialisation des Spinners
        SpinnerValueFactory<Integer> capaciteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50);
        capaciteid.setValueFactory(capaciteFactory);

        SpinnerValueFactory<Double> prixFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 10000.0, 0.0);
        prixid.setValueFactory(prixFactory);


        // Initialisation des heures
        if (heuredebid != null) {
            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m += 30) {
                    String time = String.format("%02d:%02d", h, m);
                    heuredebid.getItems().add(time);
                    heurefinid.getItems().add(time);
                }
            }
        }

        // Initialisation des statuts
        statusid.getItems().addAll("DISPONIBLE","COMPLET");

        // Initialisation de la liste des régions
        lieuid.getItems().addAll( "Tunis", "Ariana", "Beja", "BenArous", "Bizerte", "Gabès", "Gafsa", "Jendouba"
                , "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba", "Médenine", "Monastir",
                "Nabeul", "Sfax", "SidiBouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Zaghouan");
        DateDebutId.valueProperty().addListener((obs, oldDate, newDate) -> validateDateDebut());

    }
    @FXML
    void validateDateDebut() {
        if (DateDebutId.getValue() != null && DateDebutId.getValue().isBefore(java.time.LocalDate.now())) {
            showAlert("Erreur", "La date de début ne peut pas être antérieure à aujourd'hui !");
            DateDebutId.setValue(null);
        }
    }
    @FXML
    void AjouterE(ActionEvent event) {
        try {
            // Vérification des champs obligatoires
            if (nomid.getText().isEmpty() || descriptionid.getText().isEmpty() ||
                    lieuid.getValue() == null || DateDebutId.getValue() == null ||
                    DateFinId.getValue() == null || heuredebid.getValue() == null ||
                    heurefinid.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs obligatoires !");
                return;
            }

            // Récupération des valeurs
            String nom = nomid.getText();
            String description = descriptionid.getText();
            Regions lieu = Regions.valueOf(lieuid.getValue());

            LocalDateTime dateDebut = LocalDateTime.of(DateDebutId.getValue(), LocalTime.parse(heuredebid.getValue()));
            LocalDateTime dateFin = LocalDateTime.of(DateFinId.getValue(), LocalTime.parse(heurefinid.getValue()));

            // ✅ Formatage propre de la date avec espace au lieu de "T"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDateDebut = dateDebut.format(formatter);
            String formattedDateFin = dateFin.format(formatter);

            System.out.println("Date de début formatée : " + formattedDateDebut);
            System.out.println("Date de fin formatée : " + formattedDateFin);

            int capacite = capaciteid.getValue();
            double prix = prixid.getValue();

            // Validations
            if (DateDebutId.getValue().isBefore(java.time.LocalDate.now())) {
                showAlert("Erreur", "La date de début ne peut pas être antérieure à aujourd'hui !");
                return;
            }
            if (dateDebut.isAfter(dateFin)) {
                showAlert("Erreur", "La date de début doit être antérieure à la date de fin.");
                return;
            }

            if (capacite <= 0) {
                showAlert("Erreur", "La capacité doit être un nombre positif.");
                return;
            }

            if (prix < 0) {
                showAlert("Erreur", "Le prix doit être un nombre positif.");
                return;
            }

            TypeStatus statut = TypeStatus.valueOf(statusid.getValue());

            // Création et ajout de l'événement
            Event e = new Event(nom, description, dateDebut, dateFin, lieu, capacite, prix, statut, imageEvent);
            EventService eventService = new EventService();
            eventService.addEvent(e);

            showAlert("Succès", "Événement ajouté avec succès !");

            // Rafraîchir la table des événements
            if (displayEventsController != null) {
                displayEventsController.refreshTable();
            }

            // Fermer la fenêtre
            ((Stage) nomid.getScene().getWindow()).close();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Erreur", "Impossible d'ajouter l'événement : " + ex.getMessage());
        }
    }

    @FXML
    void Uploadid(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            try {
                File destDir = new File("uploads");
                if (!destDir.exists()) destDir.mkdir();

                File destFile = new File(destDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imageEvent = destFile.getAbsolutePath();
                showAlert("Succès", "Image téléchargée avec succès !");
            } catch (Exception e) {
                showAlert("Erreur", "Impossible d'enregistrer l'image : " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
