package evoplan.controllers.EventPlannerHomePage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

public class OpenRouterAPI {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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"; // My API key

    public static CompletableFuture<String> sendRequest(String model, String prompt) {
        // Create the request body
        String requestBody = String.format("""
        {
            "model": "%s",
            "messages": [
                {
                    "role": "user",
                    "content": "%s"
                }
            ]
        }
        """, model, prompt);

        // Create the HTTP client and request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        // Send the request asynchronously and return the response
        return client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        // Extract the response body as a String
                        String responseBody = response.body();

                        // Parse the JSON response
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readTree(responseBody);

                        // Check if the response contains an error
                        if (rootNode.has("error")) {
                            return "API Error: " + rootNode.path("error").path("message").asText();
                        }

                        // Check if the `choices` field exists and is not empty
                        JsonNode choicesNode = rootNode.path("choices");
                        if (choicesNode.isMissingNode() || !choicesNode.isArray() || choicesNode.isEmpty()) {
                            return "No choices available in the response";
                        }

                        // Extract the generated text
                        return choicesNode.get(0).path("message").path("content").asText();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Error parsing response";
                    }
                });
    }}
