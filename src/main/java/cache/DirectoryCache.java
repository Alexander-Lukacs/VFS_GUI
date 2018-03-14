package cache;

import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.nio.file.Files;

public class DirectoryCache {
    private static DirectoryCache gob_directoryCache;
    private static File gob_rootDirectory;
    private static File gob_userDirectory;
    private static File gob_publicDirectory;
    private static File gob_privateDirectory;
    private static File gob_sharedDirectory;
    private static File gob_serverDirectory;

    public static DirectoryCache getDirectoryCache() {
        if (gob_directoryCache == null) {
            gob_directoryCache = new DirectoryCache();
        }

        return gob_directoryCache;
    }

    public File getRootDirectory() {
        return gob_rootDirectory;
    }

    public File getUserDirectory() {
        return gob_userDirectory;
    }

    public File getPublicDirectory() {
        return gob_publicDirectory;
    }

    public File getPrivateDirectory() {
        return gob_privateDirectory;
    }

    public File getSharedDirectory() {
        return gob_sharedDirectory;
    }

    public File getServerDirectory() {
        return gob_serverDirectory;
    }

    public void setPublicDirectoryName(String iva_newName) {
        String lva_newFilePath = gob_publicDirectory.getAbsolutePath();
        lva_newFilePath = lva_newFilePath.replaceFirst("[^\\\\]*$", iva_newName);
        gob_publicDirectory = new File(lva_newFilePath);
    }

    public void setPrivateDirectoryName(String iva_newName) {
        String lva_newFilePath = gob_privateDirectory.getAbsolutePath();
        lva_newFilePath = lva_newFilePath.replaceFirst("[^\\\\]*$", iva_newName);
        gob_privateDirectory = new File(lva_newFilePath);
    }

    public void setSharedDirectoryName(String iva_newName) {
        String lva_newFilePath = gob_sharedDirectory.getAbsolutePath();
        lva_newFilePath = lva_newFilePath.replaceFirst("[^\\\\]*$", iva_newName);
        gob_sharedDirectory = new File(lva_newFilePath);
    }

    private DirectoryCache() {
        String lva_ip = DataCache.getDataCache().get(DataCache.GC_IP_KEY);
        String lva_port = DataCache.getDataCache().get(DataCache.GC_PORT_KEY);

        gob_rootDirectory = new File(Utils.getUserBasePath());
        gob_serverDirectory = new File(Utils.getUserBasePath() + "\\" + lva_ip + "_" + lva_port);
        gob_userDirectory = new File(gob_serverDirectory.getAbsolutePath() + "\\" + DataCache.getDataCache().get(DataCache.GC_EMAIL_KEY));
        File lob_configDirectory = new File(gob_userDirectory + "\\config");

        try {
            TreeTool.createDirectory(lob_configDirectory);
            Files.setAttribute(lob_configDirectory.toPath(), "dos:hidden", true);
        } catch (Exception ignore) {

        }

        //create the root directory if it does not exist
        TreeTool.createDirectory(gob_rootDirectory);

        //create the server directory if it does not exist
        TreeTool.createDirectory(gob_serverDirectory);

        //create the user directory if it does not exist
        TreeTool.createDirectory(gob_rootDirectory);

        gob_publicDirectory = new File(gob_userDirectory.getAbsolutePath() + "\\" + DirectoryNameMapper.getPublicDirectoryName());
        gob_privateDirectory = new File(gob_userDirectory.getAbsolutePath() + "\\" + DirectoryNameMapper.getPrivateDirectoryName());
        gob_sharedDirectory = new File(gob_userDirectory.getAbsolutePath() + "\\" + DirectoryNameMapper.getSharedDirectoryName());

        //create the public directory
        TreeTool.createDirectory(gob_publicDirectory);

        //create the private directory
        TreeTool.createDirectory(gob_privateDirectory);

        //create the shared directory
        TreeTool.createDirectory(gob_sharedDirectory);
    }
}
