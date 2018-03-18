package threads.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import cache.DirectoryCache;
import cache.FileMapperCache;
import cache.SharedDirectoryCache;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import models.classes.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.PathFileComparator;
import restful.clients.FileRestClient;
import restful.clients.SharedDirectoryRestClient;
import threads.constants.FileManagerConstants;
import threads.interfaces.ThreadControl;
import tools.AlertWindows;
import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.lang.Thread;

import static models.classes.FileService.readAllFilesFromDirectory;
import static restful.constants.HttpStatusCodes.GC_HTTP_OK;
import static threads.constants.FileManagerConstants.*;

public class FileManagerThreadControl implements ThreadControl, Runnable {
    private volatile boolean isRunning;
    private FileRestClient gob_restClient;
    private final AtomicInteger gva_commandIndex = new AtomicInteger(0);
    private final List<Command> gco_commands = Collections.synchronizedList(new ArrayList<Command>());
    private static final int GC_MAX_TRIES = 20;
    private static Thread gob_thread;

    @Override
    public void start() {
        if (!isRunning) {
            gob_thread = new Thread(this, FileManagerThreadControl.class.getSimpleName());
            gob_thread.setDaemon(true);
            gob_restClient = RestClientBuilder.buildFileRestClientWithAuth();
            isRunning = true;
            gob_thread.start();
        }
    }

