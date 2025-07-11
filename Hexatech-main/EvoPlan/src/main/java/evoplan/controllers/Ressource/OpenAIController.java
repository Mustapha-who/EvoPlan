package evoplan.controllers.Ressource;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.Duration;

public class OpenAIController {

    @FXML
    private TextField promptField; // Champ de texte pour saisir le prompt

    @FXML
    private TextArea responseArea; // Zone de texte pour afficher la réponse de l'IA

    private OpenAiService openAiService; // Service pour interagir avec l'API OpenAI

    // Clé API OpenAI (à remplacer par votre clé)
    private static final String OPENAI_API_KEY = "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";
    // Méthode d'initialisation du contrôleur
    @FXML
    public void initialize() {
        // Initialiser le service OpenAI avec la clé API
        openAiService = new OpenAiService(OPENAI_API_KEY, Duration.ofSeconds(30));
    }

    // Méthode appelée lorsque l'utilisateur clique sur le bouton "Envoyer"
    @FXML
    private void onSendPrompt() {
        // 1. Récupérer le texte saisi par l'utilisateur
        String prompt = promptField.getText();

        // 2. Vérifier si le champ de texte est vide
        if (prompt.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez saisir un prompt.");
            return;
        }

        try {
            // 3. Créer une requête de complétion pour OpenAI
            CompletionRequest request = CompletionRequest.builder()
                    .prompt(prompt) // Le texte saisi par l'utilisateur
                    .model("gpt-3.5-turbo") // Utilisez un modèle plus récent
                    .maxTokens(100) // Nombre maximum de tokens dans la réponse
                    .temperature(0.7) // Contrôle la créativité de la réponse (0 = strict, 1 = créatif)
                    .build();

            // 4. Envoyer la requête à OpenAI et récupérer la réponse
            String response = openAiService.createCompletion(request).getChoices().get(0).getText();

            // 5. Afficher la réponse dans la zone de texte
            responseArea.setText(response);
        } catch (Exception e) {
            // 6. Gérer les erreurs (par exemple, connexion échouée)
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite lors de la communication avec OpenAI : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}