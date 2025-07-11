package evoplan.services.user;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;


public class VerificationCodeGenerator {
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    private static final Map<String, String> verificationCodes = new HashMap<>();

    public static void saveVerificationCode(String email, String code) {
        verificationCodes.put(email, code);
    }

    public static boolean verifyCode(String email, String inputCode) {
        return verificationCodes.containsKey(email) && verificationCodes.get(email).equals(inputCode);
    }
}
