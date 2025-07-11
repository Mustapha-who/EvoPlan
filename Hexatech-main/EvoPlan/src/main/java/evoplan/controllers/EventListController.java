package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.services.event.EventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EventListController {

    @FXML
    private GridPane gridPane;

    @FXML
    private ComboBox<String> searchLieuField;

    @FXML
    private Button searchButton;

    private final EventService eventService = new EventService();
    private static final int EVENTS_PER_PAGE = 6; // Nombre d'événements par page
    private int currentPage = 1;
    private List<Event> allEvents; // Liste complète des événements

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label pageLabel;// Instance du service pour récupérer les événements

    @FXML
    public void initialize() {
        // Ajouter l'option "TOUS" et les régions à la ComboBox
        searchLieuField.getItems().addAll(
                "TOUS", "Tunis", "Ariana", "Beja", "BenArous", "Bizerte", "Gabès", "Gafsa", "Jendouba",
                "Kairouan", "Kasserine", "Kébili", "Kef", "Mahdia", "Manouba", "Médenine", "Monastir",
                "Nabeul", "Sfax", "SidiBouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Zaghouan"
        );

        // Sélectionner "TOUS" par défaut
        searchLieuField.setValue("TOUS");

        // Charger tous les événements au démarrage
        loadEvents(null);

        // Ajouter l'action du bouton de recherche
        searchButton.setOnAction(event -> searchByLieu());
        allEvents = eventService.getAllEvents(); // Charger tous les événements
        updatePage(); // Charger la première page

        searchButton.setOnAction(event -> searchByLieu());
        prevButton.setOnAction(event -> changePage(-1));
        nextButton.setOnAction(event -> changePage(1));
    }

    private void searchByLieu() {
        String selectedLieu = searchLieuField.getValue();

        if ("TOUS".equals(selectedLieu)) {
            allEvents = eventService.getAllEvents(); // Récupérer tous les événements
        } else {
            allEvents = eventService.getAllEvents().stream()
                    .filter(event -> selectedLieu.equalsIgnoreCase(event.getLieu().name()))
                    .collect(Collectors.toList());
        }

        currentPage = 1; // Revenir à la première page après un filtre
        updatePage();
    }

    private void loadEvents(String lieuFilter) {
        gridPane.getChildren().clear(); // Nettoyer la grille avant d'afficher les nouveaux événements
        List<Event> events = eventService.getAllEvents(); // Récupérer tous les événements

        // Appliquer le filtre si une région spécifique est sélectionnée
        if (lieuFilter != null && !lieuFilter.isEmpty()) {
            events = events.stream()
                    .filter(event -> lieuFilter.equalsIgnoreCase(event.getLieu().name()))
                    .collect(Collectors.toList());
        }

        int row = 0;
        int column = 0;

        for (Event event : events) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/Events/EventCard.fxml"));
                AnchorPane eventCard = loader.load();

                DisplayEventClientController controller = loader.getController();
                controller.setEventData(event);

                gridPane.add(eventCard, column, row);

                column++;
                if (column == 3) { // Ajuste à 2 colonnes max par ligne
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void updatePage() {
        gridPane.getChildren().clear(); // Nettoyer la grille

        int start = (currentPage - 1) * EVENTS_PER_PAGE;
        int end = Math.min(start + EVENTS_PER_PAGE, allEvents.size());
        List<Event> eventsToShow = allEvents.subList(start, end);

        int row = 0, column = 0;
        for (Event event : eventsToShow) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/Events/EventCard.fxml"));
                AnchorPane eventCard = loader.load();

                DisplayEventClientController controller = loader.getController();
                controller.setEventData(event);

                gridPane.add(eventCard, column, row);
                column++;
                if (column == 3) { // 3 colonnes max par ligne
                    column = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pageLabel.setText("Page " + currentPage);
        prevButton.setDisable(currentPage == 1);
        nextButton.setDisable(end >= allEvents.size());
    }
    private void changePage(int delta) {
        currentPage += delta;
        updatePage();
    }

}
