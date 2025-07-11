package evoplan.controllers.userModule;

import evoplan.controllers.EventPlannerHomePage.Session.SessionController;
import evoplan.entities.user.*;
import evoplan.services.event.EventService;
import evoplan.services.user.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class LoginController {

    public Label btnForgot;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblErrors;
    @FXML
    private Button btnSignin;

    private final AuthService authService = new AuthService();

    private final AppSessionManager session = AppSessionManager.getInstance();
    InputValidator inputValidator = new InputValidator();

    EmailService emailService = new EmailService();
    @FXML
    public void initialize() {
        btnForgot.setOnMouseClicked(event -> handlePasswordReset());
        // Check if a user session exists
        if (session.isLoggedIn()) {
            System.out.println("User already logged in. Redirecting...");
            navigateBasedOnRole(); // Redirect to the appropriate dashboard
        }
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        boolean valid = validateLoginForm(username, password);

        if (valid) {
            User user = authService.login(username, password); // This should return a User with the correct role

            if (user != null) {
                String role = authService.getUserRole(user.getId()); // Fetch the role separately

                // Retrieve the full subclass object based on role
                User fullUser = fetchUserWithCorrectSubclass(user.getId(), role);

                session.login(fullUser, role); // Store correct subclass in session
                navigateBasedOnRole();
            } else {
                lblErrors.setText("Invalid credentials. Please try again.");
            }
        } else {
            lblErrors.setText("Please fill in both fields.");
        }
    }


    private User fetchUserWithCorrectSubclass(int userId, String role) {

        ClientService clientService = new ClientService();
        AdministratorService administratorService = new AdministratorService();
        EventPlannerService eventPlannerService = new EventPlannerService();
        InstructorService instructorService = new InstructorService();


        switch (role.toUpperCase()) {
            case "CLIENT":
                return clientService.getUser(userId); // Returns Client subclass
            case "EVENTPLANNER":
                return eventPlannerService.getUser(userId); // Returns EventPlanner subclass
            case "INSTRUCTOR":
                return instructorService.getUser(userId); // Returns Instructor subclass
            case "ADMINISTRATOR":
                return administratorService.getUser(userId); // Returns Administrator subclass
            default:
                return AppSessionManager.getInstance().getCurrentUser(); // Returns generic User
        }
    }


    private boolean validateLoginForm(String username, String password) {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    private void navigateBasedOnRole() {
        if (!session.isLoggedIn()) {
            lblErrors.setText("No user logged in.");
            return;
        }

        String role = session.getUserRole();
        Stage currentStage = (Stage) btnSignin.getScene().getWindow();

        switch (role) {
            case "CLIENT":
                navigateToPage(currentStage, "/ClientHomePage/ClientHomePage.fxml");
                break;
            case "ADMINISTRATOR":
                navigateToPage(currentStage, "/SideBar.fxml");
                break;
            case "INSTRUCTOR":
                navigateToPage(currentStage, "/SideBarInst.fxml");
                break;
            case "EVENTPLANNER":
                navigateToPage(currentStage, "/EventPlannerHomePage/EventPLannerHomePage.fxml");
                break;
            default:
                lblErrors.setText("Role not found.");
                break;
        }
    }

    private void navigateToPage(Stage currentStage, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblErrors.setText("Error loading page.");
        }
    }

    @FXML
    private void handleSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnSignin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblErrors.setText("Error loading signup page.");
        }
    }


    private void googleSignUpRoute() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/googleRegister.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnSignin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblErrors.setText("Error loading signup page.");
        }
    }

    @FXML
    private Button googleLoginButton;

    @FXML
    private void handleGoogleLogin() {
        try {
            Stage currentStage = (Stage) btnSignin.getScene().getWindow();
            // Authenticate with Google and get the email
            String email = GoogleAuthService.getAuthenticatedEmail(); // Assuming you created this method

            if (email != null && inputValidator.isEmailExists(email)) {
                User user = authService.loginWithGoogle(email);
                String role = authService.getUserRole(user.getId());
                User fullUser = fetchUserWithCorrectSubclass(user.getId(), role);
                if (user != null) {

                    session.login(fullUser,role); // Store user session
                    navigateBasedOnRole(); // Redirect based on role
                } else {
                    lblErrors.setText("Google authentication failed. Please try again.");

                }
            } else {
                session.setGoogleAuthRegisterMail(email);
                googleSignUpRoute();
            }
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            lblErrors.setText("An error occurred during authentication.");
        }
    }

    @FXML
    private void handlePasswordReset() {
        // Prompt for the email
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Email Verification");
        emailDialog.setHeaderText("Enter your registered email to proceed.");
        emailDialog.setContentText("Email:");

        Optional<String> enteredEmail = emailDialog.showAndWait();

        if (enteredEmail.isPresent()&& InputValidator.isValidEmail(enteredEmail.get())) {
            String email = enteredEmail.get();

            // Fetch the userId based on the provided email
            int userId = authService.getUserIdByEmail(email);

            if (userId == -1) {
                showAlert("Error", "No user found with this email.");
                return;
            }

            //Get the user's role
            String role = authService.getUserRole(userId);

            if (role == null || role.isEmpty()) {
                showAlert("Error", "User role could not be determined.");
                return;
            }

            //Fetch the correct subclass (Client, Administrator, EventPlanner, Instructor)
            User user = fetchUserWithCorrectSubclass(userId, role);

            if (user == null) {
                showAlert("Error", "User not found.");
                return;
            }

            // Step 5: Send a verification code email
            EmailService emailService = new EmailService();
            emailService.sendVerificationEmail(email);

            // Step 6: Ask the user to enter the verification code
            TextInputDialog codeDialog = new TextInputDialog();
            codeDialog.setTitle("Email Verification");
            codeDialog.setHeaderText("Enter the verification code sent to your email.");
            codeDialog.setContentText("Verification Code:");

            Optional<String> enteredCode = codeDialog.showAndWait();

            // Step 7: Verify the code
            if (enteredCode.isPresent() && VerificationStorage.verifyCode(email, enteredCode.get())) {
                // If the code is correct, prompt the user to reset their password
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

                // Step 8: Process password update
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
                        showAlert("Error", "Password requires 1 uppercase, 1 lowercase, 1 number, and 1 special character.");
                        return;
                    }
                    ProfileController profileController = new ProfileController();
                    switch (role) {
                        case "CLIENT":
                            Client client = (Client) user;
                            profileController.updateClientField(client, null, null, newPassword, null);
                            break;
                        case "INSTRUCTOR":
                            Instructor instructor = (Instructor) user;
                            profileController.updateInstructorField(instructor, null, null, newPassword, null);
                            break;
                        case "ADMINISTRATOR":
                            Administrator administrator = (Administrator) user;
                            profileController.updateAdminField(administrator, null, null, newPassword);
                            break;
                        case "EVENTPLANNER":
                            EventPlanner eventPlanner = (EventPlanner) user;
                            profileController.updateEventPlannerField(eventPlanner, null, null, newPassword, null);

                            break;
                    }


                    showAlert("Success", "Password updated successfully.");
                }
            } else {
                showAlert("Error", "Incorrect verification code.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}