    @Override
    public void stop() {
        gob_thread.interrupt();
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
                System.out.println(GC_COMPARE_TREE);
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
     *                    2. boolean add to file prevention
     *                    3. int version of the file
     */
    private void addLocalFile(Command iob_command) {
        boolean lva_isDirectory;
        boolean lva_addToPrevention = false;
        MappedFile lob_mappedFile;
        int lva_version;
        long lva_lastModified;

        if (iob_command.gva_maxTries >= GC_MAX_TRIES) {
            gco_commands.remove(iob_command);
            System.out.println("Command removed: max tries");
            return;
        }

        try {
            if (iob_command.gob_file.exists()) {
                lva_isDirectory = iob_command.gob_file.isDirectory();
            } else {
                lva_isDirectory = getObjectFromInformationArray(iob_command, 0, Boolean.class);
            }
            lva_version = getObjectFromInformationArray(iob_command, 2, Integer.class);
        } catch (RuntimeException ex) {
            gco_commands.remove(iob_command);
            System.err.println("Command removed:");
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

        try {
            if (!iob_command.gob_file.exists()) {
                if (lva_isDirectory) {
                    if (!iob_command.gob_file.mkdir()) {
                        iob_command.gva_maxTries++;
                        gva_commandIndex.incrementAndGet();
                        return;
                    }
                } else {
                    if (!iob_command.gob_file.createNewFile()) {
                        iob_command.gva_maxTries++;
                        gva_commandIndex.incrementAndGet();
                        return;
                    }
                }
            }
        } catch (IOException ex) {
            iob_command.gva_maxTries++;
            gva_commandIndex.incrementAndGet();
            return;
        }

        if (lva_addToPrevention) {
            PreventDuplicateOperation.getDuplicateOperationPrevention().putCreated(iob_command.gob_file.toPath());
        }

        try {
            lva_lastModified = Files.readAttributes(iob_command.gob_file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis();
            lob_mappedFile = FileMapperCache.getFileMapperCache().get(iob_command.gob_file.toPath());

            if (lob_mappedFile == null) {
                lob_mappedFile = new MappedFile(iob_command.gob_file.toPath(), lva_version, lva_lastModified);
                FileMapperCache.getFileMapperCache().put(lob_mappedFile);
            } else {
                lob_mappedFile.setVersion(lva_version);
                lob_mappedFile.setLastModified(lva_lastModified);
            }
//            System.out.println(lob_mappedFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!TreeTool.isFileInTreeView(iob_command.gob_file)) {
            if (!TreeTool.addToTreeView(iob_command.gob_file)) {
                gva_commandIndex.incrementAndGet();
                return;
            }
        }


//        print();

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
     *                    0 boolean whether the file should be added to the duplicate prevention
     */
    private void deleteLocalFile(Command iob_command) {
        String lva_relativeFilePath;
        int lva_directoryId;
        SharedDirectoryCache lob_sharedDirCache;
        boolean lva_addToFilePrevention = false;
        Collection<File> lco_filesToDelete;
        TreeItem<String> lob_itemToDelete;

        try {
            lva_addToFilePrevention = getObjectFromInformationArray(iob_command, 0, Boolean.class);
        } catch (RuntimeException ignore) {

        }

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
        }



        lob_itemToDelete = TreeTool.getTreeItem(iob_command.gob_file);

        if (lob_itemToDelete != null) {
            //remove the item from the tree
            lob_itemToDelete.getParent().getChildren().remove(lob_itemToDelete);
        }

        if (iob_command.gob_file.exists()) {
            lco_filesToDelete = readAllFilesFromDirectory(iob_command.gob_file);
            //file could not be deleted
            try {

                if (iob_command.gob_file.isDirectory()) {
                    FileUtils.deleteDirectory(iob_command.gob_file);
                } else {
                    FileUtils.deleteQuietly(iob_command.gob_file);
                }

            } catch (IOException ex) {
                iob_command.gva_maxTries++;
                gva_commandIndex.incrementAndGet();
                return;
            }

            //remove all paths from the FileMapperCache
            for (File lob_file : lco_filesToDelete) {
                if (lva_addToFilePrevention) {
                    PreventDuplicateOperation.getDuplicateOperationPrevention().putDeleted(lob_file.toPath());
                }
                FileMapperCache.getFileMapperCache().remove(lob_file.toPath());
            }
        } else {
            FileMapperCache.getFileMapperCache().removeFileAndChildren(iob_command.gob_file.toPath());
        }

        lva_relativeFilePath = Utils.buildRelativeFilePath(iob_command.gob_file);
        lva_directoryId = Utils.getDirectoryIdFromRelativePath(lva_relativeFilePath, false);

//        if (lob_tree.deleteFile(iob_command.gob_file)) {
        if (lva_directoryId > 0) {
            if (lva_relativeFilePath.split("\\\\").length == 2) {
                lob_sharedDirCache = SharedDirectoryCache.getInstance();
                DirectoryNameMapper.removeSharedDirectory(lva_directoryId);
                lob_sharedDirCache.removeData(lva_directoryId);
            }
        }
//            gco_commands.remove(iob_command);
//            return;
//        }

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
        File lob_newChildFile;
        boolean lva_moveOnlyInTree;
        TreeItem<String> lob_item;
        TreeItem<String> lob_newParent;
        TreeItem<String> lob_parent;
        Collection<File> lco_files;
        MappedFile lob_mappedFile;

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

        try {
            lob_newFilePath = new File(lob_newFilePath + "\\" + lob_oldFilePath.getName());
            if (!lva_moveOnlyInTree) {
                if (!iob_command.gob_file.exists()) {
                    gco_commands.remove(iob_command);
                    return;
                }
                lco_files = FileService.readAllFilesFromDirectory(iob_command.gob_file);

                if (iob_command.gob_file.isDirectory()) {
                    FileUtils.moveDirectory(iob_command.gob_file, lob_newFilePath);
                } else {

                    FileUtils.moveFile(iob_command.gob_file, lob_newFilePath);
                }

                for (File lob_file : lco_files) {
                    lob_mappedFile = FileMapperCache.getFileMapperCache().get(lob_file.toPath());
                    lob_newChildFile = new File(
                            lob_file.toString().replace(lob_oldFilePath.toString(), lob_newFilePath.toString())
                    );
                    lob_mappedFile.setVersion(1);
                    FileMapperCache.getFileMapperCache().updateKeyAndValue(lob_file.toPath(), lob_newChildFile.toPath(), lob_mappedFile);
                }
            } else {
                lco_files = FileService.readAllFilesFromDirectory(lob_newFilePath);

                String lva_oldFilePath = iob_command.gob_file.toString();
                for (File lob_file : lco_files) {
                    lva_oldFilePath = lob_file.toString().replace(lob_newFilePath.toString(), lva_oldFilePath);
                    lob_mappedFile = FileMapperCache.getFileMapperCache().get(new File(lva_oldFilePath).toPath());
                    if (lob_mappedFile != null) {
                        FileMapperCache.getFileMapperCache().updateKeyAndValue(lob_mappedFile.getFilePath(), lob_file.toPath(), lob_mappedFile);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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

        } catch (RuntimeException ex) {
            ex.printStackTrace();
            gco_commands.remove(iob_command);
            return;
        }

        lva_requestResult = gob_restClient.moveFile(lob_oldFilePath, lob_newFilePath);

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
        File lob_parentFile;
        Collection<File> lco_files;

        if (iob_command.gob_file == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (!iob_command.gob_file.isDirectory()) {
            gco_commands.remove(iob_command);
            return;
        }

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

        lob_parentFile = iob_command.gob_file.getParentFile();
        for (File lob_file : Objects.requireNonNull(iob_command.gob_file.listFiles())) {
            addFileWithCommando(lob_file, GC_MOVE, true, lob_parentFile, true);
        }

        lco_files = readAllFilesFromDirectory(iob_command.gob_file);

        for (File lob_file : lco_files) {
            PreventDuplicateOperation.getDuplicateOperationPrevention().putMoved(lob_file.toPath());
        }

        try {
            for (File lob_file : Objects.requireNonNull(iob_command.gob_file.listFiles())) {
                if (lob_file.isDirectory()) {
                    FileUtils.moveDirectoryToDirectory(lob_file, lob_parentFile, false);
                } else {
                    FileUtils.moveFileToDirectory(lob_file, lob_parentFile, false);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        addFileWithCommando(iob_command.gob_file, GC_DELETE, true, true);

//        Platform.runLater(() ->
//            lob_parent.getChildren().addAll(lob_item.getChildren())
//        );
//
//        Platform.runLater(() ->
//            lob_parent.getChildren().remove(lob_item)
//        );

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
        TreeItem<String> lob_item;
        String lva_newName;
        boolean lva_renameTreeItem;
        String lva_newFilePath;
        String lva_newChildFilePath;
        String lva_oldFilePath;
        Collection<File> lco_files;
        MappedFile lob_mappedFile;
        FileMapperCache lob_mapperCache = FileMapperCache.getFileMapperCache();
        File lob_rootFile = DirectoryCache.getDirectoryCache().getUserDirectory();
        File lob_newFile;
        File lob_privateFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getPrivateDirectoryName());
        File lob_publicFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getPublicDirectoryName());
        File lob_sharedFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getSharedDirectoryName());

        if (iob_command.gob_file == null) {
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

        lva_newFilePath = iob_command.gob_file.toString().replaceFirst("[^^\\\\]*$", lva_newName);
        lob_newFile = new File(lva_newFilePath);

        if (iob_command.gob_file.exists()) {
            lco_files = FileService.readAllFilesFromDirectory(iob_command.gob_file);

            for (File lob_file : lco_files) {
                lob_mappedFile = lob_mapperCache.get(lob_file.toPath());
                lva_newChildFilePath = lob_mappedFile.getFilePath().toString();
                lva_newChildFilePath = lva_newChildFilePath.replace(iob_command.gob_file.toString(), lob_newFile.toString());
                lob_mapperCache.updateKeyAndValue(lob_file.toPath(), new File(lva_newChildFilePath).toPath(), lob_mappedFile);
            }

            if (!iob_command.gob_file.renameTo(lob_newFile)) {
                iob_command.gva_maxTries++;
                gva_commandIndex.incrementAndGet();
            }
        } else {
            lco_files = FileService.readAllFilesFromDirectory(lob_newFile);

            lva_oldFilePath = iob_command.gob_file.toString();
            for (File lob_file : lco_files) {
                lva_oldFilePath = lob_file.toString().replace(lob_newFile.toString(), lva_oldFilePath);
                lob_mappedFile = lob_mapperCache.get(new File(lva_oldFilePath).toPath());
                if (lob_mappedFile != null) {
                    lob_mapperCache.updateKeyAndValue(lob_mappedFile.getFilePath(), lob_file.toPath(), lob_mappedFile);
                }
            }
        }

        lob_mappedFile = FileMapperCache.getFileMapperCache().get(lob_newFile.toPath());
        lob_mappedFile.setVersion(lob_mappedFile.getVersion() + 1);

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
        if (TreeTool.isRootFile(lob_newFile)) {
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
        DownloadedContent lob_downloadedContent;

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

        lob_downloadedContent = gob_restClient.downloadFile(lva_relativePath);

        if (lob_downloadedContent == null) {
            iob_command.gva_maxTries++;
            gva_commandIndex.incrementAndGet();
            return;
        }

        lva_newFilePath = Utils.convertRelativeToAbsolutePath(lva_relativePath, false);

        //the download returned a file so it must be a directory
        if (lob_downloadedContent.isDirectory()) {
            lob_directory = new File(lva_newFilePath);
            this.addFileWithCommando(lob_directory, GC_ADD, false, true, true, lob_downloadedContent.getVersion());
            PreventDuplicateOperation.getDuplicateOperationPrevention().putCreated(lob_directory.toPath());
            gco_commands.remove(iob_command);
            return;
        } else {
            try {
                lar_fileContent = lob_downloadedContent.getFileContent();
                lob_newFile = new File(lva_newFilePath);
                FileUtils.writeByteArrayToFile(lob_newFile, lar_fileContent);
                this.addFileWithCommando(lob_newFile, GC_ADD, false, false, true, lob_downloadedContent.getVersion());
                PreventDuplicateOperation.getDuplicateOperationPrevention().putCreated(lob_newFile.toPath());
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
            PreventDuplicateOperation.getDuplicateOperationPrevention().putDeleted(iob_command.gob_file.toPath());

            iob_command.gar_fileInformation[0] = lob_sharedDirectory.getId();
            deleteFileOnServer(iob_command);
            deleteLocalFile(iob_command);

            lob_response = lob_restClient.deleteSharedDirectory(lob_sharedDirectory);

        } else if (lob_user.getEmail().equals(lob_dataCache.get(DataCache.GC_EMAIL_KEY))) {
            PreventDuplicateOperation.getDuplicateOperationPrevention().putDeleted(iob_command.gob_file.toPath());

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
    @SuppressWarnings("Duplicates")
    private void compareTrees(Command iob_command) {
        FileRestClient lob_restClient = RestClientBuilder.buildFileRestClientWithAuth();
        TreeDifference lob_treeDifference;
        String lva_relativeDownloadPath;
        List<File> lco_filesToDelete;
        Path lob_pointer;
        Path lob_parent = null;

//        for (MappedFile lob_mappedFile : FileMapperCache.getFileMapperCache().getAll()) {
//            System.out.println(t.toString());
//        }

        lob_treeDifference = lob_restClient.compareClientAndServerTree();

        if (lob_treeDifference == null) {
            iob_command.gva_maxTries++;
            gva_commandIndex.incrementAndGet();
            return;
        }

        lco_filesToDelete = lob_treeDifference.getFilesToDelete().stream().map(File::new).collect(Collectors.toList());

        lco_filesToDelete.sort(PathFileComparator.PATH_COMPARATOR);

        for (Iterator<File> lob_iterator = lco_filesToDelete.iterator(); lob_iterator.hasNext();) {
            lob_pointer = lob_iterator.next().toPath();

            if (lob_parent == null) {
                lob_parent = lob_pointer;
            }

            if (!lob_parent.equals(lob_pointer)) {
                if (lob_pointer.startsWith(lob_parent)) {
                    lob_iterator.remove();
                } else {
                    lob_parent = lob_pointer;
                }
            }
        }

        for (File lob_file : lco_filesToDelete) {
            File lob_deleteFile = new File(Utils.convertRelativeToAbsolutePath(lob_file.toString(), true));
            ThreadManager.addCommandToFileManager(lob_deleteFile, FileManagerConstants.GC_DELETE, false, true);
        }

        for (String lva_relativeFilePath : lob_treeDifference.getFilesToInsert()) {
            File lob_insertFile = new File(Utils.convertRelativeToAbsolutePath(lva_relativeFilePath, true));
            lva_relativeDownloadPath = Utils.buildRelativeFilePath(lob_insertFile);
            ThreadManager.addCommandToFileManager(lob_insertFile, FileManagerConstants.GC_DOWNLOAD_FROM_SERVER, true, lva_relativeDownloadPath);
        }

        for (String lva_relativeFilePath : lob_treeDifference.getFilesToUpdate()) {
            System.out.println(lva_relativeFilePath);

            File lob_updateFile = new File(Utils.convertRelativeToAbsolutePath(lva_relativeFilePath, true));
            lva_relativeDownloadPath = Utils.buildRelativeFilePath(lob_updateFile);
            ThreadManager.addCommandToFileManager(lob_updateFile, FileManagerConstants.GC_DOWNLOAD_FROM_SERVER, true, lva_relativeDownloadPath);
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
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        Command lob_command;
        while (true) {
            try {
                if (!(gva_commandIndex.get() >= gco_commands.size() || gva_commandIndex.get() < 0)) {
                    lob_command = gco_commands.get(gva_commandIndex.get());
                    executeCommand(lob_command);
                } else {
                    gva_commandIndex.set(0);
                    try {
                        java.lang.Thread.sleep(100);
                    } catch (InterruptedException ignore) {

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
