package fileTree.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import cache.SharedDirectoryCache;
import controller.classes.MainController;
import controller.classes.SharedDirectoryController;
import fileTree.interfaces.Tree;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import models.classes.SharedDirectory;
import restful.clients.SharedDirectoryRestClient;
import threads.constants.FileManagerConstants;
import threads.interfaces.ThreadControl;
import threads.classes.ThreadManager;
import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static fileTree.constants.TreeControlConstants.*;
import static tools.TreeTool.*;

public class TreeControl {
    private Tree gob_tree;
    private TreeView<String> gob_treeView;
    private ContextMenu gob_contextMenu;
    private final MainController gob_mainController;

    public TreeControl(String iva_ip, String iva_port, MainController iob_mainController) {
        gob_mainController = iob_mainController;
        File lob_userDirectory = initDirectories(iva_ip, iva_port);
        initSharedDirectoryCache();

        try {
            TreeSingleton.setTreeRootPath(lob_userDirectory.getCanonicalPath());
            gob_tree = TreeSingleton.getInstance().getTree();
            gob_treeView = TreeSingleton.getInstance().getTreeView();
            gob_treeView.setShowRoot(false);

            TreeItem<String> lob_root = new TreeItem<>(gob_tree.getRoot().getName());
            lob_root.setGraphic(TreeTool.getInstance().getTreeIcon(gob_tree.getRoot().getCanonicalPath()));
            gob_treeView.setRoot(lob_root);


            ThreadManager.getFileManagerThread().start();
            addFilesToTree(gob_tree.getRoot());
            ThreadManager.addCommandToFileManager(null, FileManagerConstants.GC_COMPARE_TREE, false, gob_tree);
            buildContextMenu();
            gob_treeView.setContextMenu(gob_contextMenu);
            gob_treeView.setOnContextMenuRequested(event ->
                    onContextMenuRequest()
            );

            gob_treeView.setEditable(true);
            Collection<File> lob_directoriesToWatch = gob_tree.getAllDirectories();
            lob_directoriesToWatch.clear();
            lob_directoriesToWatch.add(gob_tree.getRoot());
            ThreadControl lob_watcher = ThreadManager.getDirectoryWatcherThread(gob_tree.getRoot());
            lob_watcher.start();

            gob_treeView.setCellFactory(siTreeView ->
                    new TreeCellImpl(this.gob_tree)
            );

            gob_treeView.setOnEditCommit(event -> {
                int lva_counter = 0;
                if (!event.getOldValue().equals(event.getNewValue())) {
                    for (TreeItem<String> lob_sibling : event.getTreeItem().getParent().getChildren()) {
                        if (lob_sibling.getValue().equals(event.getNewValue())) {
                            lva_counter++;
                        }
                    }

                    if (lva_counter >= 1) {
                        event.getTreeItem().setValue(event.getOldValue());
                        return;
                    }

                    File lob_renamedFile = buildFileFromItem(event.getTreeItem(), gob_tree);
                    TreeSingleton.getInstance().getDuplicateOperationsPrevention().putRenamed(lob_renamedFile.toPath());
                    ThreadManager.addCommandToFileManager(lob_renamedFile, FileManagerConstants.GC_RENAME, true, event.getNewValue(), false);
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        gob_treeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, old_val, new_val) ->
                        gob_mainController.setTypeLabel(buildFileFromItem(new_val, gob_tree))
        );
    }

    private void addFilesToTree(File iob_file) {
        //we have to add the child nodes of the file if the file is a directory
        if (iob_file.isDirectory()) {
            //add the directory itself
            ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_ADD, false);

            File[] lob_fileList = iob_file.listFiles();
            if (lob_fileList != null) {
                //add all files in the directory
                for (File lob_directoryChildFile : lob_fileList) {
                    //if the directory contains another directory, do the same for it
                    if (lob_directoryChildFile.isDirectory()) {
                        if (!TreeTool.filterRootFiles(lob_directoryChildFile.toPath())) {
                            addFilesToTree(lob_directoryChildFile);
                        }
                    } else {
                        ThreadManager.addCommandToFileManager(lob_directoryChildFile, FileManagerConstants.GC_ADD, false);
                    }
                }
            }
        } else {
            ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_ADD, false);
        }
    }

