package evoplan.controllers.Ressource;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import evoplan.entities.ressource.Ressource;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ModifierRessourceController {

    @FXML
    private TextField nomField;
    @FXML
    private ComboBox<String> typeField;
    @FXML
    private CheckBox availabilityField;

    private Ressource ressource; // Ressource à modifier

    // Méthode pour initialiser le contrôleur avec les données de la ressource à modifier
    public void initData(Ressource ressource) {
        this.ressource = ressource;
        nomField.setText(ressource.getName());
        typeField.setValue(ressource.getType());
        availabilityField.setSelected(ressource.isAvailable());
    }

    // Méthode pour sauvegarder les modifications
    @FXML
    private void handleModifier() {
        // Contrôle de saisie : vérifier que tous les champs sont remplis
        if (nomField.getText().isEmpty() || typeField.getValue() == null) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // Récupère les valeurs modifiées
        String nouveauNom = nomField.getText();
        String nouveauType = typeField.getValue();
        boolean nouvelleDisponibilite = availabilityField.isSelected();

        // Met à jour la ressource avec les nouvelles valeurs
        ressource.setName(nouveauNom);
        ressource.setType(nouveauType);
        ressource.setAvailable(nouvelleDisponibilite);

        // Sauvegarde dans la base de données ou autre (par exemple, en appelant un service ou une méthode spécifique)
        // Exemple : ressourceService.updateRessource(ressource);

        // Fermer la fenêtre de modification
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close(); // Ferme la fenêtre une fois la modification effectuée
    }

    // Méthode pour annuler la modification
    @FXML
    private void handleAnnuler() {
        // Fermer la fenêtre sans sauvegarder les changements
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    // Affiche un message d'erreur avec une fenêtre d'alerte
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erreur de saisie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
