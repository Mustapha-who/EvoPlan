package evoplan.utils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageMerger {
    public static BufferedImage createEventTicket(
            String eventName, String eventDate,String price, String location,
            String eventImagePath, String qrCodePath) throws IOException {

        // Dimensions du ticket
        int width = 1000;
        int height = 350;
        BufferedImage ticket = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = ticket.createGraphics();

        // Activer l'antialiasing pour un rendu plus lisse
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fond blanc général
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Section gauche (fond gris clair)
        g2d.setColor(new Color(245, 245, 245));
        g2d.fillRect(0, 0, 250, height);

        // Section droite (fond violet)
        g2d.setColor(new Color(58, 36, 99));
        g2d.fillRect(750, 0, 250, height);

        // Dessiner une ligne de séparation entre les sections
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(250, 0, 250, height);
        g2d.drawLine(750, 0, 750, height);

        try {
            // Charger et dessiner l'image de l'événement au centre
            BufferedImage eventImage = ImageIO.read(new File(eventImagePath));
            g2d.drawImage(eventImage, 250, 0, 500, height, null);

            // Assombrir légèrement l'image pour une meilleure lisibilité du texte
            g2d.setColor(new Color(0, 0, 0, 50)); // Noir semi-transparent
            g2d.fillRect(250, 0, 500, height);
        } catch (IOException e) {
            // En cas d'erreur avec l'image, utiliser un fond coloré
            g2d.setColor(new Color(120, 120, 200));
            g2d.fillRect(250, 0, 500, height);
            System.err.println("Impossible de charger l'image de l'événement: " + e.getMessage());
        }

        // Section gauche - Texte et informations
        // Titre "EVENT NAME"
        g2d.setColor(new Color(58, 36, 99)); // Violet
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("EVENT NAME", 20, 40);

        // Nom de l'événement
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        drawMultilineString(g2d, eventName, 250, 20, 80);

        // "DOOR OPEN" texte
        g2d.setColor(new Color(58, 36, 99)); // Violet
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("DATE", 20, 140);

        // Date de l'événement avec formatage visuel
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString(formatDate(eventDate), 20, 180);


        // Prix du ticket
        g2d.setColor(new Color(58, 36, 99)); // Violet
        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.drawString("LOCATION", 20, 250);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString(location, 20, 290);

        // Section droite - QR Code et infos supplémentaires
        // Titre EVENT NAME à droite
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("TICKET", 770, 40);

        // VIP info
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("VIP: " + price + " ENTRY PASS", 770, 80);

        // Autres infos
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("DAY: " + formatDateShort(eventDate), 770, 120);
        g2d.drawString("THIS TICKET IS UNIQUE", 770, 150);

        // Ligne de séparation pointillée
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        g2d.drawLine(750, 170, 1000, 170);

        try {
            // Charger et dessiner le QR code
            BufferedImage qrCode = ImageIO.read(new File(qrCodePath));
            g2d.drawImage(qrCode, 790, 190, 150, 150, null);
        } catch (IOException e) {
            // En cas d'erreur avec le QR Code
            g2d.setColor(Color.WHITE);
            g2d.drawRect(790, 190, 150, 150);
            g2d.drawString("QR Code", 830, 270);
            System.err.println("Impossible de charger le QR Code: " + e.getMessage());
        }

        // Code-barres décoratif
        //drawBarcode(g2d, 770, 190, 190, 30);

        g2d.dispose();
        return ticket;
    }

    // Méthode pour dessiner du texte multiligne
    private static void drawMultilineString(Graphics2D g, String text, int maxWidth, int x, int y) {
        FontMetrics metrics = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int lineHeight = metrics.getHeight();
        int currentY = y;

        for (String word : words) {
            if (metrics.stringWidth(currentLine + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                g.drawString(currentLine.toString(), x, currentY);
                currentY += lineHeight;
                currentLine = new StringBuilder(word + " ");
            }
        }

        if (currentLine.length() > 0) {
            g.drawString(currentLine.toString(), x, currentY);
        }
    }

    // Méthode pour formater la date (si format simple comme "2024-10-11")
    private static String formatDate(String dateString) {
        try {
            // Essai de formatage simple de date
            String[] parts = dateString.split("[\\s-]");
            if (parts.length >= 3) {
                return parts[2] + "." + parts[1] + "." + parts[0]; // Format JJ.MM.AAAA
            }
            return dateString; // Renvoyer tel quel si impossible de formatter
        } catch (Exception e) {
            return dateString; // En cas d'erreur, utiliser la chaîne d'origine
        }
    }

    // Méthode pour formatage court de la date
    private static String formatDateShort(String dateString) {
        try {
            String[] parts = dateString.split("[\\s-]");
            if (parts.length >= 3) {
                return parts[2] + " - " + parts[0]; // Format JJ - AAAA
            }
            return dateString;
        } catch (Exception e) {
            return dateString;
        }
    }

    // Méthode pour dessiner un code-barres décoratif
    /*private static void drawBarcode(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(Color.WHITE);
        int barCount = 30;
        int barWidth = width / barCount;

        for (int i = 0; i < barCount; i++) {
            if (i % 3 != 0) { // Alternance des barres pour effet visuel
                g.fillRect(x + i * barWidth, y, barWidth, height);
            }
        }

        // Ajouter un numéro de ticket
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        g.drawString("EVENT TICKET " + System.currentTimeMillis()%10000, x + 30, y + height - 10);
    }*/

    public static void main(String[] args) {
        try {
            BufferedImage ticket = createEventTicket(
                    "Concert Live Music Festival 2024", "2024-11-11", "50 €","Tunis",
                    "event.jpg", "qrcode.png");
            ImageIO.write(ticket, "png", new File("event_ticket.png"));
            System.out.println("Ticket généré avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}