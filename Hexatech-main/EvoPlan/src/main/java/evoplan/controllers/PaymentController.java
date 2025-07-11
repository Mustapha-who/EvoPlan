package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.services.user.AppSessionManager;
import evoplan.utils.EmailSender;
import evoplan.utils.FlouciPaymentService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentController {
    @FXML private Label eventName;
    @FXML private Label eventPrice;
    @FXML private Button payButton;

    private Event currentEvent;
    private ReservationClientController reservationController;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String paymentId;

    public void setEvent(Event event, ReservationClientController controller) {
        this.currentEvent = event;
        this.reservationController = controller;
        eventName.setText(event.getNom());
        eventPrice.setText(event.getPrix() + " €");
    }

    @FXML
    void handlePayment(ActionEvent event) {
        String paymentLink = FlouciPaymentService.generatePaymentLink(
                currentEvent.getPrix(),
                "http://localhost/success.html",
                "http://localhost/fail.html",
                "tracking_" + currentEvent.getIdEvent()
        );

        if (paymentLink != null) {
            openPaymentPage(paymentLink);
            paymentId = extractPaymentIdFromLink(paymentLink);
            monitorPaymentStatus();
        } else {
            showAlert("Erreur", "Impossible de générer le lien de paiement.");
        }
    }

    private String extractPaymentIdFromLink(String link) {
        return link.substring(link.lastIndexOf("/") + 1);
    }

    private void openPaymentPage(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir la page de paiement.");
        }
    }

    private void monitorPaymentStatus() {
        executorService.submit(() -> {
            int attempts = 0;
            final int MAX_ATTEMPTS = 150; // Environ 5 minutes

            while (attempts < MAX_ATTEMPTS) {
                try {
                    Thread.sleep(2000);
                    attempts++;

                    // 🔹 Récupération du statut du paiement
                    String status = FlouciPaymentService.checkPaymentStatus(paymentId);
                    System.out.println("📊 Statut du paiement détecté : " + status);

                    if (status != null) {
                        status = status.trim().toLowerCase(); // 🔹 Normalisation du statut

                        if ("success".equals(status)) {
                            Platform.runLater(this::handlePaymentSuccess);
                            break;
                        } else if ("fail".equals(status)) { // 🔥 Vérification correcte de l'échec
                            Platform.runLater(() -> {
                                showAlert("Erreur", "Le paiement a échoué.");
                                closeWindow();
                            });
                            break; // 🚨 Stopper immédiatement la boucle
                        }
                    }

                    // ⏳ Si on atteint la limite, on arrête aussi
                    if (attempts >= MAX_ATTEMPTS) {
                        Platform.runLater(() -> showAlert("Erreur", "Le paiement prend trop de temps. Veuillez vérifier votre transaction."));
                        break;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void handlePaymentSuccess() {
        Platform.runLater(() -> showAlert("Succès", "Paiement validé !"));
        reservationController.processReservation();
    }
    private void closeWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) payButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
