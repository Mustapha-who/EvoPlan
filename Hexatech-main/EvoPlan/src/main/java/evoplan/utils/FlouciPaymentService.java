package evoplan.utils;

import okhttp3.*;
import org.json.JSONObject;

public class FlouciPaymentService {
    private static final String APP_TOKEN = "12f62e47-1da2-4cb8-8f52-ad5463f36450";
    private static final String APP_SECRET = "32051781-92f2-4f7a-a6b0-a73e2782d806";
    private static final String PAYMENT_URL = "https://developers.flouci.com/api/generate_payment";
    private static final String VERIFY_URL = "https://developers.flouci.com/api/verify_payment/";

    public static String getAppToken() {
        return APP_TOKEN;
    }

    public static String getAppSecret() {
        return APP_SECRET;
    }

    public static String generatePaymentLink(double amount, String successLink, String failLink, String trackingId) {
        try {
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            json.put("app_token", APP_TOKEN);
            json.put("app_secret", APP_SECRET);
            json.put("amount", amount * 1000); // Convertir en millimes
            json.put("accept_card", true);
            json.put("session_timeout_secs", 1200);
            json.put("success_link", successLink);
            json.put("fail_link", failLink);
            json.put("developer_tracking_id", trackingId);

            System.out.println("📤 Envoi de la requête à Flouci : " + json.toString(2));

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(PAYMENT_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String responseBody = response.body().string();
                    System.out.println("📩 Réponse de Flouci : " + responseBody);

                    JSONObject responseJson = new JSONObject(responseBody);
                    return responseJson.getJSONObject("result").getString("link");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String checkPaymentStatus(String paymentId) {
        try {
            OkHttpClient client = new OkHttpClient();
            String apiUrl = VERIFY_URL + paymentId;

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("apppublic", APP_TOKEN)
                    .addHeader("appsecret", APP_SECRET)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.body() != null) {
                    String responseBody = response.body().string();
                    System.out.println("📊 Réponse de Flouci (Statut Paiement) : " + responseBody);

                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.has("result") && jsonResponse.get("result") instanceof JSONObject) {
                        JSONObject result = jsonResponse.getJSONObject("result");

                        // Vérifier la structure complète de la réponse
                        System.out.println("📝 JSON complet reçu : " + result.toString(2));

                        if (result.has("status")) {
                            String status = result.getString("status");
                            System.out.println("🔍 Statut détecté : " + status);

                            if ("SUCCESS".equalsIgnoreCase(status)) {
                                return "success";
                            } else if ("FAILURE".equalsIgnoreCase(status)) {
                                return "fail";
                            } else {
                                return "pending"; // Statut intermédiaire (ex: "pending")
                            }
                        } else {
                            System.err.println("⚠️ Avertissement : Pas de statut trouvé dans la réponse !");
                        }
                    } else {
                        System.err.println("⚠️ Avertissement : Mauvaise structure de réponse JSON !");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "pending"; // Valeur par défaut si erreur
    }

}