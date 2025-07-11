package evoplan.utils;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;

public class EmailSender {
    public static void sendEmailWithQRCode(String toEmail, String qrCodePath) {
        String fromEmail = "mehdi.ayachi@esprit.tn"; // Remplace avec ton email Outlook
        String password = "gbdhnwyqtvbzzgsh"; // Remplace avec ton mot de passe Outlook

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.office365.com"); // Serveur SMTP Outlook
        props.put("mail.smtp.port", "587"); // Port sécurisé
        props.put("mail.smtp.auth", "true"); // Authentification requise
        props.put("mail.smtp.starttls.enable", "true"); // STARTTLS activé

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Votre ticket de réservation");

            // Corps de l'email
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Merci pour votre réservation. Voici votre QR Code.");

            // Attacher le QR Code
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(qrCodePath));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email envoyé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}