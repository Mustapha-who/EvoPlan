package evoplan.services.user;

import evoplan.services.user.VerificationCodeGenerator;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private final String username = "MS_L2qp17@trial-o65qngke2ddgwr12.mlsender.net";
    private final String password = "mssp.gI0ZJd8.k68zxl2ezo3lj905.uXyLo7Q"; //



    public void sendVerificationEmail(String recipientEmail) {
        String verificationCode = VerificationCodeGenerator.generateCode();
        VerificationStorage.saveVerificationCode(recipientEmail, verificationCode);

        // Set properties for Outlook SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.mailersend.net");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.auth", "true");

        // Create session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Email Verification");

            // Email content
            String content = "<h1>Email Verification</h1>"
                    + "<p>Thank you for registering. Use the code below to verify your email:</p>"
                    + "<h2 style='color:blue;'>" + verificationCode + "</h2>"
                    + "<p>If you did not request this, please ignore this email.</p>";

            message.setContent(content, "text/html");

            // Send the email
            Transport.send(message);
            System.out.println("✅ Verification email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send email.");
        }




    }
}

