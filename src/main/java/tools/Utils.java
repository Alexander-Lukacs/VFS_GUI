package tools;

import cache.DataCache;
import cache.SharedDirectoryCache;
import fileTree.classes.TreeSingleton;
import models.classes.RestResponse;
import models.classes.SharedDirectory;
import threads.classes.ThreadManager;
import threads.constants.FileManagerConstants;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;

import static restful.constants.HttpStatusCodes.*;

public abstract class Utils {
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

    public static int getDirectoryIdFromRelativePath(String iva_relativePath, boolean iva_isPathFromServer) {
        String[] lar_fileDirectories;

        if (iva_relativePath.startsWith("Shared") || iva_relativePath.startsWith(DirectoryNameMapper.getSharedDirectoryName())) {
            lar_fileDirectories = iva_relativePath.split("\\\\");
            if (iva_isPathFromServer) {
                return Integer.parseInt(lar_fileDirectories[1]);
            } else {
                return DirectoryNameMapper.getIdOfSharedDirectory(lar_fileDirectories[1]);
            }
        }

        if (iva_relativePath.startsWith("Public") || iva_relativePath.startsWith(DirectoryNameMapper.getPublicDirectoryName())) {
            return 0;
        }

        return -1;
    }

    public static String convertRelativeToAbsolutePath(String iva_filePath, boolean isPathFromServer) {
        int lva_directoryId = getDirectoryIdFromRelativePath(iva_filePath, isPathFromServer);
        String rva_absolutePath = getRootDirectory();
        String lva_sharedDirectoryName;

        if (lva_directoryId < 0) {
            rva_absolutePath += "\\" + iva_filePath.replaceFirst("[^\\\\]*", DirectoryNameMapper.getPrivateDirectoryName());
        }

        if (lva_directoryId == 0) {
            rva_absolutePath += "\\" + iva_filePath.replaceFirst("[^\\\\]*", DirectoryNameMapper.getPublicDirectoryName());
        }

        if (lva_directoryId > 0) {
            lva_sharedDirectoryName = DirectoryNameMapper.getRenamedSharedDirectoryName(lva_directoryId);
            lva_sharedDirectoryName = "\\\\" + lva_sharedDirectoryName;
            iva_filePath = iva_filePath.replaceFirst("\\\\[^\\\\]*", lva_sharedDirectoryName);
            rva_absolutePath += "\\" + iva_filePath;
        }

        return rva_absolutePath;
    }

    public static String buildRelativeFilePath (File iob_file) {
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
        String rva_rootDirectory = getUserBasePath();
        DataCache lob_cache = DataCache.getDataCache();

        return rva_rootDirectory + "\\" + lob_cache.get(DataCache.GC_IP_KEY) + "_" + lob_cache.get(DataCache.GC_PORT_KEY) +
                "\\" + lob_cache.get(DataCache.GC_EMAIL_KEY);
    }

    /**
     * Create a shared directory in the explorer and tree view
     *
     * @param iob_sharedDirectory the shared directory
     */
    public static void createSharedDirectory(SharedDirectory iob_sharedDirectory) {
        // Declaration block -----------------------------------------------------------------------------------------------

        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        File lob_file;
        int lva_counter = 1;
        String lva_filePath;

        // -----------------------------------------------------------------------------------------------------------------

        lva_filePath = buildPathToSharedDirectory(iob_sharedDirectory);

        lob_file = new File(lva_filePath.replace("$", ""));

        // if the directory already exists extend the dir name with (x).
        // x = autoincrement integer
        if (lob_file.exists()) {
            do {
                lob_file = new File(lva_filePath.replace("$", "(" + lva_counter + ")"));
                lva_counter++;
            } while (lob_file.exists());
        }

        lob_sharedDirectoryCache.put(iob_sharedDirectory.getId(), iob_sharedDirectory);
        DirectoryNameMapper.addNewSharedDirectory(iob_sharedDirectory.getId(), lob_file.getName());
        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(lob_file.toPath());
        ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_ADD, false, true);

    }

    /**
     * Builds the absolute path to the shared directory
     *
     * @param iob_sharedDirectory the shared directory
     * @return the absolute path
     */
    @SuppressWarnings("WeakerAccess")
    public static String buildPathToSharedDirectory(SharedDirectory iob_sharedDirectory) {
        return Utils.getRootDirectory() +
                "\\" + DirectoryNameMapper.getSharedDirectoryName() + "\\" + iob_sharedDirectory.getDirectoryName() + "$";
    }
}
