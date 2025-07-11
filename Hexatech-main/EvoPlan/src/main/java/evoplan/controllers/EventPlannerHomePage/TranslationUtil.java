package evoplan.controllers.EventPlannerHomePage;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

public class TranslationUtil {

    private static final String MYMEMORY_API_URL = "https://api.mymemory.translated.net/get";

    /**
     * Translates text using MyMemory Translation API.
     *
     * @param text          The text to translate.
     * @param sourceLang    The source language code (e.g., "en" for English).
     * @param targetLang    The target language code (e.g., "es" for Spanish).
     * @return The translated text.
     */
    public static String translate(String text, String sourceLang, String targetLang) {
        try {
            // Encode the text for the URL
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());

            // Build the API request URL with explicit langpair parameter
            String url = MYMEMORY_API_URL + "?q=" + encodedText + "&langpair=" + sourceLang + "%7C" + targetLang;

            // Create an HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Create an HTTP request with appropriate headers
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept-Charset", "UTF-8")
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            // Parse the response JSON to extract the translated text
            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Use proper JSON parsing instead of string splitting
                try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
                    JsonObject jsonObject = jsonReader.readObject();
                    JsonObject responseData = jsonObject.getJsonObject("responseData");
                    String translatedText = responseData.getString("translatedText");
                    return translatedText;
                }
            } else {
                throw new RuntimeException("Translation failed: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during translation: " + e.getMessage(), e);
        }
    }
}