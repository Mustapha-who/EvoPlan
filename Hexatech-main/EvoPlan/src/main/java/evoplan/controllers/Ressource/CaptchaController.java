package evoplan.controllers.Ressource;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils; // Import correct

import java.awt.*;
import java.awt.image.BufferedImage;

public class CaptchaController {

    @FXML
    private ImageView captchaImageView; // ImageView pour afficher le CAPTCHA

    @FXML
    private TextField captchaTextField; // Champ de saisie pour le CAPTCHA

    private String captchaText; // Texte CAPTCHA généré

    /**
     * Initialise le contrôleur.
     */
    @FXML
    public void initialize() {
        // Générer un CAPTCHA au démarrage
        generateCaptcha();
    }
    @FXML
    public void generateCaptcha() {
        captchaText = generateRandomText(6); // Génère un texte aléatoire de 6 caractères
        BufferedImage captchaImage = createCaptchaImage(captchaText);
        Image fxImage = SwingFXUtils.toFXImage(captchaImage, null); // Utilisation de SwingFXUtils
        captchaImageView.setImage(fxImage);
    }
    @FXML
    public void validateCaptcha() {
        String userCaptcha = captchaTextField.getText();
        if (userCaptcha != null && userCaptcha.equalsIgnoreCase(captchaText)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "CAPTCHA valide !");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "CAPTCHA invalide. Veuillez réessayer.");
            generateCaptcha(); // Régénérer un nouveau CAPTCHA
            captchaTextField.clear(); // Effacer le champ de saisie
        }
    }

    /**
     * Génère un texte aléatoire pour le CAPTCHA.
     *
     * @param length La longueur du texte à générer.
     * @return Le texte généré.
     */
    private String generateRandomText(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Crée une image CAPTCHA à partir du texte donné.
     *
     * @param text Le texte à afficher dans l'image.
     * @return L'image CAPTCHA.
     */
    private BufferedImage createCaptchaImage(String text) {
        BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 200, 50);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(text, 50, 35);
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < 10; i++) {
            int x1 = (int) (Math.random() * 200);
            int y1 = (int) (Math.random() * 50);
            int x2 = (int) (Math.random() * 200);
            int y2 = (int) (Math.random() * 50);
            g2d.drawLine(x1, y1, x2, y2);
        }
        g2d.dispose();
        return image;
    }

    /**
     * Affiche une boîte de dialogue avec un message.
     *
     * @param alertType Le type d'alerte (INFORMATION, ERROR, etc.).
     * @param title     Le titre de la boîte de dialogue.
     * @param message   Le message à afficher.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}