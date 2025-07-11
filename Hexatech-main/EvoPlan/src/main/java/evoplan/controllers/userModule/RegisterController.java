package evoplan.controllers.userModule;

import evoplan.entities.user.Client;
import evoplan.entities.user.Instructor;
import evoplan.services.user.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class RegisterController {

    public Label lblcertifError;
    public Label lblnameErrorInstructor;
    public Label lblnumberError;
    public Label lblnameErrorClient;
    // Client form fields
    @FXML private TextField txtEmailClient;
    @FXML private TextField txtFullNameClient;
    @FXML private PasswordField txtPasswordClient;
    @FXML private PasswordField txtConfirmPasswordClient;
    @FXML private Button btnSignup;
    @FXML private Label btnBackToLogin;
    @FXML private TextField phoneNumber;
    @FXML private Pane paneClient;
    @FXML private Label lblEmailErrorClient;
    @FXML private Label lblPasswordErrorClient;
    @FXML private Label lblConfirmPasswordErrorClient;

    // Instructor form fields
    @FXML private TextField txtEmailInstructor;
    @FXML private TextField txtFullNameInstructor;
    @FXML private TextField txtCertification;
    @FXML private PasswordField txtPasswordInstructor;
    @FXML private PasswordField txtConfirmPasswordInstructor;
    @FXML private Button btnSignupInstructor;
    @FXML private Label btnBackToLoginInstructor;
    @FXML private Pane paneInstructor;
    @FXML private Label lblEmailErrorInstructor;
    @FXML private Label lblPasswordErrorInstructor;
    @FXML private Label lblConfirmPasswordErrorInstructor;

    // Toggle buttons
    @FXML private Button btnClient;
    @FXML private Button btnInstructor;
    EmailService emailService = new EmailService();
    ClientService clientService = new ClientService();
    InstructorService instructorService = new InstructorService();
    InputValidator inputValidator = new InputValidator();
    // Method to switch to the client form
    @FXML
    public void showClientForm() {
        paneInstructor.setVisible(false);
        paneClient.setVisible(true);
        btnClient.setStyle("-fx-background-color: #4166d5; -fx-text-fill: white; -fx-background-radius: 9;");
        btnInstructor.setStyle("-fx-background-color: #d5d5d5; -fx-text-fill: black; -fx-background-radius: 9;");
    }

    // Method to switch to the instructor form
    @FXML
    public void showInstructorForm() {
        paneClient.setVisible(false);
        paneInstructor.setVisible(true);
        btnInstructor.setStyle("-fx-background-color: #4166d5; -fx-text-fill: white; -fx-background-radius: 9;");
        btnClient.setStyle("-fx-background-color: #d5d5d5; -fx-text-fill: black; -fx-background-radius: 9;");
    }

    // Handle client sign-up
    @FXML
    public void handleSignup() {
        String email = txtEmailClient.getText();
        String fullName = txtFullNameClient.getText();
        String password = txtPasswordClient.getText();
        String confirmPassword = txtConfirmPasswordClient.getText();
        String phone = phoneNumber.getText();

        // Validate input fields
        boolean valid = validateClientForm(email, fullName, password, confirmPassword, phone);

        if (valid) {
            // Step 1: Send verification email (code is generated & stored inside EmailService)
            emailService.sendVerificationEmail(email);

            // Step 2: Prompt user for verification code
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Email Verification");
            dialog.setHeaderText("A verification code has been sent to " + email);
            dialog.setContentText("Enter the verification code:");

            Optional<String> userInput = dialog.showAndWait();
            if (userInput.isPresent()) {
                String inputCode = userInput.get();

                // Step 3: Verify the code from storage
                if (VerificationStorage.verifyCode(email, inputCode)) {
                    // Step 4: If verified, create the Client
                    Client client = new Client(email, password, phone, fullName);
                    clientService.addUser(client);
                    showSuccessMessage("Account created successfully!");
                    goBackToLogin();
                } else {
                    showError("Invalid verification code. Please try again.");
                }
            } else {
                showError("Verification cancelled.");
            }
        }
    }

    // Handle instructor sign-up
    @FXML
    public void handleSignupInstructor() {
        String email = txtEmailInstructor.getText();
        String fullName = txtFullNameInstructor.getText();
        String certification = txtCertification.getText();
        String password = txtPasswordInstructor.getText();
        String confirmPassword = txtConfirmPasswordInstructor.getText();

        // Validate input fields
        boolean valid = validateInstructorForm(email, fullName, certification, password, confirmPassword);

        if (valid) {
            // Step 1: Send verification email (code is generated & stored inside EmailService)
            emailService.sendVerificationEmail(email);

            // Step 2: Prompt user for verification code
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Email Verification");
            dialog.setHeaderText("A verification code has been sent to " + email);
            dialog.setContentText("Enter the verification code:");

            Optional<String> userInput = dialog.showAndWait();
            if (userInput.isPresent()) {
                String inputCode = userInput.get();

                // Step 3: Verify the code from storage
                if (VerificationStorage.verifyCode(email, inputCode)) {
                    // Step 4: If verified, create the Instructor
                    Instructor instructor = new Instructor( email, password,fullName, certification, false);
                    instructorService.addUser(instructor);
                    showSuccessMessage("Instructor account created successfully!");
                    goBackToLogin();
                } else {
                    showError("Invalid verification code. Please try again.");
                }
            } else {
                showError("Verification cancelled.");
            }
        }
    }

    // Validate client form inputs
    private boolean validateClientForm(String email, String fullName, String password, String confirmPassword, String phone) {
        boolean isValid = true;

        // Check if fields are empty and set "required" error text
        if (email.isEmpty()) {
            lblEmailErrorClient.setText("Required");
            isValid = false;
        } else {
            lblEmailErrorClient.setText("");
        }

        if (fullName.isEmpty()) {
            lblnameErrorClient.setText("Required");
            isValid = false;
        } else {
            lblnameErrorClient.setText("");
        }

        if (password.isEmpty()) {
            lblPasswordErrorClient.setText("Required");
            isValid = false;
        } else {
            lblPasswordErrorClient.setText("");
        }

        if (confirmPassword.isEmpty()) {
            lblConfirmPasswordErrorClient.setText("Required");
            isValid = false;
        } else {
            lblConfirmPasswordErrorClient.setText("");
        }

        if (phone.isEmpty()) {
            lblnumberError.setText("Required");
            isValid = false;
        } else {
            lblnumberError.setText("");
        }

        if (!InputValidator.isValidPhoneNumber(phone)) {
            lblnumberError.setText("Phone number must be 8 digits");
            isValid = false;
        } else {
            lblnumberError.setText("");
        }


        // Validate email format
        if (!InputValidator.isValidEmail(email)) {
            lblEmailErrorClient.setText("Invalid email format.");
            isValid = false;
        }

        // Check if email already exists
        if (inputValidator.isEmailExists(email)) {
            lblEmailErrorClient.setText("Email already exists.");
            isValid = false;
        }

        // Validate password format
        if (!PasswordValidator.isValidPassword(password)) {
            lblPasswordErrorClient.setText("8 min length- 1 Upper- 1 lower-1 Number - 1 special Character.");
            isValid = false;
        }

        // Check if password and confirm password match
        if (!password.equals(confirmPassword)) {
            lblConfirmPasswordErrorClient.setText("Passwords do not match.");
            isValid = false;
        } else {
            // Clear error if passwords match
            lblConfirmPasswordErrorClient.setText("");
        }

        return isValid;
    }



    // Validate instructor form inputs
    private boolean validateInstructorForm(String email, String fullName, String certification, String password, String confirmPassword) {
        boolean isValid = true;

        // Check if fields are empty and set "required" error text
        if (email.isEmpty()) {
            lblEmailErrorInstructor.setText("Required");
            isValid = false;
        } else {
            lblEmailErrorInstructor.setText("");
        }

        if (fullName.isEmpty()) {
            lblnameErrorInstructor.setText("Required");
            isValid = false;
        } else {
            lblnameErrorInstructor.setText("");
        }
        if (certification.isEmpty()) {
            lblcertifError.setText("Required");
            isValid = false;
        } else {
            lblcertifError.setText("");
        }

        if (password.isEmpty()) {
            lblPasswordErrorInstructor.setText("Required");
            isValid = false;
        } else {
            lblPasswordErrorInstructor.setText("");
        }



        // Validate email format
        if (!InputValidator.isValidEmail(email)) {
            lblEmailErrorInstructor.setText("Invalid email format.");
            isValid = false;
        }

        // Check if email already exists
        if (inputValidator.isEmailExists(email)) {
            lblEmailErrorInstructor.setText("Email already exists.");
            isValid = false;
        }

        // Validate password format
        if (!PasswordValidator.isValidPassword(password)) {
            lblPasswordErrorInstructor.setText("8 min length- 1 Upper- 1 lower-1 Number - 1 special Character.");
            isValid = false;
        }

        // Check if password and confirm password match
        if (!password.equals(confirmPassword)) {
            lblConfirmPasswordErrorInstructor.setText("Passwords do not match.");
            isValid = false;
        } else {
            // Clear error if passwords match
            lblConfirmPasswordErrorInstructor.setText("");
        }

        return isValid;
    }

    // Show success message
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Go back to login
    public void goBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBackToLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Initialize the controller
    @FXML
    public void initialize() {
        paneClient.setVisible(true);
        paneInstructor.setVisible(false);
        btnClient.setStyle("-fx-background-color: #4166d5; -fx-text-fill: white; -fx-background-radius: 9;");
        btnInstructor.setStyle("-fx-background-color: #d5d5d5; -fx-text-fill: black; -fx-background-radius: 9;");
    }


    private void clearClientFields() {
        txtEmailClient.clear();
        txtFullNameClient.clear();
        txtPasswordClient.clear();
        txtConfirmPasswordClient.clear();
        phoneNumber.clear();
    }

    private void clearInstructorFields() {
        txtEmailInstructor.clear();
        txtFullNameInstructor.clear();
        txtPasswordInstructor.clear();
        txtConfirmPasswordInstructor.clear();
        txtCertification.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
