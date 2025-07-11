package evoplan.controllers.Ressource;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;

public class MapController {

    @FXML
    private WebView webView;

    @FXML
    public void initialize() {
        // Activer la journalisation des erreurs
        webView.getEngine().setOnError(event -> {
            System.err.println("Erreur WebView: " + event.getMessage());
        });

        webView.getEngine().setOnAlert(event -> {
            System.out.println("Alerte WebView: " + event.getData());
        });

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                System.err.println("Erreur de chargement: " + webView.getEngine().getLoadWorker().getException());
            }
        });

        // Charger une carte par défaut (Tunisie)
        setPosition(34.0, 9.0, 6); // Centrer sur la Tunisie, zoom 6
    }

    // Méthode pour définir la position et le zoom de la carte
    public void setPosition(double lat, double lon, int zoom) {
        // Générer le contenu HTML pour la carte avec les marqueurs
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Carte OpenStreetMap</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                "    <style>\n" +
                "        #map { height: 500px; width: 100%; border: 1px solid red; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                "    <script>\n" +
                "        const map = L.map('map').setView([" + lat + ", " + lon + "], " + zoom + ");\n" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "            attribution: '© OpenStreetMap contributors'\n" +
                "        }).addTo(map);\n" +
                "        const locations = [\n" +
                "            { name: 'Tunis', coords: [36.8065, 10.1815] },\n" +
                "            { name: 'Sfax', coords: [34.7406, 10.7603] },\n" +
                "            { name: 'Sousse', coords: [35.8254, 10.6360] },\n" +
                "            { name: 'Djerba', coords: [33.8078, 10.8451] },\n" +
                "            { name: 'Tozeur', coords: [33.9197, 8.1338] },\n" +
                "            { name: 'Kairouan', coords: [35.6712, 10.1007] },\n" +
                "            { name: 'Monastir', coords: [35.7643, 10.8113] },\n" +
                "        ];\n" +
                "        locations.forEach(location => {\n" +
                "            console.log('Ajout du marqueur : ' + location.name + ' à ' + location.coords);\n" +
                "            L.marker(location.coords)\n" +
                "                .addTo(map)\n" +
                "                .bindPopup(location.name);\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        // Charger le contenu HTML dans la WebView
        webView.getEngine().loadContent(htmlContent);
    }
}