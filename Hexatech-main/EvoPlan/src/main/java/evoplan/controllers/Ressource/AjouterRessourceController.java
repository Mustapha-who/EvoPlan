package evoplan.controllers.Ressource;

import evoplan.entities.ressource.Ressource;
import evoplan.entities.ressource.Equipment;
import evoplan.entities.ressource.Venue;
import evoplan.main.DatabaseConnection;
import evoplan.services.ressource.RessourceService;
import evoplan.controllers.Ressource.CaptchaController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.web.WebView;

import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.awt.image.BufferedImage;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import javafx.stage.DirectoryChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import java.util.Map; // Pour Map
import java.util.stream.Collectors; // Pour Collectors
import org.apache.poi.ss.usermodel.*; // Pour Row, Cell, etc.
import org.apache.poi.xssf.usermodel.*; // Pour XSSFWorkbook, XSSFSheet, XSSFDrawing, etc.
import org.apache.poi.xddf.usermodel.chart.*; // Pour XDDFCategoryAxis, XDDFValueAxis, etc.
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class AjouterRessourceController {

    // Déclarations des champs FXML
    @FXML
    private ListView<Ressource> ressourceListView;

    @FXML
    private TextField nomField;
    @FXML
    private Button openMapButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeField;
    @FXML
    private CheckBox availabilityField;
    @FXML
    private VBox equipmentFields;
    @FXML
    private TextField equipmentTypeField;
    @FXML
    private TextField quantityField;
    @FXML
    private VBox venueFields;
    @FXML
    private TextField addressField;
    @FXML
    private TextField capacityField;
    @FXML
    private WebView webView;
    @FXML
    private Label bilanLabel;
    @FXML
    private Button exportButton;
    @FXML
    private TextField promptField; // Champ de texte pour saisir le prompt
    @FXML
    private Button openAIButton; // Référence au bouton dans le FXML

    @FXML
    private TextField captchaTextField; // Champ de saisie pour le CAPTCHA

    private String captchaText; // Texte CAPTCHA généré

    @FXML
    private ImageView captchaImageView; // ImageView pour afficher le CAPTCHA

    @FXML
    private TextArea responseArea; // Zone de texte pour afficher la réponse de l'IA

    private OpenAiService openAiService; // Service pour interagir avec l'API OpenAI


    private final RessourceService ressourceService = new RessourceService();

    // Méthode d'initialisation
    @FXML
    public void initialize() {
        // Initialiser les éléments du ComboBox
        ObservableList<String> types = FXCollections.observableArrayList("Equipment", "Venue");
        typeField.setItems(types);

        // Gérer le changement de type
        typeField.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals("Equipment")) {
                equipmentFields.setVisible(true);
                venueFields.setVisible(false);
            } else if (newVal.equals("Venue")) {
                equipmentFields.setVisible(false);
                venueFields.setVisible(true);
            }
        });

        // Charger la liste des ressources au démarrage
        refreshRessourceList();

        // Générer un CAPTCHA au démarrage
        generateCaptcha();
    }

    // Méthode pour ajouter une ressource
    @FXML
    private void handleAjouter() {
        // Valider le CAPTCHA avant de continuer
        if (captchaImageView == null) {
            System.err.println("captchaImageView n'a pas été initialisé !");
        } else {
            System.out.println("captchaImageView est correctement initialisé.");
        }
        // Récupérer les valeurs saisies dans le formulaire
        String nom = nomField.getText();
        String type = typeField.getValue();
        boolean availability = availabilityField.isSelected();

        // Vérification des champs obligatoires
        if (nom.isEmpty() || type == null) {
            showError("Le nom et le type de la ressource sont obligatoires.");
            return;
        }

        Ressource ressource = null;

        // Vérification si c'est un "Equipment" ou un "Venue" et création de la ressource
        if (type.equals("Equipment")) {
            // Vérification des champs spécifiques à Equipment
            String equipmentType = equipmentTypeField.getText();
            String quantityText = quantityField.getText();

            if (equipmentType.isEmpty() || quantityText.isEmpty()) {
                showError("Les champs spécifiques à l'équipement sont obligatoires.");
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityText);
                ressource = new Equipment(nom, type, availability, equipmentType, quantity);
            } catch (NumberFormatException e) {
                showError("La quantité doit être un nombre entier.");
                return;
            }
        } else if (type.equals("Venue")) {
            // Vérification des champs spécifiques à Venue
            String address = addressField.getText();
            String capacityText = capacityField.getText();

            if (address.isEmpty() || capacityText.isEmpty()) {
                showError("Les champs spécifiques à la salle sont obligatoires.");
                return;
            }

            try {
                int capacity = Integer.parseInt(capacityText);
                ressource = new Venue(nom, type, availability, address, capacity);
            } catch (NumberFormatException e) {
                showError("La capacité doit être un nombre entier.");
                return;
            }
        }

        // Si la ressource a été correctement créée, l'ajouter à la liste et au service
        if (ressource != null) {
            ressourceService.addRessource(ressource);
            refreshRessourceList();
            clearFields();
        }
    }

    // Méthode pour valider le CAPTCHA
    @FXML
    private void validateCaptcha() {
        String userCaptcha = captchaTextField.getText().trim(); // Supprime les espaces inutiles

        if (userCaptcha.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir le CAPTCHA.");
        } else if (userCaptcha.equalsIgnoreCase(captchaText)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "CAPTCHA valide !");
            // Ajouter ici la logique pour traiter le formulaire après validation du CAPTCHA
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "CAPTCHA invalide. Veuillez réessayer.");
            generateCaptcha(); // Régénérer un nouveau CAPTCHA
            captchaTextField.clear(); // Effacer le champ de saisie
        }
    }

    // Méthode pour générer un CAPTCHA
    private void generateCaptcha() {
        // Générer le texte CAPTCHA
        captchaText = generateRandomText(6);

        // Créer une image CAPTCHA
        BufferedImage captchaImage = createCaptchaImage(captchaText);

        // Convertir BufferedImage en Image JavaFX
        Image fxImage = SwingFXUtils.toFXImage(captchaImage, null);

        // Afficher l'image dans l'ImageView
        captchaImageView.setImage(fxImage);
    }

    // Méthode pour générer un texte aléatoire
    private String generateRandomText(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    // Méthode pour créer une image CAPTCHA
    private BufferedImage createCaptchaImage(String text) {
        int width = 200;
        int height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fond blanc
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Texte CAPTCHA
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(text, 50, 35);

        // Lignes aléatoires pour brouiller l'image
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < 10; i++) {
            int x1 = (int) (Math.random() * width);
            int y1 = (int) (Math.random() * height);
            int x2 = (int) (Math.random() * width);
            int y2 = (int) (Math.random() * height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Ajouter du bruit (points aléatoires)
        for (int i = 0; i < 50; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            g2d.drawLine(x, y, x, y);
        }

        g2d.dispose();
        return image;
    }

    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Méthode pour modifier une ressource
    @FXML
    private void handleModifier() {
        try {
            System.out.println("Bouton Modifier cliqué !"); // Debug message
            // Récupérer la ressource sélectionnée
            Ressource selected = ressourceListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                System.out.println("Ressource sélectionnée : " + selected.getName()); // Debug message
                // Charger le fichier FXML de l'interface de modification
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/ressource/ModifierRessource.fxml"));
                Parent root = loader.load();

                // Obtenir le contrôleur de l'interface de modification
                ModifierRessourceController modifierController = loader.getController();

                // Initialiser les données de la ressource dans l'interface de modification
                modifierController.initData(selected);

                // Créer une nouvelle scène et l'afficher
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier une ressource");
                stage.show();
            } else {
                System.out.println("Aucune ressource sélectionnée !"); // Debug message
                showError("Veuillez sélectionner une ressource à modifier.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Afficher la stack trace de l'exception
            showError("Une erreur s'est produite : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        Ressource selected = ressourceListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ressourceService.deleteRessource(selected.getId());
            refreshRessourceList();
            clearFields();
        }
    }


    // Méthode pour rafraîchir la liste des ressources
    private void refreshRessourceList() {
        ressourceListView.getItems().clear();
        ressourceListView.getItems().addAll(ressourceService.getAllRessources());
    }

    // Méthode pour effacer les champs du formulaire
    private void clearFields() {
        nomField.clear();
        typeField.getSelectionModel().clearSelection();
        availabilityField.setSelected(false);
        equipmentTypeField.clear();
        quantityField.clear();
        addressField.clear();
        capacityField.clear();
        equipmentFields.setVisible(false);
        venueFields.setVisible(false);
    }

    // Méthode pour afficher une erreur
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour rechercher des ressources
    @FXML
    private void handleRechercher() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            ressourceListView.setItems(FXCollections.observableArrayList(ressourceService.getAllRessources()));
        } else {
            ObservableList<Ressource> filteredList = FXCollections.observableArrayList();
            for (Ressource r : ressourceService.getAllRessources()) {
                if (r.getName().toLowerCase().contains(query)) {
                    filteredList.add(r);
                }
            }
            ressourceListView.setItems(filteredList);
        }
    }

    // Méthode pour afficher la carte
    @FXML
    public void afficherCarte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/ressource/Map.fxml"));
            Parent root = loader.load();
            MapController mapController = loader.getController();
            mapController.setPosition(34.0, 9.0, 6); // Centrer sur la Tunisie, zoom 6
            Scene scene = new Scene(root, 800, 600);
            Stage stage = new Stage();
            stage.setTitle("Carte");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour récupérer les ressources depuis la base de données
    private List<Ressource> fetchRessourcesFromDatabase() {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT id, name, type, availability FROM ressource";

        try (Connection conn = DatabaseConnection.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                boolean available = rs.getBoolean("availability");
                Ressource ressource = new Ressource(id, name, type, available);
                ressources.add(ressource);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'exécution de la requête : " + e.getMessage());
            e.printStackTrace();
        }

        return ressources;
    }

    // Méthode pour exporter en Excel
    @FXML
    void onExportExcel(ActionEvent event) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            List<Ressource> ressources = fetchRessourcesFromDatabase();

            if (ressources.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune donnée à exporter.");
                return;
            }

            // Créer une feuille pour chaque type de ressource
            Map<String, List<Ressource>> ressourcesByType = ressources.stream()
                    .collect(Collectors.groupingBy(Ressource::getType));

            for (Map.Entry<String, List<Ressource>> entry : ressourcesByType.entrySet()) {
                String type = entry.getKey();
                List<Ressource> ressourcesOfType = entry.getValue();

                XSSFSheet typeSheet = workbook.createSheet(type);

                // Créer la ligne d'en-tête
                Row headerRow = typeSheet.createRow(0);
                String[] columns = {"ID", "Nom", "Type", "Disponible"};
                for (int i = 0; i < columns.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                }

                // Remplir les lignes avec les données
                int rowNum = 1;
                for (Ressource ressource : ressourcesOfType) {
                    Row row = typeSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(ressource.getId());
                    row.createCell(1).setCellValue(ressource.getName());
                    row.createCell(2).setCellValue(ressource.getType());
                    row.createCell(3).setCellValue(ressource.isAvailable() ? "Oui" : "Non");
                }
            }

            // Ouvrir une boîte de dialogue pour choisir l'emplacement du fichier
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choisir un emplacement pour enregistrer le fichier Excel");
            File selectedDirectory = directoryChooser.showDialog(null);

            if (selectedDirectory != null) {
                File outputFile = new File(selectedDirectory, "ressources_export.xlsx");
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    workbook.write(fos);
                }
                showAlert(Alert.AlertType.INFORMATION, "Export réussi", "Le fichier Excel a été enregistré ici :\n" + outputFile.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun emplacement n'a été choisi. Le fichier n'a pas été enregistré.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la génération du fichier Excel.");
        }
    }
    @FXML
    private void openOpenAIView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventPlannerHomePage/ressource/openai-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue OpenAI.");
        }
    }


}