package evoplan.controllers;

import evoplan.entities.event.Event;
import evoplan.entities.event.Reservation;
import evoplan.entities.event.TypeStatusRes;
import evoplan.services.event.ReservationService;
import evoplan.services.user.AppSessionManager;
import evoplan.utils.FlouciPaymentService;
import evoplan.utils.QRCodeGenerator;
import evoplan.utils.EmailSender;
import evoplan.utils.ImageMerger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ReservationClientController implements Initializable {
    @FXML private Text Eventnameid;
    @FXML private Text dateId;
    @FXML private Text EventName;
    @FXML private Text PRICEiD;
    @FXML private ImageView eventImage;
    @FXML private Text emailId;

    private Event currentEvent;
    private final ReservationService reservationService = new ReservationService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    public void setEventDetails(Event event) {
        this.currentEvent = event;
        String email = AppSessionManager.getInstance().getCurrentUserEmail();

        if (currentEvent != null) {
            Eventnameid.setText(currentEvent.getNom());
            emailId.setText(email);
            dateId.setText(currentEvent.getDateDebut().toString());
            PRICEiD.setText(String.valueOf(currentEvent.getPrix()) + " €");
            EventName.setText(currentEvent.getNom());

            if (currentEvent.getImageEvent() != null && !currentEvent.getImageEvent().isEmpty()) {
                try {
                    Image image = new Image(new File(currentEvent.getImageEvent()).toURI().toString());
                    eventImage.setImage(image);
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void ConfirmerC(ActionEvent event) {
        if (currentEvent == null) {
            showAlert("Erreur", "Aucun événement sélectionné !");
            return;
        }

        if (currentEvent.getPrix() > 0) {
            openPaymentInterface();
        } else {
            processReservation();
        }
    }

    private void openPaymentInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientHomePage/Events/PaymentView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            PaymentController controller = loader.getController();
            controller.setEvent(currentEvent, this);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la page de paiement.");
            e.printStackTrace();
        }
    }

    public void processReservation() {
        Integer clientId = AppSessionManager.getInstance().getCurrentUserId();
        if (clientId == null) {
            showAlert("Erreur", "Aucun utilisateur connecté !");
            return;
        }

        String email = AppSessionManager.getInstance().getCurrentUserEmail();
        String userName = AppSessionManager.getInstance().getCurrentUserName();

        try {
            Reservation reservation = new Reservation(currentEvent.getIdEvent(), clientId, TypeStatusRes.CONFIRMEE);
            reservationService.addReservation(reservation);
            showAlert("Succès", "Réservation confirmée !");
        } catch (RuntimeException e) {
            showAlert("Erreur", e.getMessage());
        }

        try {
            String qrCodePath = "ticket_qrcode.png";
            String qrCodeData = "Événement: " + currentEvent.getNom() + "\nDate: " + currentEvent.getDateDebut() + "\nRéservé par: " + userName;
            QRCodeGenerator.generateQRCode(qrCodeData, qrCodePath);

            String ticketPath = "ticket_final.png";
            BufferedImage ticket = ImageMerger.createEventTicket(
                    currentEvent.getNom(),
                    currentEvent.getDateDebut().toString(),
                    currentEvent.getPrix() + " €",
                    currentEvent.getLieu().name(),
                    currentEvent.getImageEvent(),
                    qrCodePath
            );
            ImageIO.write(ticket, "png", new File(ticketPath));

            EmailSender.sendEmailWithQRCode(email, ticketPath);
            showAlert("Succès", "Réservation confirmée et ticket envoyé à " + email);

            Stage stage = (Stage) eventImage.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de générer le ticket.");
            e.printStackTrace();
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
