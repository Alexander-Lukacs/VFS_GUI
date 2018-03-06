package tools;

import models.classes.RestResponse;

import static restful.constants.HttpStatusCodes.*;

public class Utils {
    private static final String GC_FILE_BASE_PATH = "C:\\Users\\$\\Documents\\FileSystem";

    public static String getUserBasePath() {
        return GC_FILE_BASE_PATH.replace("$", System.getProperty("user.name"));
    }

    public static void printResponseMessage(RestResponse restResponse) {
        switch (restResponse.getHttpStatus()) {
            case GC_HTTP_OK:
                new AlertWindows().createInformationAlert(restResponse.getResponseMessage());
                break;

            case GC_HTTP_BAD_REQUEST:
                new AlertWindows().createErrorAlert(restResponse.getResponseMessage());
                break;

            case GC_HTTP_CONFLICT:
                new AlertWindows().createErrorAlert(restResponse.getResponseMessage());
                break;

            case GC_HTTP_NO_PERMISSION:
                new AlertWindows().createErrorAlert(restResponse.getResponseMessage());
                break;
        }
    }
}
