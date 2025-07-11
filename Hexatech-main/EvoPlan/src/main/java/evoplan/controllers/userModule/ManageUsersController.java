package evoplan.controllers.userModule;

import evoplan.entities.user.Client;
import evoplan.entities.user.EventPlanner;
import evoplan.entities.user.Instructor;
import evoplan.entities.user.User;
import evoplan.services.user.ClientService;
import evoplan.services.user.EventPlannerService;
import evoplan.services.user.InstructorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ManageUsersController {

    public Button clearButton;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> moduleColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> phoneNumberColumn; // For Client
    @FXML
    private TableColumn<User, String> specializationColumn; // For EventPlanner
    @FXML
    private TableColumn<User, String> certificationColumn; // For Instructor
    @FXML
    private TableColumn<User, Void> actionColumn;
    @FXML
    private Pagination pagination;
    @FXML
    private TextField searchField;  // Search field
    @FXML
    private ComboBox<String> filterComboBox;

    private List<User> allUsers = new ArrayList<>();

    private List<User> filteredUsers = new ArrayList<>();

    private int rowsPerPage = 10; //

    ClientService clientService = new ClientService();
    EventPlannerService eventPlannerService = new EventPlannerService();
    InstructorService instructorService = new InstructorService();

    public void initialize() {
        filterComboBox.setItems(FXCollections.observableArrayList("Username", "Email", "Role", "Phone Number"));
        filterComboBox.setValue("Username");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers());
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filterUsers());

        // Initialize the table with proper columns
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> {
            // Extract the class name as the role
            String role = cellData.getValue().getClass().getSimpleName();
            return new SimpleStringProperty(role);
        });

        // Display specific attributes for each type of user
        phoneNumberColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Client) {
                return new SimpleStringProperty(((Client) cellData.getValue()).getPhoneNumber());
            }
            return new SimpleStringProperty("");
        });

        specializationColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof EventPlanner) {
                return new SimpleStringProperty(((EventPlanner) cellData.getValue()).getSpecialization());
            }
            return new SimpleStringProperty("");
        });

        moduleColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof EventPlanner) {
                return new SimpleStringProperty(((EventPlanner) cellData.getValue()).getAssignedModule().toString());
            }
            return new SimpleStringProperty(""); // Empty for other user types
        });
        certificationColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Instructor) {
                return new SimpleStringProperty(((Instructor) cellData.getValue()).getCertification());
            }
            return new SimpleStringProperty("");
        });

        // Set the action column to contain the Edit and Delete buttons
        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Add action for Edit button
                editButton.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    openEditUserForm(selectedUser);
                });

                // Add action for Delete button
                deleteButton.setOnAction(event -> {
                    User selectedUser = getTableView().getItems().get(getIndex());
                    confirmAndDeleteUser(selectedUser);
                });

                // Add the buttons to the cell
                HBox hbox = new HBox(10, editButton, deleteButton);
                setGraphic(hbox);
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, editButton, deleteButton));
                }
            }
        });


        loadUsers();


    }

    public void loadUsers() {
        // Fetch all users from your service classes
        //List<User> users = new ArrayList<>();
        // Add all users except the Administrator
        allUsers.addAll(clientService.displayUsers());
        allUsers.addAll(eventPlannerService.displayUsers());
        allUsers.addAll(instructorService.displayUsers());


        filterUsers();
    }

    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase();
        String selectedFilter = filterComboBox.getValue();

        filteredUsers = allUsers.stream()
                .filter(user -> {
                    boolean matches = false;
                    switch (selectedFilter) {
                        case "Username":
                            matches = user.getName().toLowerCase().contains(searchText);
                            break;
                        case "Email":
                            matches = user.getEmail().toLowerCase().contains(searchText);
                            break;
                        case "Role":
                            matches = user.getClass().getSimpleName().toLowerCase().contains(searchText);
                            break;
                        case "Phone Number":
                            if (user instanceof Client) {
                                matches = ((Client) user).getPhoneNumber().toLowerCase().contains(searchText);
                            }
                            break;
                    }
                    return matches;
                })
                .collect(Collectors.toList());

        pagination.setPageCount((int) Math.ceil((double) filteredUsers.size() / rowsPerPage));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }


    // Method to open the AddUser popup window
    @FXML
    private void openAddUserForm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddUser.fxml"));
            Parent root = loader.load();

            // Create a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Add New User");

            // Set a larger size (adjust as needed)
            Scene scene = new Scene(root, 400, 500); // Width: 400px, Height: 500px
            stage.setScene(scene);

            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with main window
            stage.setResizable(false); // Prevent resizing
            stage.showAndWait(); // Wait until the form is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditUserForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditUser.fxml"));
            Parent root = loader.load();

            // Create a new Stage (popup window)
            Stage stage = new Stage();
            stage.setTitle("Edit User");

            // Set a larger size (adjust as needed)
            Scene scene = new Scene(root, 400, 500); // Width: 400px, Height: 500px
            stage.setScene(scene);

            stage.initModality(Modality.APPLICATION_MODAL); // Block interaction with main window
            stage.setResizable(false); // Prevent resizing
            stage.showAndWait(); // Wait until the form is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private HBox createPage(int pageIndex) {
        // Calculate which users to display on the current page
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, filteredUsers.size());

        // Update the table items for the current page
        userTable.setItems(FXCollections.observableList(filteredUsers.subList(fromIndex, toIndex)));

        return new HBox(); // Return an empty HBox (since we're directly setting the TableView's items)
    }
    @FXML
    private void clearSearchField(ActionEvent event) {
        // Clear the search field
        searchField.clear();

        // Reset the filter and show all users
        filterUsers();
    }



    private void confirmAndDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("This action cannot be undone.");

        // Show the confirmation dialog and wait for user input
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (user instanceof Client) {
                    clientService.deleteUser(user.getId());
                } else if (user instanceof EventPlanner) {
                    eventPlannerService.deleteUser(user.getId());
                } else if (user instanceof Instructor) {
                    instructorService.deleteUser(user.getId());
                }

                // Refresh the table
                userTable.getItems().remove(user);
            }
        });
    }


}
