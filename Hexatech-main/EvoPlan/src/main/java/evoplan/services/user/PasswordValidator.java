package evoplan.services.user;


/**makes sure the pass has : min 1 uppercase
 * min 1 lowercase
 * min 1 special character
 * min 1 number
 * min length 8
**/
public class PasswordValidator {
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if ("@#$%^&+=!".contains(String.valueOf(ch))) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
