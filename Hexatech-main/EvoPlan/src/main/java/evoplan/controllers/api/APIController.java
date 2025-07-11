package evoplan.controllers.api;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;

public class APIController implements Initializable {
    private static final String API_KEY = "D+op30gmMeJ0geFc31syIg==Lq1jiRpNh69NYT9w";

    @FXML private Button getQuoteButton;
    @FXML private TextArea quoteDisplay;

    @FXML private ComboBox<String> commoditySelector;
    @FXML private Button getPriceButton;
    @FXML private Label priceDisplay;

    @FXML private TextField amountField;
    @FXML private ComboBox<String> fromCurrencySelector;
    @FXML private ComboBox<String> toCurrencySelector;
    @FXML private Button convertButton;
    @FXML private Label conversionResultDisplay;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupQuotesAPI();
        setupCommodityAPI();
        setupCurrencyAPI();
    }

    private void setupQuotesAPI() {
        getQuoteButton.setOnAction(e -> {
            getQuoteButton.setDisable(true);
            quoteDisplay.setText("Loading...");
            getRandomQuote();
        });
    }

    private void setupCommodityAPI() {
        // Only non-premium commodities
        String[] commodities = {
                "gold",      // Gold Futures
                "platinum",  // Platinum
                "oat",      // Oat Futures
                "aluminum", // Aluminum Futures
                "soybean_meal", // Soybean Meal Futures
                "lumber",   // Lumber Futures
                "micro_gold", // Micro Gold Futures
                "feeder_cattle", // Feeder Cattle Futures
                "rough_rice",    // Rough Rice Futures
                "palladium",     // Palladium
                "lean_hogs"      // Lean Hogs Futures
        };
        commoditySelector.getItems().addAll(commodities);
        getPriceButton.setOnAction(e -> {
            getPriceButton.setDisable(true);
            priceDisplay.setText("Loading...");
            getCommodityPrice();
        });
    }

    private void setupCurrencyAPI() {
        String[] currencies = {"GBP", "AUD"}; // Only GBP and AUD as requested
        fromCurrencySelector.getItems().addAll(currencies);
        toCurrencySelector.getItems().addAll(currencies);
        convertButton.setOnAction(e -> {
            convertButton.setDisable(true);
            conversionResultDisplay.setText("Converting...");
            convertCurrency();
        });
    }

    private void getRandomQuote() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.api-ninjas.com/v1/quotes"))
                    .header("X-Api-Key", API_KEY)
                    .header("accept", "application/json")
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            throw new RuntimeException("API returned status code: " + response.statusCode());
                        }
                        System.out.println("Quote API Response: " + response.body());
                        return response.body();
                    })
                    .thenApply(response -> {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                JSONObject quote = jsonArray.getJSONObject(0);
                                return String.format("\"%s\"\n- %s",
                                        quote.getString("quote"),
                                        quote.getString("author"));
                            }
                            return "No quote available";
                        } catch (Exception e) {
                            System.out.println("Error parsing quote response: " + e.getMessage());
                            throw e;
                        }
                    })
                    .thenAccept(quote -> {
                        Platform.runLater(() -> {
                            quoteDisplay.setText(quote);
                            getQuoteButton.setDisable(false);
                        });
                    })
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            showError("Error fetching quote: " + e.getMessage());
                            getQuoteButton.setDisable(false);
                            quoteDisplay.setText("Error fetching quote");
                        });
                        return null;
                    });
        } catch (Exception e) {
            Platform.runLater(() -> {
                showError("Error: " + e.getMessage());
                getQuoteButton.setDisable(false);
                quoteDisplay.setText("Error fetching quote");
            });
        }
    }

    private void getCommodityPrice() {
        String commodity = commoditySelector.getValue();
        if (commodity == null) {
            showError("Please select a commodity");
            getPriceButton.setDisable(false);
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.api-ninjas.com/v1/commodities?symbol=" + commodity))
                    .header("X-Api-Key", API_KEY)
                    .header("accept", "application/json")
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            throw new RuntimeException("API returned status code: " + response.statusCode());
                        }
                        System.out.println("Commodity API Response: " + response.body());
                        return response.body();
                    })
                    .thenApply(response -> {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                JSONObject item = jsonArray.getJSONObject(0);
                                return String.format("%s Price: $%.2f",
                                        item.getString("name"),
                                        item.getDouble("price"));
                            }
                            return "Price not available for " + commodity;
                        } catch (Exception e) {
                            System.out.println("Error parsing commodity response: " + e.getMessage());
                            throw e;
                        }
                    })
                    .thenAccept(price -> {
                        Platform.runLater(() -> {
                            priceDisplay.setText(price);
                            getPriceButton.setDisable(false);
                        });
                    })
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            showError("Error fetching price: " + e.getMessage());
                            getPriceButton.setDisable(false);
                            priceDisplay.setText("Error fetching price");
                        });
                        return null;
                    });
        } catch (Exception e) {
            Platform.runLater(() -> {
                showError("Error: " + e.getMessage());
                getPriceButton.setDisable(false);
                priceDisplay.setText("Error fetching price");
            });
        }
    }

    private void convertCurrency() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String fromCurrency = fromCurrencySelector.getValue();
            String toCurrency = toCurrencySelector.getValue();

            if (fromCurrency == null || toCurrency == null) {
                showError("Please select both currencies");
                convertButton.setDisable(false);
                return;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(
                            "https://api.api-ninjas.com/v1/exchangerate?pair=%s_%s",
                            fromCurrency, toCurrency)))
                    .header("X-Api-Key", API_KEY)
                    .header("accept", "application/json")
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            throw new RuntimeException("API returned status code: " + response.statusCode());
                        }
                        System.out.println("Exchange Rate API Response: " + response.body());
                        return response.body();
                    })
                    .thenApply(response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            double rate = json.getDouble("exchange_rate");
                            double result = amount * rate;
                            return String.format("%.2f %s = %.2f %s\nRate: %.4f",
                                    amount, fromCurrency, result, toCurrency, rate);
                        } catch (Exception e) {
                            System.out.println("Error parsing exchange rate response: " + e.getMessage());
                            throw e;
                        }
                    })
                    .thenAccept(result -> {
                        Platform.runLater(() -> {
                            conversionResultDisplay.setText(result);
                            convertButton.setDisable(false);
                        });
                    })
                    .exceptionally(e -> {
                        Platform.runLater(() -> {
                            showError("Error converting currency: " + e.getMessage());
                            convertButton.setDisable(false);
                            conversionResultDisplay.setText("Error converting currency");
                        });
                        return null;
                    });
        } catch (NumberFormatException e) {
            showError("Please enter a valid amount");
            convertButton.setDisable(false);
        } catch (Exception e) {
            Platform.runLater(() -> {
                showError("Error: " + e.getMessage());
                convertButton.setDisable(false);
                conversionResultDisplay.setText("Error converting currency");
            });
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}