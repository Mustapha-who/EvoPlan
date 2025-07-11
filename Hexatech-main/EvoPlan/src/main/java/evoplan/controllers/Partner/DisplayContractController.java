package evoplan.controllers.Partner;

import evoplan.entities.Contract;
import evoplan.services.Partner.ContractService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.io.File;
import java.io.IOException;
import evoplan.services.Partner.PDFExporter;

public class DisplayContractController {

    @FXML
    private TableView<Contract> contractsTable;
    @FXML
    private TableColumn<Contract, Integer> idContractColumn;
    @FXML
    private TableColumn<Contract, Integer> idPartnershipColumn;
    @FXML
    private TableColumn<Contract, Integer> idPartnerColumn;
    @FXML
    private TableColumn<Contract, String> startDateColumn;
    @FXML
    private TableColumn<Contract, String> endDateColumn;
    @FXML
    private TableColumn<Contract, String> termsColumn;
    @FXML
    private TableColumn<Contract, String> statusColumn;
    @FXML
    private TableColumn<Contract, Void> actionsColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;

    private ContractService contractService;
    private ObservableList<Contract> contracts;

    public DisplayContractController() {

        contractService = new ContractService();
        contracts = FXCollections.observableArrayList();
        contractService.checkAndUpdateExpiredContracts();
    }

    @FXML
    public void initialize() {
        // Set up the columns in the table
        idContractColumn.setCellValueFactory(new PropertyValueFactory<>("id_contract"));
        idPartnershipColumn.setCellValueFactory(new PropertyValueFactory<>("id_partnership"));
        idPartnerColumn.setCellValueFactory(new PropertyValueFactory<>("id_partner"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        termsColumn.setCellValueFactory(new PropertyValueFactory<>("terms"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyBtn = new Button("Modify");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, modifyBtn, deleteBtn);

            {
                // Style buttons
                modifyBtn.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");

                // Modify button action
                modifyBtn.setOnAction(event -> {
                    Contract contract = getTableView().getItems().get(getIndex());
                    openModifyContract(contract);
                });

                // Delete button action
                deleteBtn.setOnAction(event -> {
                    Contract contract = getTableView().getItems().get(getIndex());
                    deleteContract(contract);
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

        // Load initial data
        refreshTable();

        // Setup search and filter
        setupSearchAndFilter();
    }

    private void setupSearchAndFilter() {
        // Create a filtered list for status filtering
        FilteredList<Contract> filteredData = new FilteredList<>(contracts, p -> true);

        // Status filter functionality
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(contract -> {
                if (newValue == null || newValue.equals("All")) {
                    return true; // Show all contracts
                }
                return contract.getStatus().equalsIgnoreCase(newValue);
            });
        });

        // Create a second filtered list for search functionality
        FilteredList<Contract> searchFilteredData = new FilteredList<>(filteredData, p -> true);

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchFilteredData.setPredicate(contract -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true; // Show all contracts if search field is empty
                }

                String lowerCaseFilter = newValue.toLowerCase();
                // Check if any of the fields match the search input
                return String.valueOf(contract.getId_contract()).toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(contract.getId_partnership()).toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(contract.getId_partner()).toLowerCase().contains(lowerCaseFilter) ||
                        contract.getTerms().toLowerCase().contains(lowerCaseFilter) ||
                        contract.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Combine filtered data with sorted list
        SortedList<Contract> sortedData = new SortedList<>(searchFilteredData);
        sortedData.comparatorProperty().bind(contractsTable.comparatorProperty());
        contractsTable.setItems(sortedData);
    }

    public void refreshTable() {
        contracts.setAll(contractService.getAll()); // Get all contracts from the service
        contractsTable.setItems(contracts); // Set the items in the table
    }

    private void deleteContract(Contract contract) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Contract");
        alert.setHeaderText("Delete Contract");
        alert.setContentText("Are you sure you want to delete this contract?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                contractService.supprimer(contract);
                refreshTable();
            }
        });
    }

    private void openModifyContract(Contract contract) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/ModifyContract.fxml"));
            Parent root = loader.load();

            ModifyContractController controller = loader.getController();
            controller.setContract(contract);
            controller.setContractService(contractService);
            controller.setDisplayController(this);

            // Get the parent layout (e.g., BorderPane) and set the new content
            BorderPane mainLayout = (BorderPane) contractsTable.getScene().getRoot();
            mainLayout.setCenter(root); // Replace the center content with the modify form
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open modify window");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            // Load the previous view (e.g., DisplayPartner.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/Partner/DisplayPartnership.fxml"));
            AnchorPane partnerPane = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Get the parent of the current scene (which should be the main layout)
            BorderPane mainLayout = (BorderPane) stage.getScene().getRoot();

            // Set the new content in the center of the main layout
            mainLayout.setCenter(partnerPane);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot load previous page!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void exportToPDF() {
        // Get the selected contract from the TableView
        Contract selectedContract = contractsTable.getSelectionModel().getSelectedItem();

        if (selectedContract == null) {
            // Notify the user if no contract is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Contract Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a contract to export.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        //fileChooser.setInitialFileName("Contract.pdf"); // Set a default file name
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        // Set the initial directory to the user's home directory
        // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File file = fileChooser.showSaveDialog(contractsTable.getScene().getWindow());

        if (file != null) {
            try {
                // Export the selected contract to PDF
                PDFExporter.exportToPDF(selectedContract, file.getAbsolutePath());

                /*// Notify the user of success
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("PDF exported successfully to:\n" + file.getAbsolutePath());
                alert.showAndWait();*/
            } catch (IOException e) {
                // Notify the user of the error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to export PDF:\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }}


