package evoplan.services.user;
import okhttp3.*;

import java.io.IOException;

public class phoneService {

    public void SendPasswordRestSMS(String verificationCode) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"messages\":[{\"destinations\":[{\"to\":\"21652136727\"}],\"from\":\"447491163443\",\"text\":\"your password reset code is:" + verificationCode +"\"}]}");
        Request request = new Request.Builder()
                .url("https://g922y6.api.infobip.com/sms/2/text/advanced")
                .method("POST", body)
                .addHeader("Authorization", "App 9eed35497229628ad4a88c4f7585a6d7-b2024bd2-0069-4de5-a4d6-557ca4290b77")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        Response response = client.newCall(request).execute();


    }
    // code segment  to be added into the controller that requires the phone service .//        try {
//            phoneService phoneService = new phoneService();
//            phoneService.SendSMS();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
}
