package tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.constants.ValidationConstants.*;

/**
 * Created by Mesut on 07.02.2018.
 */

public class Validation {
    private static Pattern pattern;
    private static Matcher matcher;

    public static boolean isIpValid(String ip) {
        pattern = Pattern.compile(GC_VALID_IP_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(ip);
        return matcher.find() || ip.equals("localhost");
    }

    public static boolean isPortValid(String port) {
        pattern = Pattern.compile(GC_VALID_PORT_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(port);
        return matcher.find();
    }

    public static boolean isEmailValid(String email) {
        pattern = Pattern.compile(GC_VALID_EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(email);
        return matcher.find();
    }

    public static boolean isPasswordInvalid(String password) {
        pattern = Pattern.compile(GC_VALID_PASSWORD_REGEX);
        matcher = pattern.matcher(password);
        return !matcher.find();
    }

    public static boolean nameValidation(String iva_name) {
        return iva_name.trim().length() >= 3;
    }

    public static boolean passwordEqualsValidation(String iva_password1, String iva_password2) {
        return iva_password1.equals(iva_password2);
    }
}
