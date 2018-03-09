package tools;

import cache.DataCache;
import models.classes.RestResponse;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;

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

    public static int getDirectoryIdFromRelativePath(String iva_relativePath) {
        String lva_directoryName;

        if (iva_relativePath.startsWith("Shared")) {
            lva_directoryName = iva_relativePath.replaceFirst(".*\\\\", "");
            return DirectoryNameMapper.getIdOfSharedDirectory(lva_directoryName);
        }

        if (iva_relativePath.startsWith("Public")) {
            return 0;
        }

        return -1;
    }

    public static String convertRelativeToAbsolutePath(String iva_filePath) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_filePath);
        String rva_absolutePath = getRootDirectory();
        String lva_sharedDirectoryName;

        if (lva_directoryId <= 0) {
            return rva_absolutePath + iva_filePath;
        }

        lva_sharedDirectoryName = DirectoryNameMapper.getRenamedSharedDirectoryName(lva_directoryId);

        return rva_absolutePath.replaceFirst("^[^\\\\]*", lva_sharedDirectoryName);
    }

    public static String buildRelativeFilePath(File iob_file) {
        String lva_basePath = getRootDirectory();
        String rva_relativePath = "";
        try {
            rva_relativePath = iob_file.getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        lva_basePath += "\\";
        rva_relativePath = rva_relativePath.replace(lva_basePath, "");
        return rva_relativePath;
    }

    /**
     * @return a path like this C:\Users\{USERNAME}\Documents\FileSystem\{IP_PORT}\{EMAIL}\
     */
    @SuppressWarnings("WeakerAccess")
    public static String getRootDirectory() {
        String rva_rootDirectry = getUserBasePath();
        DataCache lob_cache = DataCache.getDataCache();

        return rva_rootDirectry + "\\" + lob_cache.get(DataCache.GC_IP_KEY) + "_" + lob_cache.get(DataCache.GC_PORT_KEY) +
                "\\" + lob_cache.get(DataCache.GC_EMAIL_KEY);
    }
}
