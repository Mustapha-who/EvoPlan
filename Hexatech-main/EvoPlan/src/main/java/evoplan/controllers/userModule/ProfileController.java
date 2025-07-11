package evoplan.controllers.userModule;



import evoplan.entities.user.*;
import evoplan.services.user.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Optional;

public class ProfileController {

    @FXML
    public Label emailLabel;
    @FXML
    public TextField emailField;
    @FXML
    public Button emailEditButton;
    @FXML
    public Label passwordLabel;
    @FXML
    public TextField passwordField;
    @FXML
    public Button passwordEditButton;
    @FXML
    public Label phoneLabel;
    @FXML
    public TextField phoneNumberField;
    @FXML
    public Button phoneEditButton;
    @FXML
    public Label specializationLabel;
    @FXML
    public TextField specializationField;
    @FXML
    public Button specializationEditButton;
    @FXML
    public Label moduleLabel;
    @FXML
    public TextField moduleField;

    @FXML
    public Label certificationLabel;
    @FXML
    public TextField certificationField;
    @FXML
    public Button certificationEditButton;
    @FXML
    public Button logoutButton;
    @FXML
    public Label roleLabel;
    @FXML
    public Label fullNameLabel;
    @FXML
    public ImageView profileImage;
    @FXML
    public Label phonetitle;
    @FXML
    public Label certificationtitle;
    @FXML
    public Label specializationtitle;
    @FXML
    public Button NameEditButton;
    @FXML
    public TextField fullNameField;
    @FXML
    private AppSessionManager sessionManager = AppSessionManager.getInstance();

    Client tempClientInstance ;
    EventPlanner tempEventPlannerInstance ;
    Instructor tempInstructorInstance ;
    Administrator tempAdministratorInstance ;
    EmailService emailService = new EmailService();
    InputValidator inputValidator = new InputValidator();
    public void initialize() {
        if (sessionManager.isLoggedIn()) {
            User currentUser = sessionManager.getCurrentUser();
            String userRole = sessionManager.getUserRole();

            fullNameLabel.setText(currentUser.getName());
            fullNameField.setText(currentUser.getName());
            fullNameField.setVisible(false);
            roleLabel.setText(userRole);
            emailLabel.setText(currentUser.getEmail());
            emailField.setText(currentUser.getEmail());
            emailField.setVisible(false);

            setUpFieldsBasedOnRole(userRole);
            setUpEditButtons();

        } else {
            System.out.println("No user logged in");
        }
    }



    private void setUpFieldsBasedOnRole(String userRole) {
        User currentUser = sessionManager.getCurrentUser();
        int rowIndex = 1;
        int rowIndexForLabel = 1;
        int rowIndexForButton = 1;
        // Get the current user
        if (currentUser != null) {
            if ("EVENTPLANNER".equals(userRole)) {
                EventPlanner eventPlanner = (EventPlanner) currentUser;

                tempEventPlannerInstance = eventPlanner;

                specializationField.setText(eventPlanner.getSpecialization());
                moduleField.setText(eventPlanner.getAssignedModule().toString());
                specializationField.setVisible(true);
                specializationLabel.setVisible(true);
                specializationtitle.setVisible(true);
                specializationtitle.setVisible(true);
                moduleField.setVisible(true);
                moduleLabel.setVisible(true);
                specializationEditButton.setVisible(true);
                GridPane.setRowIndex(specializationField, rowIndex+1);
                GridPane.setRowIndex(specializationLabel, rowIndexForLabel+1);
                GridPane.setRowIndex(specializationEditButton, rowIndexForButton+1);
                GridPane.setRowIndex(specializationtitle, rowIndexForLabel+1);
                GridPane.setRowIndex(moduleField, rowIndex+2);
                GridPane.setRowIndex(moduleLabel, rowIndexForLabel+2);
                setUpEditableField(specializationLabel, specializationField, specializationEditButton);
            } else if ("CLIENT".equals(userRole)) {
                Client client = (Client) currentUser;
                tempClientInstance = client;

                phoneNumberField.setVisible(true);
                phoneNumberField.setText(client.getPhoneNumber());
                phoneLabel.setVisible(true);
                phoneEditButton.setVisible(true);
                phonetitle.setVisible(true);
                GridPane.setRowIndex(phoneNumberField, rowIndex+1);
                GridPane.setRowIndex(phonetitle, rowIndexForLabel+1);
                GridPane.setRowIndex(phoneEditButton, rowIndexForButton+1);
                setUpEditableField(phoneLabel, phoneNumberField, phoneEditButton);
            } else if ("INSTRUCTOR".equals(userRole)) {
                Instructor instructor = (Instructor) currentUser;

                tempInstructorInstance = instructor;

                certificationField.setText(instructor.getCertification());
                certificationField.setVisible(true);
                certificationLabel.setVisible(true);
                certificationEditButton.setVisible(true);
                certificationtitle.setVisible(true);
                GridPane.setRowIndex(certificationField, rowIndex+1);
                GridPane.setRowIndex(certificationLabel, rowIndexForLabel+1);
                GridPane.setRowIndex(certificationEditButton, rowIndexForButton+1);
                GridPane.setRowIndex(certificationtitle, rowIndex+1);
                setUpEditableField(certificationLabel, certificationField, certificationEditButton);
            }else if ("ADMINISTRATOR".equals(userRole)) {
                System.out.println(currentUser);
                tempAdministratorInstance = (Administrator) currentUser;
            }
        }
    }

