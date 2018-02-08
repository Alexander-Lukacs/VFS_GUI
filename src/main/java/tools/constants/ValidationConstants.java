package tools.constants;

/**
 * Created by Mesut on 07.02.2018.
 */
public class ValidationConstants {

    public static final String GC_VALID_EMAIL_ADDRESS_REGEX = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public static final String GC_VALID_PASSWORD_REGEX = "((?=.*[a-z])(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
    public static final String GC_VALID_IP_ADDRESS_REGEX = "([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3})";
    public static final String GC_VALID_PORT_REGEX = "([0-9]{1,5})";
}
