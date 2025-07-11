package evoplan.controllers.userModule;

import evoplan.entities.user.*;
import evoplan.services.user.ClientService;
import evoplan.services.user.EventPlannerService;
import evoplan.services.user.InputValidator;
import evoplan.services.user.InstructorService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditUserController {

    @FXML
    private TextField nameField;
    @FXML
    private Label nameError;

    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordError;

    @FXML
    private VBox dynamicFieldsBox; // Contains dynamic fields based on role

    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;

    private User user;

    private InputValidator inputValidator = new InputValidator();

    private ClientService clientService = new ClientService();
    private EventPlannerService eventPlannerService = new EventPlannerService();
    private InstructorService instructorService = new InstructorService();

    public void setUser(User user) {
        this.user = user;
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        passwordField.setText("");

        initializeDynamicFields();
    }

    private void initializeDynamicFields() {
        dynamicFieldsBox.getChildren().clear();
        if (user instanceof Client) {
            createClientFields();
        } else if (user instanceof EventPlanner) {
            createEventPlannerFields();
        } else if (user instanceof Instructor) {
            createInstructorFields();
        }
    }

    private void createClientFields() {
        TextField phoneField = new TextField(((Client) user).getPhoneNumber());
        phoneField.setPromptText("Enter phone number");
        phoneField.setId("phoneField");

        Label phoneError = new Label();
        phoneError.setId("phoneError");
        phoneError.getStyleClass().add("error-message");
        phoneError.setVisible(false);

        dynamicFieldsBox.getChildren().addAll(new Label("Phone Number:"), phoneField, phoneError);
    }

    private void createEventPlannerFields() {
        TextField specializationField = new TextField(((EventPlanner) user).getSpecialization());
        specializationField.setPromptText("Enter specialization");
        specializationField.setId("specializationField");

        ChoiceBox<EventPlannerModule> moduleChoiceBox = new ChoiceBox<>();
        moduleChoiceBox.getItems().addAll(EventPlannerModule.values());
        moduleChoiceBox.setValue(((EventPlanner) user).getAssignedModule());

        dynamicFieldsBox.getChildren().addAll(
                new Label("Specialization:"), specializationField,
                new Label("Assigned Module:"), moduleChoiceBox
        );
    }

    private void createInstructorFields() {
        TextField certificationField = new TextField(((Instructor) user).getCertification());
        certificationField.setPromptText("Enter certification");
        certificationField.setId("certificationField");

        CheckBox approvedCheckbox = new CheckBox("Approved");
        approvedCheckbox.setSelected(((Instructor) user).isApproved());

        dynamicFieldsBox.getChildren().addAll(new Label("Certification:"), certificationField, approvedCheckbox);
    }

    @FXML
    private void handleUpdate() {
        resetErrorMessages();

        boolean valid = true;

        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            nameError.setText("Name is required");
            nameError.setVisible(true);
            valid = false;
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            emailError.setVisible(true);
            valid = false;
        } else if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            emailError.setText("Invalid email format");
            emailError.setVisible(true);
            valid = false;
        } else if (inputValidator.isEmailExists(email) && !email.equals(user.getEmail())) {
            emailError.setText("Email already exists");
            emailError.setVisible(true);
            valid = false;
        }

        // Password validation
        if (!passwordField.getText().isEmpty() && passwordField.getText().length() < 6) {
            passwordError.setText("8 min length - 1 upperCase- 1 digit- 1 special Char");
            passwordError.setVisible(true);
            valid = false;
        }

        if (!valid) return; // Stop execution if validation fails

        // Updating user data
        if (user instanceof Client) {
            Client client = (Client) user;
            client.setName(nameField.getText());
            client.setEmail(email);
            if (!passwordField.getText().isEmpty()) {
                client.setPassword(passwordField.getText());
            }
            client.setPhoneNumber(((TextField) dynamicFieldsBox.lookup("#phoneField")).getText());
            updateClient(client);
        } else if (user instanceof EventPlanner) {
            EventPlanner eventPlanner = (EventPlanner) user;
            eventPlanner.setName(nameField.getText());
            eventPlanner.setEmail(email);
            if (!passwordField.getText().isEmpty()) {
                eventPlanner.setPassword(passwordField.getText());
            }
            eventPlanner.setSpecialization(((TextField) dynamicFieldsBox.lookup("#specializationField")).getText());
            eventPlanner.setAssignedModule((EventPlannerModule) ((ChoiceBox<?>) dynamicFieldsBox.getChildren().get(3)).getValue());
            updateEventPlanner(eventPlanner);
        } else if (user instanceof Instructor) {
            Instructor instructor = (Instructor) user;
            instructor.setName(nameField.getText());
            instructor.setEmail(email);
            if (!passwordField.getText().isEmpty()) {
                instructor.setPassword(passwordField.getText());
            }
            instructor.setCertification(((TextField) dynamicFieldsBox.lookup("#certificationField")).getText());
            instructor.setApproved(((CheckBox) dynamicFieldsBox.getChildren().get(2)).isSelected());
            updateInstructor(instructor);
        }

        // Close dialog
        ((Stage) updateButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void updateClient(Client client) {
        clientService.updateUser(client);
    }

    private void updateEventPlanner(EventPlanner eventPlanner) {
        eventPlannerService.updateUser(eventPlanner);
    }

    private void updateInstructor(Instructor instructor) {
        instructorService.updateUser(instructor);
    }

    private void resetErrorMessages() {
        nameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
    }
}
