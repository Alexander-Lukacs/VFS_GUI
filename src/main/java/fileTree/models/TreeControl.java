package fileTree.models;

import builder.RestClientBuilder;
import cache.DataCache;
import controller.MainController;
import controller.SharedDirectoryController;
import fileTree.interfaces.Tree;
import fileTree.interfaces.TreeDifference;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import rest.RestClient;
import threads.interfaces.Thread;
import threads.models.ThreadManager;
import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static fileTree.constants.TreeControlConstants.*;
import static tools.TreeTool.*;

public class TreeControl {
    private Tree gob_tree;
    private TreeView<String> gob_treeView;
    private ContextMenu gob_contextMenu;
    private RestClient gob_restClient;
    private MainController gob_mainController;

    public TreeControl(String iva_ip, String iva_port, MainController iob_mainController) {
        int lva_loopIndex = 0;

        gob_mainController = iob_mainController;

        File lob_rootDirectory = new File(Utils.getUserBasePath());
        File lob_serverDirectory = new File(Utils.getUserBasePath() + "\\" + iva_ip + "_" + iva_port);
        File lob_userDirectory = new File(lob_serverDirectory.getAbsolutePath() + "\\" + DataCache.getDataCache().get(DataCache.GC_EMAIL_KEY));
        File lob_publicDirectory = new File(lob_userDirectory.getAbsolutePath() + "\\Public");
        File lob_privateDirectory = new File(lob_userDirectory.getAbsolutePath() + "\\Private");
        File lob_sharedDirectories = new File(lob_userDirectory.getAbsolutePath() + "\\Shared");

        gob_restClient = RestClientBuilder.buildRestClientWithAuth();

        //create the root directory if it does not exist
        TreeTool.getInstance().createDirectory(lob_rootDirectory);

        //create the server directory if it does not exist
        TreeTool.getInstance().createDirectory(lob_serverDirectory);

        //create the user directory if it does not exist
        TreeTool.getInstance().createDirectory(lob_userDirectory);

        //create the public directory
        TreeTool.getInstance().createDirectory(lob_publicDirectory);

        //create the private directory
        TreeTool.getInstance().createDirectory(lob_privateDirectory);

        //create the shared directory
        TreeTool.getInstance().createDirectory(lob_sharedDirectories);

        try {
            TreeSingleton.setTreeRootPath(lob_userDirectory.getCanonicalPath());
            gob_tree = TreeSingleton.getInstance().getTree();
            gob_treeView = TreeSingleton.getInstance().getTreeView();
            gob_treeView.setShowRoot(false);

            TreeItem<String> lob_root = new TreeItem<>(gob_tree.getRoot().getName());
            lob_root.setGraphic(TreeTool.getInstance().getTreeIcon(gob_tree.getRoot().getCanonicalPath()));
            gob_treeView.setRoot(lob_root);
            addFilesToTree(gob_tree.getRoot(), gob_treeView.getRoot());
            Collection<TreeDifference> lco_differences = gob_restClient.compareClientAndServerTree(gob_tree);
            for (TreeDifference lob_difference : lco_differences) {
                for (String lva_addFile : lob_difference.getFilesToInsert()) {
                    if (lva_loopIndex == 1) {
                        lva_addFile = "Public" + lva_addFile;
                    }

                    File lob_newFile = gob_restClient.downloadFile(lva_addFile);
                    if (lob_newFile != null) {
                        //add to private directory
                        TreeTool.getInstance().addToTreeView(lob_newFile);
                        gob_tree.addFile(lob_newFile, lob_newFile.isDirectory());
                    }
                }

                for (String lva_deleteFile : lob_difference.getFilesToDelete()) {

                    if (lva_loopIndex == 0) {
                        lva_deleteFile = gob_tree.getRoot().getAbsolutePath() + "\\Private" + lva_deleteFile;
                    }

                    if (lva_loopIndex == 1) {
                        lva_deleteFile = gob_tree.getRoot().getAbsolutePath() + "\\Public" + lva_deleteFile;
                    }

                    File lob_file = new File(lva_deleteFile);
                    gob_tree.deleteFile(lva_deleteFile);
                    TreeItem<String> lob_item = TreeTool.getTreeItem(lob_file);
                    lob_item.getParent().getChildren().remove(lob_item);
                }
                lva_loopIndex++;
            }
            buildContextMenu();
            gob_treeView.setContextMenu(gob_contextMenu);
            gob_treeView.setOnContextMenuRequested(event ->
                    onContextMenuRequest()
            );

            gob_treeView.setEditable(true);
            Collection<File> lob_directoriesToWatch = gob_tree.getAllDirectories();
            lob_directoriesToWatch.clear();
            lob_directoriesToWatch.add(gob_tree.getRoot());
            Thread lob_watcher = ThreadManager.getDirectoryWatcherThread(gob_restClient, gob_tree.getRoot());
            lob_watcher.start();
            gob_treeView.setEditable(true);

            gob_treeView.setCellFactory(siTreeView ->
                    new TreeCellImpl(this.gob_tree, this.gob_restClient)
            );

            gob_treeView.setOnEditCommit(event -> {
                try {
                    File lob_renamedFile = buildFileFromItem(event.getTreeItem(), gob_tree);
                    System.out.println(lob_renamedFile.toPath());
                    gob_tree.renameFile(lob_renamedFile, event.getNewValue());
                    TreeSingleton.getInstance().getDuplicateOperationsPrevention().putRenamed(lob_renamedFile.toPath());
                    String lva_relativePath = TreeTool.getRelativePath(lob_renamedFile.getCanonicalPath());
                    gob_restClient.renameFile(lva_relativePath, event.getNewValue());
                } catch (IOException ex) {
                    ex.printStackTrace();
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

    private void addFilesToTree(File iob_file, TreeItem<String> iob_treeItem) {
        TreeItem<String> lob_newItem;

        //we have to add the child nodes of the file if the file is a direcotry
        if (iob_file.isDirectory()) {
            //add the directory itself
            if (!addFile(iob_file, true)) {
                return;
            }

            File[] lob_fileList = iob_file.listFiles();
            if (lob_fileList != null) {
                //add all files in the directory
                for (File lob_directoryChildFile : lob_fileList) {
                    //if the directory contains another directory, do the same for it
                    if (lob_directoryChildFile.isDirectory()) {
                        if (!TreeTool.filterRootFiles(lob_directoryChildFile.toPath())) {
                            lob_newItem = TreeTool.getInstance().addTreeItem(iob_treeItem, lob_directoryChildFile);
                            addFilesToTree(lob_directoryChildFile, lob_newItem);
                        }
                    } else {
                        //add normal file
                        if (addFile(lob_directoryChildFile, false)) {
                            TreeTool.getInstance().addTreeItem(iob_treeItem, lob_directoryChildFile);
                        }
                    }
                }
            }
        } else {
            if (addFile(iob_file, false)) {
                TreeTool.getInstance().addTreeItem(iob_treeItem, iob_file);
            }
        }
    }

    private boolean addFile(File iob_file, boolean iva_isDirectory) {
        if (TreeTool.filterRootFiles(iob_file.toPath())) {
            return false;
        }
        this.gob_tree.addFile(iob_file, iva_isDirectory);
        return true;
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
            TreeItem<String> lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
            if (deleteFile(lob_selectedFile, lob_selectedItem, gob_restClient)) {
                System.out.println("GELÖSCHT");
                addAllDeleted(lob_selectedFile);
            }
        });

        lob_newDirectory = new MenuItem("New Directory");
        lob_newDirectory.setOnAction(event ->
                createNewDirectory()
        );

        lob_deleteDirectoryOnly = new MenuItem("Delete only Directory");
        lob_deleteDirectoryOnly.setOnAction(event ->
                deleteDirectoryOnly()
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
                    if (!lob_selectedFile.isDirectory() || isRootChildElement(lob_treeItem)) {
                        lob_item.setDisable(true);
                    } else {
                        lob_item.setDisable(false);
                    }
                    break;

                case GC_MENU_ITEM_DELETE:
                    if (isRootChildElement(lob_treeItem)) {
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

        if (iob_file.isDirectory()) {
            for (File file : iob_file.listFiles()) {
                addAllDeleted(file);
            }
        }
    }

    private void addAllMovedOrRenamed(File iob_file) {
        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putMoved(iob_file.toPath());

        if (iob_file.isDirectory()) {
            for (File file : iob_file.listFiles()) {
                addAllDeleted(file);
            }
        }
    }

    private void deleteDirectoryOnly() {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromItem(
                gob_treeView.getSelectionModel().getSelectedItem(), gob_tree
        );
        String lva_relativePath;
        //--------------------------------------------------------------------------------

        try {
            TreeItem<String> lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
            lva_relativePath = getRelativePath(lob_selectedFile.getCanonicalPath());

            for (File lob_child : lob_selectedFile.listFiles()) {
                addAllMovedOrRenamed(lob_child);
            }

            if (!gob_tree.deleteDirectoryOnly(lob_selectedFile)) {
                return;
            }

            TreeItem<String> lob_parentItem = lob_selectedItem.getParent();
            lob_parentItem.getChildren().addAll(lob_selectedItem.getChildren());
            lob_parentItem.getChildren().remove(lob_selectedItem);

            TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(lob_selectedFile.toPath());
            gob_restClient.deleteDirectoryOnly(lva_relativePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createNewDirectory() {
        File lob_newFile = buildNewFile("\\Neuer Ordner$");
        createFileOrDirectory(lob_newFile, true, gob_restClient);
        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(lob_newFile.toPath());
    }

    private void createNewFile() {
        File lob_newFile = buildNewFile("\\Neue Datei$.txt");
        createFileOrDirectory(lob_newFile, false, gob_restClient);
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
                lob_controller.initData(null, lob_stage);

            } else {
                // TODO shared Directory übergeben
                //lob_controller.initData(null, lob_stage);
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

            if (lob_file.isFile()) {
                lob_treeItem = lob_treeItem.getParent();
            }

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
}