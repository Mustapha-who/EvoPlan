package evoplan.services.Partner;

import evoplan.entities.Contract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PDFExporter {

    public static void exportToPDF(Contract contract, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = PDRectangle.A4.getHeight() - margin;
                float tableWidth = PDRectangle.A4.getWidth() - 2 * margin;
                float rowHeight = 25;

                // Draw page border
                contentStream.setLineWidth(1.5f);
                contentStream.addRect(margin - 10, margin - 10, PDRectangle.A4.getWidth() - 2 * margin + 20, PDRectangle.A4.getHeight() - 2 * margin + 20);
                contentStream.stroke();

                // Load logo dynamically (from resources)
                InputStream logoStream = PDFExporter.class.getResourceAsStream("/Img/hexatech.png");
                if (logoStream != null) {
                    PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoStream.readAllBytes(), "logo.png");
                    contentStream.drawImage(logo, margin, yStart - 50, 100, 50); // Adjusted x position to margin (left side)
                }

                yStart -= 70; // Move down after logo

                // Exportation Date (Top Left)
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText( LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                contentStream.endText();
                yStart -= 30; // Move down after date

                // Title (Centered)
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                String title = "Contract ";
                float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * 24;
                float titleX = (PDRectangle.A4.getWidth() - titleWidth) / 2;
                contentStream.beginText();
                contentStream.newLineAtOffset(titleX, yStart);
                contentStream.showText(title);
                contentStream.endText();
                yStart -= 40;

                // Table Headers
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                float yPosition = yStart;
                float xPosition = margin;
                String[][] data = {
                        {"Start Date", contract.getDate_debut()},
                        {"End Date", contract.getDate_fin()},
                        {"Terms", contract.getTerms()},
                        {"Status", contract.getStatus()},
                };

                float[] columnWidths = {tableWidth * 0.3f, tableWidth * 0.7f};

                // Draw header background
                contentStream.setNonStrokingColor(200, 200, 200);
                contentStream.addRect(xPosition, yPosition - 15, tableWidth, rowHeight);
                contentStream.fill();
                contentStream.setNonStrokingColor(0, 0, 0);

                // Print headers
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition + 5, yPosition - 10);
                contentStream.showText("");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition + columnWidths[0] + 5, yPosition - 10);
                contentStream.showText("");
                contentStream.endText();

                yPosition -= rowHeight;

                // Print data rows
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                for (int i = 0; i < data.length; i++) {
                    xPosition = margin;

                    if (i % 2 == 0) {
                        contentStream.setNonStrokingColor(240, 240, 240);
                    } else {
                        contentStream.setNonStrokingColor(255, 255, 255);
                    }
                    contentStream.addRect(xPosition, yPosition - 15, tableWidth, rowHeight);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(0, 0, 0);

                    // Print field names
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition + 5, yPosition - 10);
                    contentStream.showText(data[i][0]);
                    contentStream.endText();

                    // Print values
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition + columnWidths[0] + 5, yPosition - 10);
                    contentStream.showText(data[i][1]);
                    contentStream.endText();

                    yPosition -= rowHeight;
                }

                // Footer
                contentStream.setFont(PDType1Font.HELVETICA, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, margin - 8);
                contentStream.showText(" Evoplan Your Best EventPlanner system");
                contentStream.endText();
            }

            document.save(new File(filePath));
        } catch (IOException e) {
            throw new IOException("Failed to export PDF: " + e.getMessage(), e);
        }
    }
}