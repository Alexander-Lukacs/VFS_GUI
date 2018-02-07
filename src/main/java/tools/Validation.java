package tools;

import java.util.regex.*;

import static tools.constants.ValidationConstants.*;

/**
 * Created by Mesut on 07.02.2018.
 */
public class Validation {
    private static Pattern pattern;
    private static Matcher matcher;

    public static boolean ipValidation(String ip){
        pattern = Pattern.compile(VALID_IP_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(ip);
        return matcher.find();
    }

    public static boolean portValidation(String port){
        pattern = Pattern.compile(VALID_PORT_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(port);
        return matcher.find();
    }

    public static boolean emailValidation(String email){
        pattern = Pattern.compile(VALID_EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(email);
        return matcher.find();
    }

    public static boolean passwordValidation(String password){
        pattern = Pattern.compile(VALID_PASSWORD_REGEX);
        matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean nameValidation(String iva_name) {
        return iva_name.trim().length() >= 3;
    }

    public static boolean passwordEqualsValidation(String iva_password1, String iva_password2) {
        return iva_password1.equals(iva_password2);
    }
}
