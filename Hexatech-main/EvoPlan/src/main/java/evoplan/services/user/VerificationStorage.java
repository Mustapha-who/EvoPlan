package evoplan.services.user;

import java.util.HashMap;
import java.util.Map;

public class VerificationStorage {
    private static final Map<String, String> verificationCodes = new HashMap<>();

    public static void saveVerificationCode(String email, String code) {
        verificationCodes.put(email, code);
    }

    public static boolean verifyCode(String email, String inputCode) {
        return verificationCodes.containsKey(email) && verificationCodes.get(email).equals(inputCode);
    }
}