    private void setUpEditButtons() {
        setUpEditableField(emailLabel, emailField, emailEditButton);
        setUpEditableField(fullNameLabel,fullNameField,NameEditButton);
    }

    private void setUpEditableField(Label label, TextField textField, Button button) {
        // Initial set up
        label.setText(textField.getText());
        textField.setVisible(false);
        label.setVisible(true);
        button.setText("Edit");

        // Button click handler
        button.setOnAction(event -> {
            if (textField.isVisible()) {
                // Saving the updated data
                label.setText(textField.getText());  // Update label with the new value
                textField.setVisible(false);
                label.setVisible(true);
                button.setText("Edit");

                // Using switch-case to handle different fields
                String updatedValue = textField.getText();
                switch (textField.getId()) {
                    case "emailField":
                        if (InputValidator.isValidEmail(updatedValue) && !inputValidator.isEmailExists(updatedValue)) {
                            // Step 1: Send verification email (code is generated & stored inside EmailService)
                            emailService.sendVerificationEmail(updatedValue);

                            // Step 2: Prompt user for verification code
                            TextInputDialog dialog = new TextInputDialog();
                            dialog.setTitle("Email Verification");
                            dialog.setHeaderText("A verification code has been sent to " + updatedValue);
                            dialog.setContentText("Enter the verification code:");

                            Optional<String> userInput = dialog.showAndWait();
                            if (userInput.isPresent()) {
                                String inputCode = userInput.get();


                                if (VerificationStorage.verifyCode(updatedValue, inputCode)) {

                                    // Update only email
                                    switch (sessionManager.getUserRole()){
                                        case "CLIENT": updateClientField(tempClientInstance, null, updatedValue, null, null);

                                            break;
                                        case "INSTRUCTOR": updateInstructorField(tempInstructorInstance, null, updatedValue, null, null);
                                            break;
                                        case  "ADMINISTRATOR": updateAdminField(tempAdministratorInstance,  null,updatedValue, null);
                                            break;
                                        case  "EVENTPLANNER": updateEventPlannerField(tempEventPlannerInstance,null,updatedValue,null,null);
                                            break;
                                    }
                                    emailField.setText(updatedValue);
                                    sessionManager.getCurrentUser().setEmail(updatedValue);

                                } else {
                                    showError("Invalid verification code. Please try again.");
                                }
                            } else {
                                showError("Verification cancelled.");
                                emailField.setText(sessionManager.getCurrentUser().getEmail());
                            }
                        } else {
                            showError("Email exist or is Invalid.");
                            emailField.setText(sessionManager.getCurrentUser().getEmail());
                        }

                        break;
                    case "fullNameField":
                        switch (sessionManager.getUserRole()){
                            case "CLIENT": updateClientField(tempClientInstance,  updatedValue,null, null, null);
                                break;
                            case "INSTRUCTOR": updateInstructorField(tempInstructorInstance, updatedValue,null,  null, null);
                                break;
                            case  "ADMINISTRATOR": updateAdminField(tempAdministratorInstance,updatedValue,  null, null);
                                break;
                            case  "EVENTPLANNER": updateEventPlannerField(tempEventPlannerInstance,updatedValue,null,null,null);
                                break;
                        }
                          // Update only name
                        fullNameLabel.setText(updatedValue);
                        sessionManager.getCurrentUser().setName(updatedValue);
                        break;
                    case "phoneNumberField":
                        updateClientField(tempClientInstance, null, null, null, updatedValue);  // Update only phone number
                        phoneNumberField.setText(updatedValue);
                        tempClientInstance.setPhoneNumber(updatedValue);
                        break;
                    case "specializationField":
                        updateEventPlannerField(tempEventPlannerInstance, null, null, null, updatedValue);  // Update only phone number
                        specializationField.setText(updatedValue);
                        tempEventPlannerInstance.setSpecialization(updatedValue);
                        break;
                    case "certificationField":
                        updateInstructorField(tempInstructorInstance, null, null, null, updatedValue);  // Update only phone number
                        certificationField.setText(updatedValue);
                        tempInstructorInstance.setCertification(updatedValue);
                        break;
                    default:
                        System.out.println("Unknown field: " + label.getText());
                }



            } else {
                // Show the text field again
                textField.setVisible(true);
                textField.setText(label.getText());
                label.setVisible(false);
                button.setText("Save");
            }
        });
    }

