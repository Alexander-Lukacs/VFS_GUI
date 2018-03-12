package threads.classes;

import builder.RestClientBuilder;
import fileTree.classes.TreeSingleton;
import models.classes.SharedDirectory;
import restful.clients.SharedDirectoryRestClient;
import threads.constants.FileManagerConstants;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.List;

public class NotifyServerThread extends Thread {
    private static final int GC_ACCEPT_TIMEOUT_MILLIS = 500;
    private static final int GC_NOTIFY_SERVER_PORT = 32001;

    private static final String GC_MESSAGE_MOVE = "move";
    private static final String GC_MESSAGE_ADD = "add";
    private static final String GC_MESSAGE_DELETE = "delete";
    private static final String GC_MESSAGE_RENAME = "rename";
    private static final String GC_MESSAGE_DELETE_DIR = "deleteDir";

    private static final String GC_MESSAGE_SHARED_DIR_ADD = "addSharedDir";
    private static final String GC_MESSAGE_SHARED_DIR_DELETE = "deleteSharedDir";

    @Override
    public void run() {
        ServerSocket lob_notifyServer;
        Socket lob_client = null;
        BufferedReader lob_inputStream = null;
        String lva_message;
        String[] lar_messageArray;

        try {

            lob_notifyServer = new ServerSocket(GC_NOTIFY_SERVER_PORT);
            lob_notifyServer.setSoTimeout(GC_ACCEPT_TIMEOUT_MILLIS);

            while (!isInterrupted()) {
                try {
                    lob_client = lob_notifyServer.accept();
                    lob_inputStream = new BufferedReader(new InputStreamReader(lob_client.getInputStream()));

                    lva_message = lob_inputStream.readLine();
                    lar_messageArray = lva_message.split("\\|");

                    switch (lar_messageArray[0]) {
                        case GC_MESSAGE_ADD:
                            System.out.println("Add command read!");
                            addFile(lar_messageArray);
                            break;

                        case GC_MESSAGE_MOVE:
                            System.out.println("Move command read!");
                            moveFile(lar_messageArray);
                            break;

                        case GC_MESSAGE_DELETE:
                            System.out.println("Delete command read!");
                            deleteFile(lar_messageArray);
                            break;

                        case GC_MESSAGE_RENAME:
                            System.out.println("Rename command read!");
                            renameFile(lar_messageArray);
                            break;

                        case GC_MESSAGE_DELETE_DIR:
                            System.out.println("Delete Dir command read!");
                            deleteDir(lar_messageArray);
                            break;

                        case GC_MESSAGE_SHARED_DIR_ADD:
                            System.out.println("Add shared dir command read!");
                            addSharedDirectory(lar_messageArray);
                            break;

                        case GC_MESSAGE_SHARED_DIR_DELETE:
                            System.out.println("Delete shared dir command read!");
                            deleteSharedDirectory(lar_messageArray);
                            break;
                    }

                } catch (InterruptedIOException ignore) {
                }

                if (lob_client != null) {
                    lob_client.close();
                }

                if (lob_inputStream != null) {
                    lob_inputStream.close();
                }
            }

            lob_notifyServer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Move file within the explore and tree view
     *
     * @param iar_messageArray at position 0: operation
     *                         at position 1: actual file location
     *                         at position 2: new file location
     */
    private void moveFile(String[] iar_messageArray) {
        File lob_file;
        File lob_newFileLocation;
        String lva_actualFileLocation;
        String lva_newFileLocation;

        lva_actualFileLocation = Utils.convertRelativeToAbsolutePath(iar_messageArray[1], true);
        lva_newFileLocation = Utils.convertRelativeToAbsolutePath(iar_messageArray[2], true);

        lob_file = new File(lva_actualFileLocation);
        lob_newFileLocation = new File(lva_newFileLocation);

        ThreadManager.addCommandToFileManager(
                lob_file, FileManagerConstants.GC_MOVE, false,
                lob_newFileLocation, false);
    }

    /**
     * Add file to explorer and tree view
     *
     * @param iar_messageArray at position 0: operation
     *                         at position 1: relative file path
     */
    private void addFile(String[] iar_messageArray) {
        String lva_actualFilePath;
        String lva_relativePath;
        File lob_newFile;

        lva_actualFilePath = Utils.convertRelativeToAbsolutePath(iar_messageArray[1], true);
        lob_newFile = new File(lva_actualFilePath);
        lva_relativePath = Utils.buildRelativeFilePath(lob_newFile);

        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(lob_newFile.toPath());
        ThreadManager.addCommandToFileManager(
                null, FileManagerConstants.GC_DOWNLOAD_FROM_SERVER, true,
                lva_relativePath);
    }

    /**
     * Delete file from explorer and tree view
     *
     * @param iar_messageArray at position 0: operation
     *                         at position 1: relative file path
     */
    private void deleteFile(String[] iar_messageArray) {
        File lob_file;
        String lva_actualFilePath;

        lva_actualFilePath = Utils.convertRelativeToAbsolutePath(iar_messageArray[1], true);
        lob_file = new File(lva_actualFilePath);

        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(lob_file.toPath());
        ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_DELETE, false);
    }

    /**
     * Rename file within the explorer and tree view
     *
     * @param iar_messageArray at position 0: operation
     *                         at position 1: relative file path
     *                         at position 2: new file name
     */
    private void renameFile(String[] iar_messageArray) {
        File lob_file;
        String lva_actualFilePath;
        String lva_newFileName;

        lva_actualFilePath = Utils.convertRelativeToAbsolutePath(iar_messageArray[1], true);
        lva_newFileName = iar_messageArray[2];

        lob_file = new File(lva_actualFilePath);

        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putRenamed(lob_file.toPath());
        ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_RENAME, false,
                lva_newFileName, true);
    }

