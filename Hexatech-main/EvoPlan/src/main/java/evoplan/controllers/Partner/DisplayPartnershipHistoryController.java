package evoplan.controllers.Partner;

import evoplan.entities.Partnership;
import evoplan.entities.Partner;
import evoplan.services.Partner.PartnershipService;
import  evoplan.controllers.EventPlannerHomePage.EventPlannerHomePage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.stage.Stage;

import java.io.IOException;

public class DisplayPartnershipHistoryController {

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
    private TextField searchField;

    private PartnershipService partnershipService;
    private ObservableList<Partnership> partnerships;
    private Partner selectedPartner; // To hold the selected partner

    public DisplayPartnershipHistoryController() {
        partnershipService = new PartnershipService();
        partnerships = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Set up the columns in the table
        idPartnershipColumn.setCellValueFactory(new PropertyValueFactory<>("id_partnership"));
        idPartnerColumn.setCellValueFactory(new PropertyValueFactory<>("id_partner"));
        idEventColumn.setCellValueFactory(new PropertyValueFactory<>("id_event"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        termsColumn.setCellValueFactory(new PropertyValueFactory<>("terms"));

        // Load partnerships from the service
        partnerships.setAll(partnershipService.getAll());
        partnershipsTable.setItems(partnerships);

        // Setup search functionality
        setupSearchFunctionality();
    }

    public void setPartner(Partner partner) {
        this.selectedPartner = partner; // Set the selected partner
        loadPartnershipsForPartner(); // Load partnerships for this partner
    }

    private void loadPartnershipsForPartner() {
        if (selectedPartner != null) {
            partnerships.setAll(partnershipService.getPartnershipsByPartnerId(selectedPartner.getId_partner())); // Fetch partnerships for the selected partner
            partnershipsTable.setItems(partnerships); // Set the items in the table
        }
    }

    private void setupSearchFunctionality() {
        FilteredList<Partnership> filteredData = new FilteredList<>(partnerships, b -> true);

        // Listen for changes in the search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
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

    @FXML
    private  void navigateBack(){
        returnToDisplayPartner();
    }

    public void returnToDisplayPartner() {
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
            showAlert("Error", "Cannot load DisplayPartner page!");
            e.printStackTrace();
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