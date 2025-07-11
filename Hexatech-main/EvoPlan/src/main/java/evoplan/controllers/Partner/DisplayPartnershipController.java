package evoplan.controllers.Partner;

import evoplan.controllers.DisplayEventsController;
import evoplan.entities.Partner;
import evoplan.entities.Partnership;
import evoplan.services.Partner.GoogleCalendar;
import evoplan.services.Partner.PartnershipReminderScheduler;
import evoplan.services.Partner.PartnershipService;
import javafx.collections.FXCollections;
import evoplan.controllers.EventPlannerHomePage.EventPlannerHomePage;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import evoplan.services.Partner.EmailSender;
import java.time.LocalDate;
import java.util.List;
import java.io.IOException;
import java.util.Comparator;

public class DisplayPartnershipController {

    @FXML
    private TableView<Partnership> partnershipsTable;
    @FXML
    private TableColumn<Partnership, Integer> idPartnershipColumn;
    @FXML
    private TableColumn<Partnership, Integer> idPartnerColumn;
    @FXML
    private TableColumn<Partnership, Integer> idEventColumn;
    @FXML
    private TableColumn<Partnership, String> startDateColumn;
    @FXML
    private TableColumn<Partnership, String> endDateColumn;
    @FXML
    private TableColumn<Partnership, String> termsColumn;
    @FXML
    private TableColumn<Partnership, Void> actionsColumn;
    @FXML
    private TextField searchField;

    @FXML
    private Button loadDashboard;
    @FXML
    private Button loadEvents;
    @FXML
    private Button loadWorkshops;
    @FXML
    private Button loadPartners;
    @FXML
    private Button loadSettings;


    private PartnershipService partnershipService;
    private ObservableList<Partnership> partnerships;
    private Partner selectedPartner;
    private PartnershipReminderScheduler reminderScheduler;



    public DisplayPartnershipController() {
        partnershipService = new PartnershipService();
        partnerships = FXCollections.observableArrayList();

    }

    @FXML
    public void initialize() {

        System.out.println("Initializing DisplayPartnershipController..."); // Debug statement

        // Set up the columns in the table
        idPartnershipColumn.setCellValueFactory(new PropertyValueFactory<>("id_partnership"));
        idPartnerColumn.setCellValueFactory(new PropertyValueFactory<>("id_partner"));
        idEventColumn.setCellValueFactory(new PropertyValueFactory<>("id_event"));

        // Set up date columns with comparators for sorting
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        startDateColumn.setComparator(Comparator.comparing(LocalDate::parse)); // Assuming date_debut is in a parseable format

        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        endDateColumn.setComparator(Comparator.comparing(LocalDate::parse)); // Assuming date_fin is in a parseable format

        termsColumn.setCellValueFactory(new PropertyValueFactory<>("terms"));

        // Load partnerships from the service
        partnerships.setAll(partnershipService.getAll());
        System.out.println("Loaded partnerships: " + partnerships.size()); // Debug statement

        // Ensure partnerships are loaded before setting up search functionality
        if (partnerships.isEmpty()) {
            System.out.println("No partnerships found."); // Debug statement
        } else {
            System.out.println("Partnerships loaded successfully."); // Debug statement
        }

        setupSearchFunctionality();

        // Setup actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modify");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, modifyBtn, deleteBtn);

            {
                modifyBtn.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

                modifyBtn.setOnAction(event -> {
                    Partnership partnership = getTableView().getItems().get(getIndex());
                    openModifyPartnership(partnership);
                });

                deleteBtn.setOnAction(event -> {
                    Partnership partnership = getTableView().getItems().get(getIndex());
                    deletePartnership(partnership);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
        try {
            initializeReminderScheduler();
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize reminder scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchFunctionality() {
        // Create a filtered list
        FilteredList<Partnership> filteredData = new FilteredList<>(partnerships, p -> true);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Search input changed: " + newValue); // Debug statement
            filteredData.setPredicate(partnership -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all partnerships if search field is empty
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Check if any of the fields match the search input
                return String.valueOf(partnership.getId_partnership()).toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(partnership.getId_partner()).toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(partnership.getId_event()).toLowerCase().contains(lowerCaseFilter) ||
                        partnership.getTerms().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Combine filtered data with sorted list
        SortedList<Partnership> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(partnershipsTable.comparatorProperty());
        partnershipsTable.setItems(sortedData);
    }

    public void refreshTable() {
        partnerships.setAll(partnershipService.getAll());
        partnershipsTable.setItems(partnerships);
        System.out.println("Partnerships refreshed: " + partnerships.size());
    }

    private void deletePartnership(Partnership partnership) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Partnership");
        alert.setHeaderText("Delete Partnership");
        alert.setContentText("Are you sure you want to delete this partnership?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                partnershipService.supprimer(partnership);
                refreshTable();
            }
        });
    }




    private void openModifyPartnership(Partnership partnership) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/ModifyPartnership.fxml"));
            Parent root = loader.load();

            ModifyPartnershipController controller = loader.getController();
            controller.setPartnership(partnership);
            controller.setPartnershipService(partnershipService);
            controller.setDisplayController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            stage.setOnHidden(event -> {
                refreshTable();
                setupSearchFunctionality();
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open modify window");
        }
    }

    @FXML
    private void switchToAddPartnership() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/DisplayEvents.fxml"));
            Parent root = loader.load();

            DisplayEventsController controller = loader.getController();
            controller.setPartnerSelectionMode(true, null);

            BorderPane parent = (BorderPane) partnershipsTable.getScene().getRoot();
            parent.setCenter(root);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger la page des événements !");
            e.printStackTrace();
        }
    }
    @FXML
    private void navigateBack() {
        returnToDisplayPartner();
    }

    @FXML
    private void switchToDisplayContract() {
        returnToDisplayContract();
    }

    private void returnToDisplayPartner() {
        try {
            // Get the current stage
            Stage stage = (Stage) partnershipsTable.getScene().getWindow();

            // Load the main home page again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the main controller
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Display Partner" view inside the main page
            homePageController.loadDisplayPartner();

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Error", "Cannot load Partnership Display!");
            e.printStackTrace();
        }
    }



    private void returnToDisplayContract() {
        try {
            // Get the current stage
            Stage stage = (Stage) partnershipsTable.getScene().getWindow();

            // Load the main home page again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the main controller
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Display Contract" view inside the main page
            homePageController.loaddisplayContaract();

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Error", "Cannot load Contract Display!");
            e.printStackTrace();
        }
    }
    private void initializeReminderScheduler() {
        new Thread(() -> {
            try {
                GoogleCalendar googleCalendarService = new GoogleCalendar();
                EmailSender emailSender = new EmailSender(
                        "yacineamrouche2512@gmail.com", // Remplacez par votre email
                        "ujkd ylry kcza dvqj"          // Remplacez par votre mot de passe d'application
                );
                PartnershipService partnershipService = new PartnershipService();

                // Vérifier si le scheduler est déjà en cours d'exécution
                PartnershipReminderScheduler reminderScheduler = PartnershipReminderScheduler.getInstance(
                        partnershipService, emailSender, googleCalendarService
                );

                if (!reminderScheduler.isRunning()) { // Vérifier s'il tourne déjà
                    reminderScheduler.start();
                    System.out.println("✅ PartnershipReminderScheduler démarré.");
                } else {
                    System.out.println("⚠️ PartnershipReminderScheduler tourne déjà, pas besoin de le redémarrer.");
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'initialisation du scheduler : " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
