package models.constants;

/**
 * Created by Mesut on 07.02.2018.
 */
public class UserConstants {

    public static final String VALID_EMAIL_ADDRESS_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public static final String VALID_PASSWORD_REGEX = "((?=.*[a-z])(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
    public static final String VALID_IP_ADDRESS_REGEX = "([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3})";
    public static final String VALID_PORT_REGEX = "([0-9]{1,5})";
}