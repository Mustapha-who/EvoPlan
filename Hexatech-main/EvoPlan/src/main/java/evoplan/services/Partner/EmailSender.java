package evoplan.services.Partner;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailSender {

    private final String senderEmail; // Fixed sender email
    private final String senderPassword; // Fixed sender password


    /**
     * Constructor to initialize the EmailSender with sender credentials.
     *
     * @param senderEmail    The sender's email address.
     * @param senderPassword The sender's email password.
     */
    public EmailSender(String senderEmail, String senderPassword) {
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
    }
    public void sendEmail(String recipientEmail, String subject, String body) {
        // Configure email properties for Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            // Send the email
            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + recipientEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Sends an email with an attachment.
     *
     * @param recipientEmail The recipient's email address.
     * @param subject        The subject of the email.
     * @param body           The body content of the email.
     * @param attachmentPath The file path of the attachment.
     */
    public void sendEmailWithAttachment(String recipientEmail, String subject, String body, String attachmentPath) {
        // Configure email properties for Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Gmail SMTP server
        props.put("mail.smtp.port", "587"); // Gmail secure port
        props.put("mail.smtp.auth", "true"); // Authentication required
        props.put("mail.smtp.starttls.enable", "true"); // Enable STARTTLS

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create the email body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            // Attach the file
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(attachmentPath));

            // Combine the text and attachment parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            // Set the content of the message
            message.setContent(multipart);

            // Send the email
            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + recipientEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}