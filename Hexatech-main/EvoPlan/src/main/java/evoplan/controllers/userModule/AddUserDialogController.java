package evoplan.controllers.userModule;

import evoplan.entities.user.Client;
import evoplan.entities.user.EventPlanner;
import evoplan.entities.user.EventPlannerModule;
import evoplan.entities.user.Instructor;
import evoplan.services.user.*;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.sql.Connection;

public class AddUserDialogController {
    @FXML
    public VBox clientFields;

    @FXML
    public TextField clientPhoneNumberField;
    @FXML
    public VBox eventPlannerFields;
    @FXML
    public TextField plannerDepartmentField;
    @FXML
    public VBox instructorFields;
    @FXML
    public TextField instructorCertificationField;
    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private VBox dynamicFieldsContainer;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private Label usernameError;
    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label roleError;

    @FXML
    private Label clientPhoneNumberError;
    @FXML
    private Label plannerDepartmentError;
    @FXML
    private Label instructorCertificationError;

    Connection cnx;

    ClientService clientService = new ClientService();
    EventPlannerService eventPlannerService = new EventPlannerService();
    InstructorService instructorService = new InstructorService();

    InputValidator inputValidator = new InputValidator();

    @FXML
    private void initialize() {


        // Listen for role selection changes
        roleComboBox.setOnAction(event -> updateDynamicFields());
    }

    private void updateDynamicFields() {
        dynamicFieldsContainer.getChildren().clear(); // Clear previous fields

        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null) return;

        switch (selectedRole) {
            case "Client":
                addClientFields();
                break;
            case "Event Planner":
                addEventPlannerFields();
                break;
            case "Instructor":
                addInstructorFields();
                break;
        }
    }



    @FXML
    public void onRoleSelected() {
        String selectedRole = roleComboBox.getValue();

        // Clear previous fields
        dynamicFieldsContainer.getChildren().clear();

        if ("Client".equals(selectedRole)) {
            addClientFields();
        } else if ("Event Planner".equals(selectedRole)) {
            addEventPlannerFields();
        } else if ("Instructor".equals(selectedRole)) {
            addInstructorFields();
        }
    }

    private void addClientFields() {
        TextField phoneNumberField = new TextField();
        phoneNumberField.setPromptText("Phone Number");
        phoneNumberField.setId("phoneNumberField"); // Used for retrieval
        dynamicFieldsContainer.getChildren().add(phoneNumberField);
    }


    private void addEventPlannerFields() {
        TextField specializationField = new TextField();
        specializationField.setPromptText("Specialization");
        specializationField.setId("specializationField"); // Used for retrieval

        ComboBox<String> moduleComboBox = new ComboBox<>();
        moduleComboBox.getItems().addAll("Logistics", "Resources", "Workshops", "Schedules");
        moduleComboBox.setPromptText("Select Module");
        moduleComboBox.setId("moduleComboBox");

        dynamicFieldsContainer.getChildren().addAll(specializationField, moduleComboBox);
    }


    private void addInstructorFields() {
        TextField certificationField = new TextField();
        certificationField.setPromptText("Certification");
        certificationField.setId("certificationField");

        CheckBox approvedCheckBox = new CheckBox("Approved?");
        approvedCheckBox.setId("approvedCheckBox");

        dynamicFieldsContainer.getChildren().addAll(certificationField, approvedCheckBox);
    }


    private TextField createTextField(String placeholder) {
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        return textField;
    }

    @FXML
    private void onSubmit() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        // Clear previous error messages and reset styles
        clearErrorStyles();

        boolean hasError = false;

        // Validate Username
        if (username.isEmpty()) {
            usernameError.setText("Username is required.");
            usernameError.setVisible(true);
            hasError = true;
        }

        // Validate Email
        if (email.isEmpty()) {
            emailError.setText("Email is required.");
            emailError.setVisible(true);
            hasError = true;
        } else if (!InputValidator.isValidEmail(email)) {
            emailError.setText("Invalid email format.");
            emailError.setVisible(true);
            hasError = true;
        } else if (inputValidator.isEmailExists(email)) {
            emailError.setText("Email already exists!");
            emailError.setVisible(true);
            hasError = true;
        }

        // Validate Password
        if (password.isEmpty()) {
            passwordError.setText("Password is required.");
            passwordError.setVisible(true);
            hasError = true;
        } else if (!PasswordValidator.isValidPassword(password)) {
            passwordError.setText("Password must be at least 6 characters.");
            passwordError.setVisible(true);
            hasError = true;
        }

        // Validate Role
        if (selectedRole == null) {
            roleError.setText("Please select a role.");
            roleError.setVisible(true);
            hasError = true;
        }

        // If there are validation errors, do not submit
        if (hasError) {
            return;
        }


        // Proceed with submission if no errors
        switch (selectedRole) {
            case "Client":
                handleClientSubmission(username, email, password);
                break;
            case "Event Planner":
                handleEventPlannerSubmission(username, email, password);
                break;
            case "Instructor":
                handleInstructorSubmission(username, email, password);
                break;
        }
    }

    private void clearErrorStyles() {
        // Reset error message visibility and text
        usernameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        roleError.setVisible(false);
        clientPhoneNumberError.setVisible(false);
        plannerDepartmentError.setVisible(false);
        instructorCertificationError.setVisible(false);
    }