    private void buildContextMenu() {
        //-----------------------Variables---------------------------------
        MenuItem lob_deleteFile;
        MenuItem lob_newDirectory;
        MenuItem lob_newFile;
        MenuItem lob_deleteDirectoryOnly;
        MenuItem lob_renameFile;
        MenuItem lob_sharedDirectory;
        //-----------------------------------------------------------------

        gob_contextMenu = new ContextMenu();
        lob_deleteFile = new MenuItem("Delete");
        lob_deleteFile.setOnAction(event -> {
            File lob_selectedFile = buildFileFromItem(
                    gob_treeView.getSelectionModel().getSelectedItem(), gob_tree
            );
            ThreadManager.addCommandToFileManager(lob_selectedFile, FileManagerConstants.GC_DELETE,
                    true, 0);
            addAllDeleted(lob_selectedFile);
        });

        lob_newDirectory = new MenuItem("New Directory");
        lob_newDirectory.setOnAction(event ->
                createNewDirectory()
        );

        lob_deleteDirectoryOnly = new MenuItem("Delete only Directory");
        lob_deleteDirectoryOnly.setOnAction(event -> {
                File lob_file = buildFileFromItem(gob_treeView.getSelectionModel().getSelectedItem(), gob_tree);
                ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_DELETE_DIR_ONLY, true);
                TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(lob_file.toPath());
            }
        );

        lob_renameFile = new MenuItem("Rename");
        lob_renameFile.setOnAction(event ->
                renameFile()
        );

        lob_newFile = new MenuItem("New File");
        lob_newFile.setOnAction(event ->
                createNewFile()
        );

        lob_sharedDirectory = new MenuItem("Properties");
        lob_sharedDirectory.setOnAction(event ->
                sharedDirectoryScene(gob_treeView.getSelectionModel().getSelectedItem())
        );

