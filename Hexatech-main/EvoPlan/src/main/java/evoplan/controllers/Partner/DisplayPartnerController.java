package evoplan.controllers.Partner;

import evoplan.entities.Partner;
import evoplan.services.Partner.PartnerService;
import evoplan.controllers.DisplayEventsController;
import javafx.collections.FXCollections;
import evoplan.controllers.EventPlannerHomePage.EventPlannerHomePage;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.BorderPane;
import evoplan.entities.PartnerType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;


public class DisplayPartnerController {

    @FXML
    FlowPane cardsContainer;
    @FXML
    private TextField searchField; // Search bar
    @FXML
    private ComboBox<String> filterComboBox; // Filter options
    @FXML
    private ComboBox<String> sortComboBox; // Sort options

    private PartnerService partnerService;
    private ObservableList<Partner> partners;
    private ObservableList<Partner> filteredPartners; // For search and filter
    private Partner selectedPartner;


    public DisplayPartnerController() {
        partnerService = new PartnerService();
        partners = FXCollections.observableArrayList();
        filteredPartners = FXCollections.observableArrayList();
        // Initialize filtered list
    }

    @FXML
    public void initialize() {
        partners.addAll(fetchPartners());
        filteredPartners.addAll(partners); // Initially, all partners are displayed
        updatePartnerCards(filteredPartners); // Display all partners initially
        filterComboBox.getItems().addAll("All", "speaker","sponsor"); // Add actual filter types
        sortComboBox.getItems().addAll("unsorted","Email", "PartnerType"); // Add actual sort criteria
        partners.clear(); // Clear existing partners
        partners.addAll(fetchPartners()); // Fetch and add new partners
        filteredPartners.setAll(partners); // Update filtered list
        updatePartnerCards(filteredPartners);
    }


    private void updatePartnerCards(ObservableList<Partner> partnerList) {
        cardsContainer.getChildren().clear(); // Clear existing cards
        for (Partner partner : partnerList) {
            VBox card = createPartnerCard(partner);
            cardsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        filteredPartners.setAll(partners.filtered(partner ->
                (partner.getType_partner() != null && partner.getType_partner().name().toLowerCase().contains(searchText)) ||
                        (partner.getEmail() != null && partner.getEmail().toLowerCase().contains(searchText))

        ));
        updatePartnerCards(filteredPartners);
    }

    @FXML
    private void handleFilter() {
        String selectedFilter = filterComboBox.getSelectionModel().getSelectedItem();
        System.out.println("Filter selected: " + selectedFilter); // Debugging statement

        List<Partner> filteredPartners;

        if ("All".equals(selectedFilter)) {
            filteredPartners = partners; // Show all partners
        } else {
            // Filter based on the selected PartnerType
            filteredPartners = partners.stream()
                    .filter(partner -> partner.getType_partner() != null && partner.getType_partner().name().equalsIgnoreCase(selectedFilter))
                    .collect(Collectors.toList());
        }

        // Debugging statement to check the filtered partners
        System.out.println("Filtered Partners Count: " + filteredPartners.size());

        refreshPartnerCards(filteredPartners);
    }

    @FXML
    private void handleSort() {
        String selectedSort = sortComboBox.getSelectionModel().getSelectedItem();
        List<Partner> sortedPartners;

        switch (selectedSort) {
            case "unsorted":
                sortedPartners = partners; // No sorting, keep original order
                break;
            case "Email":
                sortedPartners = partners.stream()
                        .sorted(Comparator.comparing(Partner::getEmail)) // Sort by email
                        .collect(Collectors.toList());
                break;
            case "PartnerType":
                sortedPartners = partners.stream()
                        .sorted(Comparator.comparing(Partner::getType_partner, PartnerType.getComparator())) // Sort by PartnerType
                        .collect(Collectors.toList());
                break;
            default:
                sortedPartners = partners; // Default case
                break;
        }

        refreshPartnerCards(sortedPartners);
    }

    private VBox createPartnerCard(Partner partner) {
        // Main card container
        VBox card = new VBox(15);
        card.setPrefWidth(280);
        card.setPrefHeight(350);
        card.setStyle("-fx-background-color: white;" +
                "-fx-padding: 15;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);" +
                "-fx-border-color: #f0f0f0;" +
                "-fx-border-radius: 10;");
        card.setAlignment(Pos.TOP_CENTER);

        // Logo container with background
        VBox logoContainer = new VBox();
        logoContainer.setPrefHeight(150);
        logoContainer.setPrefWidth(260);
        logoContainer.setStyle("-fx-background-color: #f8f9fa;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 0;" +
                "-fx-alignment: center;");
        logoContainer.setAlignment(Pos.CENTER);

        // Logo image view with improved fitting
        ImageView logoImageView = new ImageView();
        logoImageView.setFitHeight(150);
        logoImageView.setFitWidth(260);
        logoImageView.setPreserveRatio(false);
        logoImageView.setSmooth(true);
        logoImageView.setStyle("-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 0);");

        // Load logo image
        String logoPath = partner.getLogo();
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                File file = new File(logoPath);
                logoImageView.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {
                e.printStackTrace();
                logoImageView.setImage(new Image("path/to/default/logo.png"));
            }
        }

        // Wrap ImageView in a StackPane to handle overflow and corners
        StackPane imageWrapper = new StackPane(logoImageView);
        imageWrapper.setStyle("-fx-background-radius: 10; -fx-background-color: #f8f9fa;");
        imageWrapper.setClip(new Rectangle(260, 150));

        // Add the wrapped image to the logo container
        logoContainer.getChildren().add(imageWrapper);

        // Simple separator
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e0e0e0;" +
                "-fx-opacity: 0.7;" +
                "-fx-padding: 0 15;");
        separator.setPrefWidth(260);