//    private void clearErrorStyles() {
//        usernameField.setStyle("");  // Reset the styles for username
//        emailField.setStyle("");     // Reset the styles for email
//        passwordField.setStyle("");  // Reset the styles for password
//        roleComboBox.setStyle("");   // Reset the styles for comboBox
//
//        // Clear any error messages (you can adjust how to clear them based on your UI layout)
//        dynamicFieldsContainer.getChildren().removeIf(node -> node instanceof Label);
//    }






    private void handleClientSubmission(String username, String email, String password) {

        TextField phoneNumberField = (TextField) dynamicFieldsContainer.lookup("#phoneNumberField");
        String phoneNumber = phoneNumberField.getText();
        boolean hasError = false;

        if (phoneNumber.isEmpty()) {
            clientPhoneNumberError.setText("Phone number is required.");
            clientPhoneNumberError.setVisible(true);
            hasError = true;
        }
        if (InputValidator.isValidPhoneNumber(phoneNumber)) {
            clientPhoneNumberError.setText("Phone number must be 8 digits.");
            clientPhoneNumberError.setVisible(true);
            hasError = true;
        }

        if (!hasError) {

            Client client = new Client(email, password, phoneNumber, username);
            clientService.addUser(client);
            System.out.println("✅ Client added successfully!");
        }
    }


    private void handleEventPlannerSubmission(String username, String email,String password) {
        TextField specializationField = (TextField) dynamicFieldsContainer.lookup("#specializationField");
        ComboBox<String> moduleComboBox = (ComboBox<String>) dynamicFieldsContainer.lookup("#moduleComboBox");

        // Retrieve password
        String specialization = specializationField.getText();
        String selectedModuleStr = moduleComboBox.getValue();

        boolean hasError = false;

        if (specialization.isEmpty()) {
            plannerDepartmentError.setText("Specialization is required.");
            plannerDepartmentError.setVisible(true);
            hasError = true;
        }

        // Validate module selection
        if (selectedModuleStr == null) {
            plannerDepartmentError.setText("Module selection is required.");
            plannerDepartmentError.setVisible(true);
            hasError = true;
        }


        if (!hasError) {
            try {
                // Convert string to enum
                EventPlannerModule selectedModule = EventPlannerModule.valueOf(selectedModuleStr.toUpperCase());

                EventPlanner eventPlanner = new EventPlanner(email, password, username, specialization, selectedModule);
                eventPlannerService.addUser(eventPlanner);
                System.out.println("✅ Event Planner added successfully!");

            } catch (IllegalArgumentException e) {
                System.out.println("❌ Invalid module selected. Please choose a valid option.");
            }
        }
    }




    private void handleInstructorSubmission(String username, String email, String password) {
        TextField certificationField = (TextField) dynamicFieldsContainer.lookup("#certificationField");
        CheckBox approvedCheckBox = (CheckBox) dynamicFieldsContainer.lookup("#approvedCheckBox");

        String certification = certificationField.getText();
        boolean isApproved = approvedCheckBox.isSelected();
        boolean hasError = false;
        // Validate certification
        if (certification.isEmpty()) {
            instructorCertificationError.setText("Certification is required.");
            instructorCertificationError.setVisible(true);
            hasError = true;
        }


        if (!hasError) {
            Instructor instructor = new Instructor(email, password, username, certification, isApproved);
            instructorService.addUser(instructor);
            System.out.println("✅ Instructor added successfully!");
        }
    }







}
