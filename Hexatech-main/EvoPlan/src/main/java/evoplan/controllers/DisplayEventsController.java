package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.entities.Partner;
import evoplan.controllers.Partner.AjouterPartnershipController;
import evoplan.services.Partner.PartnershipService;

import evoplan.services.event.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DisplayEventsController {

    @FXML
    private TableView<Event> eventTable;

    @FXML
    private TableColumn<Event, Integer> idColumn;

    @FXML
    private TableColumn<Event, ImageView> imageColumn;

    @FXML
    private TableColumn<Event, String> lieuColumn;

    @FXML
    private TableColumn<Event, String> nomColumn;

    @FXML
    private TableColumn<Event, LocalDateTime> dateDebutColumn;

    @FXML
    private TableColumn<Event, LocalDateTime> dateFinColumn;

    @FXML
    private TableColumn<Event, Double> prixColumn;

    @FXML
    private Button supprimerButton, modifierButton, creerButton1;
    @FXML
    private TextField searchNameField;
    @FXML
    private DatePicker searchDateField;
    @FXML
    private ComboBox<String> searchLieuField;

    private final EventService eventService = new EventService();
    private ObservableList<Event> eventList;
    private ObservableList<Event> allEvents;
    private boolean isPartnerSelectionMode = false;
    private Partner selectedPartner = null;
    private final PartnershipService partnershipService = new PartnershipService(); // Add this lin

    @FXML
    public void initialize() {
        List<Event> events = eventService.getAllEvents();
        eventList = FXCollections.observableArrayList(events);
        allEvents = FXCollections.observableArrayList(events);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        eventTable.setItems(eventList);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("idEvent"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(new Callback<TableColumn<Event, LocalDateTime>, TableCell<Event, LocalDateTime>>() {
            @Override
            public TableCell<Event, LocalDateTime> call(TableColumn<Event, LocalDateTime> param) {
                return new TableCell<Event, LocalDateTime>() {
                    @Override
                    protected void updateItem(LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(item.format(formatter)); // Convertir LocalDateTime en String
                        }
                    }
                };
            }
        });

// Formatter la colonne dateFin
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(new Callback<TableColumn<Event, LocalDateTime>, TableCell<Event, LocalDateTime>>() {
            @Override
            public TableCell<Event, LocalDateTime> call(TableColumn<Event, LocalDateTime> param) {
                return new TableCell<Event, LocalDateTime>() {
                    @Override
                    protected void updateItem(LocalDateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(item.format(formatter)); // Convertir LocalDateTime en String
                        }
                    }
                };
            }
        });
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));

        imageColumn.setCellValueFactory(param -> {
            String imagePath = param.getValue().getImageEvent();
            if (imagePath != null && !imagePath.isEmpty()) {
                ImageView imageView = new ImageView(new Image("file:" + imagePath));
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                return new SimpleObjectProperty<>(imageView);
            }
            return null;
        });
        searchLieuField.getItems().addAll( "Tunis", "Ariana", "Beja", "BenArous", "Bizerte", "Gabès", "Gafsa", "Jendouba"
                , "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba", "Médenine", "Monastir",
                "Nabeul", "Sfax", "SidiBouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Zaghouan");
        supprimerButton.setOnAction(event -> deleteSelectedEvent());
        modifierButton.setOnAction(event -> editSelectedEvent());

        eventTable.setOnMouseClicked(event -> {
            Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                if (isPartnerSelectionMode) {
                    openAddPartnershipForm(selectedEvent);
                } else if (event.getClickCount() == 2) {
                    openReservationWindow(selectedEvent);
                }
            }
        });
    }

    private void openReservationWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/DisplayReservation.fxml"));
            Scene scene = new Scene(loader.load());

            DisplayReservationController controller = loader.getController();
            controller.setEvent(event);

            Stage stage = new Stage();
            stage.setTitle("Réservations pour l'événement : " + event.getNom());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page des réservations.");
        }
    }


    @FXML
    void CreerButton1(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/AddEvent.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Ajouter un événement");
            stage.setScene(scene);

            // Récupérer le contrôleur de AddEvent
            AddEventController controller = loader.getController();
            // Passer une référence de DisplayEventsController pour pouvoir rafraîchir la table
            controller.setDisplayEventsController(this);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page d'ajout d'événement.");
        }
    }

    private void deleteSelectedEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement à supprimer.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'événement : " + selectedEvent.getNom());
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ? Cette action supprimera également tous les partenariats et réservations associés.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                eventService.deleteEvent(selectedEvent);
                eventList.remove(selectedEvent);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'événement a été supprimé avec succès.");
            } catch (RuntimeException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'événement : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void editSelectedEvent() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement à modifier.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/UpdateEvent.fxml"));
            Scene scene = new Scene(loader.load());
            UpdateEventController controller = loader.getController();
            controller.setEventData(selectedEvent, this);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'événement");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la page de modification.");
        }
    }

    public void refreshTable() {
        eventList.setAll(eventService.getAllEvents());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setPartnerSelectionMode(boolean mode, Partner partner) {
        this.isPartnerSelectionMode = mode;
        this.selectedPartner = partner;
    }

    private void openAddPartnershipForm(Event selectedEvent) {
        // Check if a partnership already exists before opening the form
        if (selectedPartner != null && partnershipService.partnershipExists(selectedPartner.getId_partner(), selectedEvent.getIdEvent())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Un partenariat avec cet événement existe déjà !");
            return; // Do not proceed to open the form
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/AjouterPartnership.fxml"));
            Parent root = loader.load();

            AjouterPartnershipController controller = loader.getController();
            controller.initData(selectedPartner, selectedEvent); // Pass the selected event here

            // Get the parent BorderPane and set the new view
            BorderPane parent = (BorderPane) eventTable.getScene().getRoot();
            parent.setCenter(root);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open add partnership form.");
        }
    }
    @FXML
    private void handleSearch() {
        String searchName = searchNameField.getText().toLowerCase();
        LocalDate searchDate = searchDateField.getValue();
        String searchLieu = searchLieuField.getValue();

        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

        for (Event event : allEvents) { // allEvents est la liste complète des événements
            boolean matchName = searchName.isEmpty() || event.getNom().toLowerCase().contains(searchName);
            boolean matchDate = searchDate == null ||  event.getDateDebut().toLocalDate().equals(searchDate);
            boolean matchLieu = searchLieu == null || event.getLieu().toString().equals(searchLieu);


            if (matchName && matchDate && matchLieu) {
                filteredEvents.add(event);
            }
        }

        eventTable.setItems(filteredEvents);
    }
    @FXML
    private void handleShowStats() {
        Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement pour voir les statistiques.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/EventStatistics.fxml"));
            Parent root = loader.load();

            StatsController controller = loader.getController();
            controller.setEventId(selectedEvent.getIdEvent());

            Stage stage = new Stage();
            stage.setTitle("Statistiques de l'événement : " + selectedEvent.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre des statistiques.");
        }
    }

}