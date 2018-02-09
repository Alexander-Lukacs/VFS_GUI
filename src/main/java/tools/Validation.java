package tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static controller.constants.AlertConstants.*;
import static tools.constants.ValidationConstants.*;

/**
 * Created by Mesut on 07.02.2018.
 */

/**
 *
 */
public class Validation {
    private static Pattern pattern;
    private static Matcher matcher;

    private static boolean ipValidation(String ip) {
        pattern = Pattern.compile(GC_VALID_IP_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(ip);
        return matcher.find();
    }

    private static boolean portValidation(String port) {
        pattern = Pattern.compile(GC_VALID_PORT_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(port);
        return matcher.find();
    }

    private static boolean emailValidation(String email) {
        pattern = Pattern.compile(GC_VALID_EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(email);
        return matcher.find();
    }

    private static boolean passwordValidation(String password) {
        pattern = Pattern.compile(GC_VALID_PASSWORD_REGEX);
        matcher = pattern.matcher(password);
        return matcher.find();
    }

    private static boolean nameValidation(String iva_name) {
        return iva_name.trim().length() >= 3;
    }

    public static boolean passwordEqualsValidation(String iva_password1, String iva_password2) {
        return iva_password1.equals(iva_password2);
    }

    public static boolean checkIfIpPortValid(String iva_ip, String iva_port) {
        if (!ipValidation(iva_ip)) {
            AlertWindows.WarningAlert(GC_WARNING_IP);
            return false;
        }
        if (!portValidation(iva_port)) {
            AlertWindows.WarningAlert(GC_WARNING_PORT);
            return false;
        }
        return true;
    }


    public static boolean checkIfPasswordEqual(String iva_password, String iva_confirmPassword) {
        if (!passwordEqualsValidation(iva_password, iva_confirmPassword)) {
            AlertWindows.WarningAlert(GC_WARNING_PASSWORD_NOT_EQUAL);
            return false;
        }
        return true;
    }

    public static boolean checkIfPasswordValid(String iva_password) {
        if (!passwordValidation(iva_password)) {
            AlertWindows.WarningAlert(GC_WARNING_PASSWORD);
            return false;
        }
        return true;
    }

    public static boolean checkIfEmailValid(String iva_email) {
        if (!emailValidation(iva_email)) {
            AlertWindows.WarningAlert(GC_WARNING_EMAIL);
            return false;
        }
        return true;
    }

    public static boolean checkIfNameValid(String iva_name) {
        if (!nameValidation(iva_name)) {
            AlertWindows.WarningAlert(GC_WARNING_USERNAME);
            return false;
        }
        return true;
    }
}