        // Text content container
        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER_LEFT);
        textContent.setPadding(new Insets(10, 0, 0, 0));

        // Partner type with icon
        HBox typeBox = new HBox(10);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeIcon = new Label("🏢");
        typeIcon.setStyle("-fx-font-size: 16px;");
        String partnerType = partner.getType_partner() != null ? partner.getType_partner().name() : "N/A";
        Label typeLabel = new Label(partnerType);
        typeLabel.setStyle("-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2196f3;");
        typeBox.getChildren().addAll(typeIcon, typeLabel);

        // Email with icon
        HBox emailBox = new HBox(10);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        Label emailIcon = new Label("✉");
        emailIcon.setStyle("-fx-font-size: 14px;");
        String email = partner.getEmail() != null ? partner.getEmail() : "N/A";
        Label emailLabel = new Label(email);
        emailLabel.setStyle("-fx-font-size: 12px;" +
                "-fx-text-fill: #424242;");
        emailLabel.setWrapText(true);
        emailBox.getChildren().addAll(emailIcon, emailLabel);

        // Phone with icon
        HBox phoneBox = new HBox(10);
        phoneBox.setAlignment(Pos.CENTER_LEFT);
        Label phoneIcon = new Label("📞");
        phoneIcon.setStyle("-fx-font-size: 14px;");
        String phoneNumber = partner.getPhone_Number() != null ? partner.getPhone_Number() : "N/A";
        Label phoneLabel = new Label(phoneNumber);
        phoneLabel.setStyle("-fx-font-size: 12px;" +
                "-fx-text-fill: #424242;");
        phoneBox.getChildren().addAll(phoneIcon, phoneLabel);

        // Add all elements to text content
        textContent.getChildren().addAll(typeBox, emailBox, phoneBox);

        // Add view partnerships button
        Button viewPartnershipsButton = new Button("View Partnerships");
        viewPartnershipsButton.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 10;");
        viewPartnershipsButton.setOnAction(event -> {
            switchToDisplayPartnershipHistory(partner); // Call the method to switch to partnership history
        });
        textContent.getChildren().add(viewPartnershipsButton); // Add the button to the text content

        // Add all components to card
        card.getChildren().addAll(logoContainer, separator, textContent);

        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: white;" +
                    "-fx-padding: 15;" +
                    "-fx-background-radius: 10;" +
                    "-fx-effect: dropshadow(gaussian, rgba(33,150,243,0.15), 15, 0, 0, 0);" +
                    "-fx-border-color: #2196f3;" +
                    "-fx-border-radius: 10;");
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white;" +
                    "-fx-padding: 15;" +
                    "-fx-background-radius: 10;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);" +
                    "-fx-border-color: #f0f0f0;" +
                    "-fx-border-radius: 10;");
            card.setScaleX(1);
            card.setScaleY(1);
        });

        // Add click handler to the card
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Check for double-click
                openModifyPartner(partner); // Open the modify window in the same area
            } else {
                // Deselect previously selected card if any
                cardsContainer.getChildren().forEach(node -> {
                    if (node instanceof VBox) {
                        node.setStyle(node.getStyle().replace("-fx-border-color: #2196f3;", "-fx-border-color: #f0f0f0;"));
                    }
                });

                // Select this card
                card.setStyle(card.getStyle().replace("-fx-border-color: #f0f0f0;", "-fx-border-color: #2196f3;"));
                selectedPartner = partner; // Set the selected partner for adding a partnership
                System.out.println("Selected partner: " + partner.getId_partner()); // Use getId() or another method
            }
        });

        return card;
    }

    private void openModifyPartner(Partner partner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/ModifyPartner.fxml"));
            Parent modifyPartnerPane = loader.load();

            ModifyPartnerController controller = loader.getController();
            controller.setPartnerService(partnerService); // Set the existing PartnerService
            controller.setPartner(partner); // Set the selected partner
            controller.setDisplayController(this); // Set the display controller for returning

            // Get the parent BorderPane
            BorderPane parent = (BorderPane) cardsContainer.getScene().getRoot();
            controller.setParentBorderPane(parent); // Pass the parent BorderPane to the ModifyPartnerController

            parent.setCenter(modifyPartnerPane); // Load the ModifyPartner view in the center of the BorderPane

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open modify window");
        }
    }

    @FXML
    private void returnToDisplayPartner() {
        try {
            // Get the current stage
            Stage stage = (Stage) cardsContainer.getScene().getWindow();

            // Load the main page again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the main controller
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Display Partner" view
            homePageController.loadDisplayPartner();

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page DisplayPartner !");
            e.printStackTrace();
        }
    }


    private List<Partner> fetchPartners() {
        // Example implementation, replace with your actual data fetching logic
        return partnerService.getall(); // Ensure this method returns a non-empty list
    }

    @FXML
    private void deleteSelectedPartner() {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner to delete!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Partner");
        alert.setHeaderText("Are you sure you want to delete this partner?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                partnerService.supprimer(selectedPartner); // This now deletes partnerships and contracts first
                initialize(); // Refresh the partner list
                selectedPartner = null; // Clear the selected partner
            }
        });
    }

    @FXML
    private void switchToAddPartner() {
        try {
            // Get the current stage
            Stage stage = (Stage) cardsContainer.getScene().getWindow();

            // Load the EventPlannerHomePage.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the EventPlannerHomePageController
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Add Partner" view
            homePageController.loadAddPartner();

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Error", "Cannot load add partner form!");
            e.printStackTrace();
        }
    }


    @FXML
    private void switchToDisplayPartnership() {
        try {
            // Get the current stage
            Stage stage = (Stage) cardsContainer.getScene().getWindow();

            // Load the EventPlannerHomePage.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the EventPlannerHomePageController
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Display Partnership" view
            homePageController.loadDisplayPartnership();

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Error", "Cannot load partnerships page!");
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToDisplayPartnershipHistory(Partner partner) {
        try {
            // Get the current stage
            Stage stage = (Stage) cardsContainer.getScene().getWindow();

            // Load the main home page again
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/EventPlannerHomePage.fxml"));
            Parent root = loader.load();

            // Get the main controller
            EventPlannerHomePage homePageController = loader.getController();

            // Load the "Display Partnership History" view inside the main page
            homePageController.loadDisplayPartnershipHistory();

            // Get the controller for Partnership History and pass the selected partner
            FXMLLoader historyLoader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/PartnershipHistory.fxml"));
            Parent historyRoot = historyLoader.load();
            DisplayPartnershipHistoryController historyController = historyLoader.getController();
            historyController.setPartner(partner); // Pass the selected partner

            // Set the new scene
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            showAlert("Error", "Could not load partnership history view.");
            e.printStackTrace();
        }
    }


    @FXML
    private void switchToAddPartnership() {
        if (selectedPartner == null) {
            showAlert("Error", "Please select a partner first!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Event/DisplayEvents.fxml"));
            Parent root = loader.load();

            // Get the controller and set it to partner selection mode
            DisplayEventsController controller = loader.getController();
            controller.setPartnerSelectionMode(true, selectedPartner);

            // Get the parent BorderPane and set the new view
            BorderPane parent = (BorderPane) cardsContainer.getScene().getRoot();
            parent.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load events page!");
        }
    }
    private void refreshPartnerCards(List<Partner> partnersToDisplay) {
        cardsContainer.getChildren().clear(); // Clear existing cards

        for (Partner partner : partnersToDisplay) {
            // Create and add your card for each partner
            VBox partnerCard = createPartnerCard(partner); // Implement this method to create a card
            cardsContainer.getChildren().add(partnerCard);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}