    /**
     * Delete only the directory and move all contained files to the parent
     *
     * @param iar_messageArray at position 0: operation
     *                         at position 1: relative file path
     */
    private void deleteDir(String[] iar_messageArray) {
        File lob_file;
        String lva_actualFilePath;
        String lva_relativeFilePath;

        lva_relativeFilePath = iar_messageArray[1];
        lva_actualFilePath = Utils.convertRelativeToAbsolutePath(lva_relativeFilePath, true);

        lob_file = new File(lva_actualFilePath);

        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(lob_file.toPath());
        ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_DELETE_DIR_ONLY, false);
    }

    /**
     * Delete the shared directory in explorer and tree view
     * @param iar_messageArray at position 0: operation
     *                         at position 1: relative file path
     *                         at position 2: shared directory id
     */
    private void deleteSharedDirectory(String[] iar_messageArray) {
        deleteFile(iar_messageArray);
    }

    /**
     * Add new shared directory to explorer and tree view
     * @param iar_messageArray at position 0: operation
     *                         at position 1: relative file path
     *                         at position 2: shared directory id
     */
    private void addSharedDirectory(String[] iar_messageArray) {
        SharedDirectoryRestClient lob_restClient;
        List<SharedDirectory> lli_sharedDirectories;
        String sharedDirectoryId;

        sharedDirectoryId = iar_messageArray[2];

        lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();
        lli_sharedDirectories = lob_restClient.getAllSharedDirectoriesOfUser();

        for (SharedDirectory lob_tmpSharedDirectory : lli_sharedDirectories) {
            if (lob_tmpSharedDirectory.getId() == Integer.parseInt(sharedDirectoryId)) {
                try {
                    DirectoryNameMapper.getRenamedSharedDirectoryName(Integer.parseInt(sharedDirectoryId));
                } catch (IllegalArgumentException ex) {
                    Utils.createSharedDirectory(lob_tmpSharedDirectory);
                }
            }
        }
    }
}