        gob_contextMenu.getItems().addAll(lob_deleteFile,
                lob_newDirectory,
                lob_newFile,
                lob_deleteDirectoryOnly,
                 lob_renameFile,
                lob_sharedDirectory);
    }

    private void onContextMenuRequest() {
        //-------------------------------Variables----------------------------------------
        TreeItem lob_treeItem = gob_treeView.getSelectionModel().getSelectedItem();
        File lob_selectedFile = buildFileFromItem(lob_treeItem, gob_tree);
        //--------------------------------------------------------------------------------

        if (gob_treeView.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        if (!lob_selectedFile.exists()) {
            return;
        }

        for (MenuItem lob_item : gob_contextMenu.getItems()) {
            switch (lob_item.getText()) {
                case GC_MENU_ITEM_DELETE_ONLY_DIR:
                    if (!lob_selectedFile.isDirectory() || isRootChildElement(lob_treeItem)
                            || lob_treeItem.getParent().getValue().equals(GC_DIR_NAME)) {
                        lob_item.setDisable(true);
                    } else {
                        lob_item.setDisable(false);
                    }
                    break;

                case GC_MENU_ITEM_DELETE:
                    if (isRootChildElement(lob_treeItem) || lob_treeItem.getParent().getValue().equals(GC_DIR_NAME)) {
                        lob_item.setDisable(true);
                    } else {
                        lob_item.setDisable(false);
                    }

                    break;

                case GC_MENU_ITEM_RENAME:
                    if (lob_treeItem.getValue().equals(GC_DIR_NAME)) {
                        lob_item.setDisable(true);
                    } else {
                        lob_item.setDisable(false);
                    }
                    break;

                case GC_MENU_ITEM_NEW_FILE:
                    if (lob_selectedFile.getName().equals(GC_DIR_NAME)) {
                        lob_item.setDisable(true);
                    } else {
                        lob_item.setDisable(false);
                    }

                    break;

                case GC_MENU_ITEM_NEW_DIR:
                    if (lob_selectedFile.getName().equals(GC_DIR_NAME)) {
                        lob_item.setDisable(true);
                    } else {
                        lob_item.setDisable(false);
                    }

                    break;

                case GC_MENU_ITEM_NEW_SHARED_DIR:
                    isFileSharedDirectory(lob_selectedFile, lob_item);
                    break;

                case GC_MENU_ITEM_PROPERTIES:
                    isFileSharedDirectory(lob_selectedFile, lob_item);
                    break;
            }
        }
    }

    private void isFileSharedDirectory(File lob_selectedFile, MenuItem lob_item) {
        if (lob_selectedFile.getName().equals("Shared")) {
            lob_item.setDisable(false);
            lob_item.setText("New shared directory");
        } else if (lob_selectedFile.getParentFile().getName().equals("Shared")) {
            lob_item.setDisable(false);
            lob_item.setText("Properties");
        } else {
            lob_item.setDisable(true);
        }
    }

    private void renameFile() {
        gob_treeView.edit(gob_treeView.getSelectionModel().getSelectedItem());
    }

    private void addAllDeleted(File iob_file) {
        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(iob_file.toPath());
        File[] lar_files = iob_file.listFiles();

        if (iob_file.isDirectory() && lar_files != null) {
            for (File file : lar_files) {
                addAllDeleted(file);
            }
        }
    }

//    private void addAllMovedOrRenamed(File iob_file) {
//        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putMoved(iob_file.toPath());
//        File[] lar_files = iob_file.listFiles();
//
//        if (iob_file.isDirectory() && lar_files != null) {
//            for (File file : lar_files) {
//                addAllDeleted(file);
//            }
//        }
//    }

    private void createNewDirectory() {
        File lob_newFile = buildNewFile("\\new Directory$");
//        createFileOrDirectory(lob_newFile, true, gob_restClient);
        ThreadManager.addCommandToFileManager(lob_newFile, FileManagerConstants.GC_ADD,
                true, true);
        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(lob_newFile.toPath());
    }

    private void createNewFile() {
        File lob_newFile = buildNewFile("\\new File$.txt");
//        createFileOrDirectory(lob_newFile, false, gob_restClient);
        ThreadManager.addCommandToFileManager(lob_newFile, FileManagerConstants.GC_ADD,
                true, false);

        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(lob_newFile.toPath());

    }

    private File buildNewFile(String iva_name) {
        //-------------------------------Variables----------------------------
        File lob_selectedFile = buildFileFromItem(
                gob_treeView.getSelectionModel().getSelectedItem(), gob_tree
        );
        File lob_newFile;
        int lva_counter = 1;
        String lva_newFilePath;
        //--------------------------------------------------------------------

        if (lob_selectedFile.isDirectory()) {
            lva_newFilePath = lob_selectedFile.toPath().toString();
        } else {
            lva_newFilePath = lob_selectedFile.getParentFile().toPath().toString();
        }

        lva_newFilePath += iva_name;
        lob_newFile = new File(lva_newFilePath.replaceFirst("\\$", ""));

        if (lob_newFile.exists()) {
            do {
                lob_newFile = new File(lva_newFilePath.replaceFirst("\\$", "(" + lva_counter + ")"));
                lva_counter++;
            } while (lob_newFile.exists());
        }
        return lob_newFile;
    }

    private void sharedDirectoryScene(TreeItem iob_treeItem) {
        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/sharedDirectoryScreen.fxml"));
        GridPane lob_pane;
        int lva_sharedDirectoryId;
        String lva_selectedItemName;
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        SharedDirectory lob_sharedDirectory;

        lva_selectedItemName = gob_treeView.getSelectionModel().getSelectedItem().getValue();

        try {
            lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle("Shared Directory");
            lob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
            lob_stage.setResizable(false);
            lob_stage.setScene(lob_scene);
            SharedDirectoryController lob_controller = lob_loader.getController();

            if (iob_treeItem.getValue().equals("Shared")) {
                lob_controller.initData(null, lob_stage, null);

            } else {
                lva_sharedDirectoryId = DirectoryNameMapper.getIdOfSharedDirectory(lva_selectedItemName);
                lob_sharedDirectory = lob_sharedDirectoryCache.get(lva_sharedDirectoryId);

                lob_controller.initData(lob_sharedDirectory, lob_stage,
                        buildFileFromItem(gob_treeView.getSelectionModel().getSelectedItem(), gob_tree));
            }

            lob_stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the path of the current selected treeItem
     * If the selected is a file, the parent directory path gets returned
     * If nothing is selected return null
     *
     * @return path of selected treeItem
     */
    public String getPathOfSelectedItem() {
        TreeItem<String> lob_treeItem = gob_treeView.getSelectionModel().getSelectedItem();
        File lob_file;

        if (lob_treeItem != null) {
            lob_file = buildFileFromItem(lob_treeItem, gob_tree);
            return lob_file.toPath().toString();
        }

        return null;
    }

    private boolean isRootChildElement(TreeItem iob_selectedItem) {
        return iob_selectedItem.getValue().equals(DirectoryNameMapper.getPrivateDirectoryName()) ||
                iob_selectedItem.getValue().equals(DirectoryNameMapper.getPublicDirectoryName()) ||
                iob_selectedItem.getValue().equals("Shared");
    }

    private void initSharedDirectoryCache() {
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        SharedDirectoryRestClient lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();
        List<SharedDirectory> lli_sharedDirectories;

        lli_sharedDirectories = lob_restClient.getAllSharedDirectoriesOfUser();

        for (SharedDirectory lob_sharedDirectory : lli_sharedDirectories) {
            lob_sharedDirectoryCache.put(lob_sharedDirectory.getId(), lob_sharedDirectory);
        }
    }

    private File initDirectories(String iva_ip, String iva_port) {
        File lob_rootDirectory = new File(Utils.getUserBasePath());
        File lob_serverDirectory = new File(Utils.getUserBasePath() + "\\" + iva_ip + "_" + iva_port);
        File lob_userDirectory = new File(lob_serverDirectory.getAbsolutePath() + "\\" + DataCache.getDataCache().get(DataCache.GC_EMAIL_KEY));
        File lob_publicDirectory;
        File lob_privateDirectory;
        File lob_sharedDirectories;

        //create the root directory if it does not exist
        TreeTool.getInstance().createDirectory(lob_rootDirectory);

        //create the server directory if it does not exist
        TreeTool.getInstance().createDirectory(lob_serverDirectory);

        //create the user directory if it does not exist
        TreeTool.getInstance().createDirectory(lob_userDirectory);

        lob_publicDirectory = new File(lob_userDirectory.getAbsolutePath() + "\\" + DirectoryNameMapper.getPublicDirectoryName());
        lob_privateDirectory = new File(lob_userDirectory.getAbsolutePath() + "\\" + DirectoryNameMapper.getPrivateDirectoryName());
        lob_sharedDirectories = new File(lob_userDirectory.getAbsolutePath() + "\\" + DirectoryNameMapper.getSharedDirectoryName());

        //create the public directory
        TreeTool.getInstance().createDirectory(lob_publicDirectory);

        //create the private directory
        TreeTool.getInstance().createDirectory(lob_privateDirectory);

        //create the shared directory
        TreeTool.getInstance().createDirectory(lob_sharedDirectories);

        return lob_userDirectory;
    }
}