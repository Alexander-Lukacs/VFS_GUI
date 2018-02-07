package tools;

public class Utils {
    private static final String GC_FILE_BASE_PATH= "C:\\Users\\$\\Documents\\FileSystem";

    public static String getUserBasePath() {
        return GC_FILE_BASE_PATH.replace("$", System.getProperty("user.name"));
    }
}
