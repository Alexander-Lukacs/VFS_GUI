package threads.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import cache.SharedDirectoryCache;
import fileTree.interfaces.Tree;
import fileTree.interfaces.TreeDifference;
import fileTree.classes.TreeSingleton;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import models.classes.RestResponse;
import models.classes.SharedDirectory;
import models.classes.User;
import org.apache.commons.io.FileUtils;
import restful.clients.FileRestClient;
import restful.clients.SharedDirectoryRestClient;
import threads.interfaces.ThreadControl;
import tools.AlertWindows;
import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static restful.constants.HttpStatusCodes.GC_HTTP_OK;
import static threads.constants.FileManagerConstants.*;

public class FileManagerThreadControl implements ThreadControl, Runnable {
    private volatile boolean isRunning;
    private FileRestClient gob_restClient;
    private AtomicInteger gva_commandIndex = new AtomicInteger(0);
    private volatile List<Command> gco_commands = Collections.synchronizedList(new ArrayList<Command>());
    private static final int GC_MAX_TRIES = 20;

    @Override
    public void start() {
        if (!isRunning) {
            java.lang.Thread lob_runnerThread = new java.lang.Thread(this, FileManagerThreadControl.class.getSimpleName());
            lob_runnerThread.setDaemon(true);
            gob_restClient = RestClientBuilder.buildFileRestClientWithAuth();
            isRunning = true;
            lob_runnerThread.start();
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public void clear() {
        this.gco_commands.clear();
    }

    public void addFileWithCommando(File iob_file, String iva_commando, boolean iva_executeCommandOnServer, Object... iar_fileInformation) {
        Command lob_command = new Command();
        Command lob_serverCommand;

        lob_command.gob_file = iob_file;
        lob_command.gva_command = iva_commando;
        lob_command.gva_maxTries = 0;
        if (iar_fileInformation.length != 0) {
            lob_command.gar_fileInformation = iar_fileInformation;
        } else {
            lob_command.gar_fileInformation = null;
        }
        gco_commands.add(lob_command);

        if (iva_executeCommandOnServer && !iva_commando.endsWith("server")) {
            lob_serverCommand = new Command();
            lob_serverCommand.gob_file = iob_file;
            lob_serverCommand.gva_command = iva_commando + "_on_server";
            lob_serverCommand.gar_fileInformation = iar_fileInformation;
            lob_serverCommand.gva_maxTries = 0;

            gco_commands.add(lob_serverCommand);
        }
    }

    /**
     * forward the given command to the matching method
     * @param iob_command contains all information about the command
     */
    private void executeCommand(Command iob_command) {
        switch (iob_command.gva_command) {
            case GC_ADD:
                System.out.println(GC_ADD);
                addLocalFile(iob_command);
                break;

            case GC_UPLOAD_TO_SERVER:
                System.out.println(GC_UPLOAD_TO_SERVER);
                uploadFileToServer(iob_command);
                break;

            case GC_DELETE:
                System.out.println(GC_DELETE);
                deleteLocalFile(iob_command);
                break;

            case GC_DELETE_ON_SERVER:
                System.out.println(GC_DELETE_ON_SERVER);
                deleteFileOnServer(iob_command);
                break;

            case GC_MOVE:
                System.out.println(GC_MOVE);
                moveLocalFile(iob_command);
                break;

            case GC_MOVE_ON_SERVER:
                System.out.println(GC_MOVE_ON_SERVER);
                moveFileOnServer(iob_command);
                break;

            case GC_DELETE_DIR_ONLY:
                System.out.println(GC_DELETE_DIR_ONLY);
                deleteLocalDirectory(iob_command);
                break;

            case GC_DELETE_DIR_ONLY_ON_SERVER:
                System.out.println(GC_DELETE_DIR_ONLY_ON_SERVER);
                deleteDirectoryOnServer(iob_command);
                break;

            case GC_RENAME:
                System.out.println(GC_RENAME);
                renameLocalFile(iob_command);
                break;

            case GC_RENAME_ON_SERVER:
                System.out.println(GC_RENAME_ON_SERVER);
                renameFileOnServer(iob_command);
                break;

            case GC_DOWNLOAD_FROM_SERVER:
                System.out.println(GC_DOWNLOAD_FROM_SERVER + ": " + gco_commands.size());
                downloadFile(iob_command);
                break;

            case GC_DELETE_SHARED_DIR:
                System.out.println(GC_DELETE_SHARED_DIR);
                deleteSharedDirectory(iob_command);
                break;

            case GC_COMPARE_TREE:
                System.out.println("GC_COMPARE_TREE");
                compareTrees(iob_command);
                break;

            default: gco_commands.remove(iob_command);
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// methods to execute a command
//----------------------------------------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------------------------------------------
    // "GC_ADD"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * add the file only to the client
     * @param iob_command expected input in gar_information if the file does not exist:
     *                    1. boolean is the file a directory
     *
     */
    @SuppressWarnings("Duplicates")
    private void addLocalFile(Command iob_command) {
        boolean lva_isDirectory;
        boolean lva_addToPrevention = false;
        Tree lob_tree = TreeSingleton.getInstance().getTree();

        try {
            if (iob_command.gob_file.exists()) {
                lva_isDirectory = iob_command.gob_file.isDirectory();
            } else {
                lva_isDirectory = getObjectFromInformationArray(iob_command, 0, Boolean.class);
            }
        } catch (RuntimeException ex) {
            gco_commands.remove(iob_command);
            System.err.println("Command removed");
            return;
        }

        try {
            lva_addToPrevention = getObjectFromInformationArray(iob_command, 1, Boolean.class);
        } catch(RuntimeException ignore) {

        }

        if (TreeTool.filterRootFiles(iob_command.gob_file.toPath())) {
            gco_commands.remove(iob_command);
            System.err.println("Command removed: rootFile filtered");
            return;
        }

        //add the file to the tree
//        lob_tree.addFile(iob_command.gob_file, lva_isDirectory);

        if (lva_addToPrevention) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(iob_command.gob_file.toPath());
        }

        if (!TreeTool.isFileInTreeView(iob_command.gob_file)) {
//            Platform.runLater(() -> TreeTool.addToTreeView(iob_command.gob_file));
            if (!TreeTool.addToTreeView(iob_command.gob_file)) {
                gva_commandIndex.incrementAndGet();
                return;
            }
        }

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_UPLOAD_TO_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * upload a file or a directory to the server
     * @param iob_command expected input in gar_information if the file is a directory and not yet created:
     *                    1. boolean is the file a directory
     */
    @SuppressWarnings("Duplicates")
    private void uploadFileToServer(Command iob_command) {
        boolean lva_isDirectory;

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            return;
        }

        try {
            if (iob_command.gob_file.exists()) {
                lva_isDirectory = iob_command.gob_file.isDirectory();
            } else {
                lva_isDirectory = getObjectFromInformationArray(iob_command, 0, Boolean.class);
            }
        } catch (RuntimeException ex) {
            gco_commands.remove(iob_command);
            return;
        }

        if (TreeTool.filterRootFiles(iob_command.gob_file.toPath())) {
            gco_commands.remove(iob_command);
            return;
        }

        if (lva_isDirectory) {
            if (gob_restClient.createDirectoryOnServer(iob_command.gob_file)) {
                gco_commands.remove(iob_command);
            } else {
                iob_command.gva_maxTries++;
                gva_commandIndex.incrementAndGet();
            }
        } else {
            if (!iob_command.gob_file.exists()) {
                try {
                    if (!iob_command.gob_file.createNewFile()) {
                        iob_command.gva_maxTries++;
                        gva_commandIndex.incrementAndGet();
                        return;
                    }
                } catch (IOException ex) {
                    iob_command.gva_maxTries++;
                    gva_commandIndex.incrementAndGet();
                    return;
                }
            }

            if (gob_restClient.uploadFilesToServer(iob_command.gob_file)) {
                gco_commands.remove(iob_command);
            } else {
                iob_command.gva_maxTries++;
                gva_commandIndex.incrementAndGet();
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * delete file on the client
     * @param iob_command expected input in gar_information
     */
    private void deleteLocalFile(Command iob_command) {
        Tree lob_tree = TreeSingleton.getInstance().getTree();
        Platform.runLater(() -> TreeTool.getInstance().deleteItem(iob_command.gob_file));
        String lva_relativeFilePath;
        int lva_directoryId;
        SharedDirectoryCache lob_sharedDirCache;

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
        }

        lva_relativeFilePath = Utils.buildRelativeFilePath(iob_command.gob_file);
        lva_directoryId = Utils.getDirectoryIdFromRelativePath(lva_relativeFilePath, false);

        if (lob_tree.deleteFile(iob_command.gob_file)) {
            if (lva_directoryId > 0) {
                if (lva_relativeFilePath.split("\\\\").length == 2) {
                    lob_sharedDirCache = SharedDirectoryCache.getInstance();
                    DirectoryNameMapper.removeSharedDirectory(lva_directoryId);
                    lob_sharedDirCache.removeData(lva_directoryId);
                }
            }
            gco_commands.remove(iob_command);
            return;
        }

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE_ON_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * delete a file on the server
     * @param iob_command file cant be null
     */
    private void deleteFileOnServer(Command iob_command) {

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
        }

        if (iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            return;
        }

        if (gob_restClient.deleteOnServer(iob_command.gob_file)) {
            gco_commands.remove(iob_command);
        } else {
            iob_command.gva_maxTries++;
            gva_commandIndex.incrementAndGet();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_MOVE"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @param iob_command expected input in gar_information:
     *                    1: new File path
     *                    2: boolean whether the file should be only moved in the tree or on the disk as well
     */
    private void moveLocalFile(Command iob_command) {
//-------------------------Variables------------------------------------
        File lob_oldFilePath;
        File lob_newFilePath;
        boolean lva_moveOnlyInTree;
        TreeItem<String> lob_item;
        TreeItem<String> lob_newParent;
        TreeItem<String> lob_parent;
        Tree lob_tree = TreeSingleton.getInstance().getTree();

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        try {
            //get all needed information from the informationArray
            lob_oldFilePath = iob_command.gob_file;
            lob_newFilePath = getObjectFromInformationArray(iob_command, 0, File.class);
            lva_moveOnlyInTree = getObjectFromInformationArray(iob_command, 1, Boolean.class);

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            gco_commands.remove(iob_command);
            return;
        }

        if (!iob_command.gob_file.exists() && !lva_moveOnlyInTree) {
            gco_commands.remove(iob_command);
            return;
        }

        lob_item = TreeTool.getTreeItem(lob_oldFilePath);
        lob_newParent = TreeTool.getTreeItem(lob_newFilePath);

        if (lob_item == null || lob_newParent == null) {
            gco_commands.remove(iob_command);
            return;
        }

        lob_parent = lob_item.getParent();

        if (lob_parent == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (!canFileBeMoved(lob_item, lob_newParent)) {
            Platform.runLater(() -> new AlertWindows().createErrorAlert("File could not be moved. There is a File with the same name"));
            gco_commands.remove(iob_command);
            return;
        }

        //move the item in the treeView
        Platform.runLater(() -> {
            lob_parent.getChildren().remove(lob_item);
            lob_newParent.getChildren().add(lob_item);
        });

        lob_tree.moveFile(lob_oldFilePath, lob_newFilePath.getAbsolutePath(), lva_moveOnlyInTree);
        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_MOVE_ON_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @param iob_command expected input in gar_information:
     *                    1: new File path
     */
    private void moveFileOnServer(Command iob_command) {
        int lva_requestResult;
        File lob_oldFilePath;
        File lob_newFilePath;
        String lva_oldRelativePath;
        String lva_newRelativePath;

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            return;
        }

        try {
            //get all needed information from the informationArray
            lob_oldFilePath = iob_command.gob_file;
            lob_newFilePath = getObjectFromInformationArray(iob_command, 0, File.class);
            lva_oldRelativePath = Utils.buildRelativeFilePath(lob_oldFilePath);
            lva_newRelativePath = Utils.buildRelativeFilePath(lob_newFilePath);

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            gco_commands.remove(iob_command);
            return;
        }

        lva_requestResult = gob_restClient.moveFile(lva_oldRelativePath, lva_newRelativePath);

        if (lva_requestResult > 2) {
            iob_command.gva_maxTries++;
            gva_commandIndex.incrementAndGet();
            return;
        }

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE_DIR_ONLY"
    //------------------------------------------------------------------------------------------------------------------
    private void deleteLocalDirectory(Command iob_command) {
        TreeItem<String> lob_item;
        TreeItem<String> lob_parent;
        Tree lob_tree = TreeSingleton.getInstance().getTree();

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (!iob_command.gob_file.isDirectory()) {
            gco_commands.remove(iob_command);
            return;
        }

//        lob_parent = TreeTool.getTreeItem(iob_command.gob_file.getParentFile());
        lob_item = TreeTool.getTreeItem(iob_command.gob_file);

        if (lob_item == null) {
            gco_commands.remove(iob_command);
            return;
        }

        lob_parent = lob_item.getParent();

        if (lob_parent == null) {
            gco_commands.remove(iob_command);
            return;
        }

        for (TreeItem<String> lob_child : lob_item.getChildren()) {
            if (!canFileBeMoved(lob_child, lob_parent)) {
                gco_commands.remove(iob_command);
                Platform.runLater(() ->  new AlertWindows().createErrorAlert("The directory contains a file with the same nam as in the parent directory"));
                return;
            }
        }

        Platform.runLater(() ->
            lob_parent.getChildren().addAll(lob_item.getChildren())
        );

        Platform.runLater(() ->
            lob_parent.getChildren().remove(lob_item)
        );

        lob_tree.deleteDirectoryOnly(iob_command.gob_file);
        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE_DIR_ONLY_ON_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    private void deleteDirectoryOnServer(Command iob_command) {
        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            return;
        }

        if (!gob_restClient.deleteDirectoryOnly(iob_command.gob_file)) {
            gva_commandIndex.incrementAndGet();
            iob_command.gva_maxTries++;
            return;
        }

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_RENAME"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @param iob_command expected input in gar_information:
     *                    1: new File name
     *                    2: boolean whether the file should be renamed in the tree view or not
     */
    private void renameLocalFile(Command iob_command) {
        Tree lob_tree = TreeSingleton.getInstance().getTree();
        TreeItem<String> lob_item;
        String lva_newName;
        boolean lva_renameTreeItem;
        File lob_rootFile = TreeSingleton.getInstance().getTree().getRoot();
        File lob_privateFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getPrivateDirectoryName());
        File lob_publicFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getPublicDirectoryName());
        File lob_sharedFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getSharedDirectoryName());

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (!iob_command.gob_file.exists()) {
            gco_commands.remove(iob_command);
            return;
        }

        try {
            lva_newName = getObjectFromInformationArray(iob_command, 0, String.class);
            lva_renameTreeItem = getObjectFromInformationArray(iob_command, 1, Boolean.class);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            gco_commands.remove(iob_command);
            return;
        }

        if (lva_renameTreeItem) {
            lob_item = TreeTool.getTreeItem(iob_command.gob_file);

            if (lob_item == null) {
                gco_commands.remove(iob_command);
                return;
            }
            Platform.runLater(() -> lob_item.setValue(lva_newName));
        }

        if (iob_command.gob_file.equals(lob_privateFile)) {
            DirectoryNameMapper.setPrivateDirectoryName(lva_newName);
        }

        if (iob_command.gob_file.equals(lob_publicFile)) {
            DirectoryNameMapper.setPublicDirectoryName(lva_newName);
        }

        if (iob_command.gob_file.equals(lob_sharedFile)) {
            DirectoryNameMapper.setSharedDirectoryName(lva_newName);
        }

        if (TreeTool.isSharedDirectory(iob_command.gob_file.toPath())) {
            String lva_relativeFilePath = Utils.buildRelativeFilePath(iob_command.gob_file);
            String[] lar_relativePathArray = lva_relativeFilePath.split("\\\\");
            if (lar_relativePathArray.length == 2) {
                int lva_sharedDirectoryId = DirectoryNameMapper.getIdOfSharedDirectory(lar_relativePathArray[1]);
                DirectoryNameMapper.setNameOfSharedDirectory(lva_sharedDirectoryId, lva_newName);
            }
        }

        lob_tree.renameFile(iob_command.gob_file, lva_newName);
        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_RENAME_ON_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @param iob_command expected input in gar_information:
     *                    1: new File path
     */
    private void renameFileOnServer(Command iob_command) {
        String lva_newName;
        File lob_newFile;

        try {
            lva_newName = getObjectFromInformationArray(iob_command, 0, String.class);
        } catch (RuntimeException ex) {
            gco_commands.remove(iob_command);
            return;
        }

        lob_newFile = new File(iob_command.gob_file.toString().replaceFirst("[^\\\\]*$", lva_newName));
        if (TreeTool.isRootFile(lob_newFile) || TreeTool.isSharedDirectory(lob_newFile.toPath())) {
            gco_commands.remove(iob_command);
            System.out.println("Filter root file");
            return;
        }

        if (!gob_restClient.renameFile(iob_command.gob_file, lva_newName)) {
            iob_command.gva_maxTries++;
            return;
        }

        gco_commands.remove(iob_command);

    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DOWNLOAD_FROM_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    /**
     *
     * @param iob_command expected input in gar_information:
     *                    1: relative File Path
     */
    private void downloadFile(Command iob_command) {
        String lva_relativePath;
        String lva_newFilePath;
        File lob_directory;
        File lob_newFile;
        byte[] lar_fileContent;

        if (iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            System.err.println("Max Tries");
        }

        try {
            lva_relativePath = getObjectFromInformationArray(iob_command, 0, String.class);
        } catch (RuntimeException ex) {
            gco_commands.remove(iob_command);
            System.err.println("Command removed");
            return;
        }

        Object lob_downloadContent = gob_restClient.downloadFile(lva_relativePath);

        if (lob_downloadContent == null) {
            iob_command.gva_maxTries++;
            gva_commandIndex.incrementAndGet();
//            gco_commands.remove(iob_command);
//            System.err.println("Command removed: Content null");
            return;
        }

        lva_newFilePath = Utils.convertRelativeToAbsolutePath(lva_relativePath, false);

        //the download returned a file so it must be a directory
        if (lob_downloadContent instanceof Integer) {
            lob_directory = new File(lva_newFilePath);
            this.addFileWithCommando(lob_directory, GC_ADD, false, true, true);
            gco_commands.remove(iob_command);
            return;
        }

        if (lob_downloadContent instanceof byte[]) {
            try {
                lar_fileContent = (byte[]) lob_downloadContent;
                lob_newFile = new File(lva_newFilePath);
                FileUtils.writeByteArrayToFile(lob_newFile, lar_fileContent);
                this.addFileWithCommando(lob_newFile, GC_ADD, false, false, true);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                System.err.println("Command removed");
            }
        }

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE_SHARED_DIR"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * compare the local tree to the tree on the server
     * @param iob_command expected input in gar_information:
     *                    1. shared directory
     *                    2. user who wants to delete the directory
     */
    private void deleteSharedDirectory(Command iob_command) {
        SharedDirectoryRestClient lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();
        SharedDirectory lob_sharedDirectory;
        DataCache lob_dataCache = DataCache.getDataCache();
        User lob_user;
        RestResponse lob_response;
        SharedDirectoryCache lob_sharedDirCache;

        if (iob_command.gob_file == null ||iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            return;
        }

        try {
            lob_sharedDirectory = getObjectFromInformationArray(iob_command, 0, SharedDirectory.class);
            lob_user = getObjectFromInformationArray(iob_command, 1, User.class);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            gco_commands.remove(iob_command);
            return;
        }

        if (lob_sharedDirectory.getOwner().getEmail().equals(lob_user.getEmail())) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(iob_command.gob_file.toPath());

            iob_command.gar_fileInformation[0] = lob_sharedDirectory.getId();
            deleteFileOnServer(iob_command);
            deleteLocalFile(iob_command);

            lob_response = lob_restClient.deleteSharedDirectory(lob_sharedDirectory);

        } else if (lob_user.getEmail().equals(lob_dataCache.get(DataCache.GC_EMAIL_KEY))) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(iob_command.gob_file.toPath());

            deleteLocalFile(iob_command);

            lob_response = lob_restClient.removeMemberFromSharedDirectory(lob_sharedDirectory, lob_user);
        } else {
            Platform.runLater(() -> new AlertWindows().createWarningAlert("You are not allowed to remove other members!"));
            gco_commands.remove(iob_command);
            return;
        }

        lob_sharedDirCache = SharedDirectoryCache.getInstance();
        DirectoryNameMapper.removeSharedDirectory(lob_sharedDirectory.getId());
        lob_sharedDirCache.removeData(lob_sharedDirectory.getId());

        Platform.runLater(() -> Utils.printResponseMessage(lob_response));

        if (lob_response.getHttpStatus() != GC_HTTP_OK) {
            iob_command.gva_maxTries++;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_COMPARE_TREE"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * compare the local tree to the tree on the server
     * @param iob_command expected input in gar_information:
     *                    1. tree from the client
     */
    private void compareTrees(Command iob_command) {
        Tree lob_tree;
        int lva_loopIndex = 0;

        if (iob_command.gar_fileInformation == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (iob_command.gar_fileInformation[0] instanceof Tree) {
            lob_tree = (Tree) iob_command.gar_fileInformation[0];
        } else {
            gco_commands.remove(iob_command);
            return;
        }

        Collection<TreeDifference> lco_differences = gob_restClient.compareClientAndServerTree(lob_tree);
        for (TreeDifference lob_difference : lco_differences) {
            addFiles(lob_difference, lva_loopIndex);
            deleteFiles(lob_difference, lob_tree, lva_loopIndex);
            lva_loopIndex++;
        }

        gco_commands.remove(iob_command);
    }

//----------------------------------------------------------------------------------------------------------------------
// helper methods
//----------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canFileBeMoved(TreeItem<String> iob_item, TreeItem<String> iob_newParent) {
        //check if a file with the same name already exists
        for (TreeItem<String> lob_child : iob_newParent.getChildren()) {
            if (lob_child.getValue().equals(iob_item.getValue())) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> T getObjectFromInformationArray(Command iob_command, int iob_index, Class<T> iob_class) {
        if (iob_command.gar_fileInformation == null || iob_command.gar_fileInformation.length <= iob_index) {
            gco_commands.remove(iob_command);
            throw new RuntimeException();
        }

        if (iob_class.isAssignableFrom(iob_command.gar_fileInformation[iob_index].getClass())) {
            return (T) (iob_command.gar_fileInformation[iob_index]);
        } else {
            gco_commands.remove(iob_command);
            throw new RuntimeException();
        }
    }

    private void addFiles(TreeDifference iob_difference, int iva_loopIndex) {
        for (String lva_addFile : iob_difference.getFilesToInsert()) {
            if (iva_loopIndex == 0) {
                lva_addFile = DirectoryNameMapper.getPrivateDirectoryName() + lva_addFile;
            }

            if (iva_loopIndex == 1) {
                lva_addFile = DirectoryNameMapper.getPublicDirectoryName() + lva_addFile;
            }

            this.addFileWithCommando(null, GC_DOWNLOAD_FROM_SERVER, true, lva_addFile);
        }
    }

    private void deleteFiles(TreeDifference iob_difference, Tree iob_tree, int iva_loopIndex) {
        for (String lva_deleteFile : iob_difference.getFilesToDelete()) {
            if (iva_loopIndex == 0) {
                lva_deleteFile = iob_tree.getRoot().getAbsolutePath() + "\\" + DirectoryNameMapper.getPrivateDirectoryName() + lva_deleteFile;
            }

            if (iva_loopIndex == 1) {
                lva_deleteFile = iob_tree.getRoot().getAbsolutePath() + "\\" + DirectoryNameMapper.getPublicDirectoryName() + lva_deleteFile;

            }
            File lob_file = new File(lva_deleteFile);
            iob_tree.deleteFile(lva_deleteFile);
            TreeTool.getInstance().deleteItem(lob_file);
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        Command lob_command;
        while (isRunning) {
            try {
                if (!(gva_commandIndex.get() >= gco_commands.size() || gva_commandIndex.get() < 0)) {
                    lob_command = gco_commands.get(gva_commandIndex.get());
                    executeCommand(lob_command);
                } else {
                    gva_commandIndex.set(0);
                    try {
                        java.lang.Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                gco_commands.remove(gco_commands.get(gva_commandIndex.get()));
            }
        }
    }

    private class Command {
        private Object[] gar_fileInformation;
        private File gob_file;
        private String gva_command;
        private int gva_maxTries;
    }
}