    private void saveUserToDatabase() {
        System.out.println("Saving user");
    }



    public void updateClientField(Client client, String newName, String newEmail, String newPassword, String newPhoneNumber) {
        ClientService clientService = new ClientService();
        String currentName = (newName != null) ? newName : client.getName();
        String currentEmail = (newEmail != null) ? newEmail : client.getEmail();
        String currentPassword = (newPassword != null) ? newPassword : null;
        String currentPhoneNumber = (newPhoneNumber != null) ? newPhoneNumber : client.getPhoneNumber();
        Client tempclient = new Client(client.getId(), currentEmail, currentPassword, currentName, currentPhoneNumber  ) ;

        clientService.updateUser(tempclient);


    }
    public void updateAdminField(Administrator administrator , String newName, String newEmail, String newPassword) {
        AdministratorService adminService = new AdministratorService();
        String currentName = (newName != null) ? newName : administrator.getName();
        String currentEmail = (newEmail != null) ? newEmail : administrator.getEmail();
        String currentPassword = (newPassword != null) ? newPassword : null;
        Administrator tempAdmin = new Administrator(administrator.getId(),currentEmail,currentPassword,currentName) ;

        adminService.updateUser(tempAdmin);


    }

    public void updateEventPlannerField(EventPlanner eventPlanner, String newName, String newEmail, String newPassword, String newspec ) {
        EventPlannerService eventPlannerService = new EventPlannerService();
        String currentName = (newName != null) ? newName : eventPlanner.getName();
        String currentEmail = (newEmail != null) ? newEmail : eventPlanner.getEmail();
        String currentPassword = (newPassword != null) ? newPassword : null;
        String currentSpec = (newspec != null) ? newspec : eventPlanner.getSpecialization();
        EventPlannerModule currentModule =  eventPlanner.getAssignedModule();
        EventPlanner tempEventplanner = new EventPlanner(eventPlanner.getId(),currentEmail,currentPassword,currentName,currentSpec,currentModule);

        eventPlannerService.updateUser(tempEventplanner);


    }
    public void updateInstructorField(Instructor instructor, String newName, String newEmail, String newPassword, String newcertif ) {
        InstructorService instructorService = new InstructorService();
        String currentName = (newName != null) ? newName : instructor.getName();
        String currentEmail = (newEmail != null) ? newEmail : instructor.getEmail();
        String currentPassword = (newPassword != null) ? newPassword : null;
        String currentCertif = (newcertif != null) ? newcertif : instructor.getCertification();
        Instructor tempInstructor = new Instructor(instructor.getId(), currentEmail,currentPassword,currentName,currentCertif,instructor.isApproved());

        instructorService.updateUser(tempInstructor);


    }

    public void updatePassword()
    {

                if(sessionManager.getUserRole().equals("CLIENT")) {

                        smsPwdResetClient();
                }else {
                    pwdResetUsingEmail();
                }


    }

