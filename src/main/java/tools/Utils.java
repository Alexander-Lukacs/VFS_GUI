package tools;

import client.RestResponse;

import static client.constants.HttpStatusCodes.GC_HTTP_BAD_REQUEST;
import static client.constants.HttpStatusCodes.GC_HTTP_CONFLICT;
import static client.constants.HttpStatusCodes.GC_HTTP_OK;

public class Utils {
    private static final String GC_FILE_BASE_PATH = "C:\\Users\\$\\Documents\\FileSystem";

    public static String getUserBasePath() {
        return GC_FILE_BASE_PATH.replace("$", System.getProperty("user.name"));
    }

    public static void printResponseMessage(RestResponse restResponse) {
        switch (restResponse.getHttpStatus()) {
            case GC_HTTP_OK:
                AlertWindows.createInformationAlert(restResponse.getResponseMessage());
                break;

            case GC_HTTP_BAD_REQUEST:
                AlertWindows.createErrorAlert(restResponse.getResponseMessage());
                break;

            case GC_HTTP_CONFLICT:
                AlertWindows.createErrorAlert(restResponse.getResponseMessage());
                break;
        }
    }
}