    public void smsPwdResetClient(){
        // Generate verification code
        String verificationCode = VerificationCodeGenerator.generateCode();
        VerificationStorage.saveVerificationCode(tempClientInstance.getEmail(), verificationCode);

        // Send the SMS with the verification code
        phoneService phoneService = new phoneService();
        try {
            phoneService.SendPasswordRestSMS( verificationCode);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send SMS verification code.");
            return;
        }

        // Ask user to enter the verification code
        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setTitle("SMS Verification");
        codeDialog.setHeaderText("Enter the verification code sent to your phone.");
        codeDialog.setContentText("Verification Code:");

        Optional<String> enteredCode = codeDialog.showAndWait();

        if (enteredCode.isPresent() && VerificationStorage.verifyCode(tempClientInstance.getEmail(), enteredCode.get())) {
            // If code is correct, show password reset fields
            Dialog<Pair<String, String>> passwordDialog = new Dialog<>();
            passwordDialog.setTitle("Reset Password");
            passwordDialog.setHeaderText("Enter your new password.");

            // Set up password fields
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("New Password");
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm Password");

            GridPane grid = new GridPane();
            grid.add(new Label("New Password:"), 0, 0);
            grid.add(newPasswordField, 1, 0);
            grid.add(new Label("Confirm Password:"), 0, 1);
            grid.add(confirmPasswordField, 1, 1);
            passwordDialog.getDialogPane().setContent(grid);

            // Add OK and Cancel buttons
            passwordDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return new Pair<>(newPasswordField.getText(), confirmPasswordField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = passwordDialog.showAndWait();

            // Process password update
            if (result.isPresent()) {
                String newPassword = result.get().getKey();
                String confirmPassword = result.get().getValue();

                // Check if passwords match
                if (!newPassword.equals(confirmPassword)) {
                    showAlert("Error", "Passwords do not match. Try again.");
                    return;
                }

                // Check password complexity
                if (!PasswordValidator.isValidPassword(newPassword)) {
                    showAlert("Error", "Password requires 1 uppercase , 1 lowercase , 1 number and 1 special character.");
                    return;
                }


                updateClientField(tempClientInstance, null, null, newPassword, null);


                showAlert("Success", "Password updated successfully.");
            }
        } else {
            showAlert("Error", "Incorrect verification code.");
        }
    }


    public void pwdResetUsingEmail() {



        EmailService emailService = new EmailService();

        emailService.sendVerificationEmail(sessionManager.getCurrentUser().getEmail());

        // Ask user to enter the verification code
        TextInputDialog codeDialog = new TextInputDialog();
        codeDialog.setTitle("Email Verification");
        codeDialog.setHeaderText("Enter the verification code sent to the user email.");
        codeDialog.setContentText("Verification Code:");

        Optional<String> enteredCode = codeDialog.showAndWait();

        if (enteredCode.isPresent() && VerificationStorage.verifyCode(sessionManager.getCurrentUserEmail(), enteredCode.get())) {
            // If code is correct, show password reset fields
            Dialog<Pair<String, String>> passwordDialog = new Dialog<>();
            passwordDialog.setTitle("Reset Password");
            passwordDialog.setHeaderText("Enter your new password.");

            // Set up password fields
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("New Password");
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm Password");

            GridPane grid = new GridPane();
            grid.add(new Label("New Password:"), 0, 0);
            grid.add(newPasswordField, 1, 0);
            grid.add(new Label("Confirm Password:"), 0, 1);
            grid.add(confirmPasswordField, 1, 1);
            passwordDialog.getDialogPane().setContent(grid);

            // Add OK and Cancel buttons
            passwordDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            passwordDialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return new Pair<>(newPasswordField.getText(), confirmPasswordField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = passwordDialog.showAndWait();

            // Process password update
            if (result.isPresent()) {
                String newPassword = result.get().getKey();
                String confirmPassword = result.get().getValue();

                // Check if passwords match
                if (!newPassword.equals(confirmPassword)) {
                    showAlert("Error", "Passwords do not match. Try again.");
                    return;
                }

                // Check password complexity
                if (!PasswordValidator.isValidPassword(newPassword)) {
                    showAlert("Error", "Password requires 1 uppercase , 1 lowercase , 1 number and 1 special character.");
                    return;
                }
                switch (sessionManager.getUserRole()) {
                    case "INSTRUCTOR":
                        updateInstructorField(tempInstructorInstance, null, null, newPassword, null);
                        break;
                    case "ADMINISTRATOR":
                        updateAdminField(tempAdministratorInstance, null, null, newPassword);
                        break;
                    case "EVENTPLANNER":

                        updateEventPlannerField(tempEventPlannerInstance, null, null, newPassword, null);

                        break;
                }


            }
        } else {
            showAlert("Error", "Incorrect verification code.");
        }
    }

    @FXML
    private void onLogoutButtonClicked() {

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Logout Confirmation");
        confirmationAlert.setHeaderText("Are you sure you want to log out?");
        confirmationAlert.setContentText("Any unsaved changes will be lost.");


        Optional<ButtonType> result = confirmationAlert.showAndWait();


        if (result.isPresent() && result.get() == ButtonType.OK) {

            AppSessionManager.getInstance().logout();
            System.out.println("User logged out successfully.");


            navigateToPage((Stage) logoutButton.getScene().getWindow(), "/Login.fxml");
        } else {

            System.out.println("Logout canceled.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void navigateToPage(Stage currentStage, String fxmlFile) {
        try {
            // Load the desired FXML file based on the role
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Set the new scene and show it
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }



}
