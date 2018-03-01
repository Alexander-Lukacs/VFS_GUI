package fileTree.models;

import builder.RestClientBuilder;
import cache.DataCache;
import controller.SharedDirectoryController;
import fileTree.interfaces.FileChangeListener;
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
import rest.RestClient;
import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static tools.TreeTool.buildFileFromItem;
import static tools.TreeTool.moveFile;

public class TreeControl {
    private Tree gob_tree;
    private TreeView<String> gob_treeView;
    private ContextMenu gob_contextMenu;
    private RestClient gob_restClient;

    public TreeControl(String iva_ip, String iva_port) {
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
            buildContextMenu();
            gob_treeView.setContextMenu(gob_contextMenu);
            gob_treeView.setOnContextMenuRequested(event ->
                    onContextMenuRequest()
            );

            gob_treeView.setEditable(true);
            Collection<File> lob_directoriesToWatch = gob_tree.getAllDirectories();
            lob_directoriesToWatch.clear();
            lob_directoriesToWatch.add(gob_tree.getRoot());

            DirectoryWatchService w = new DirectoryWatchService(gob_tree.getRoot().toPath(), new FileChangeListener() {
                @Override
                public void fileAdded(Path iob_path) {
                    boolean lva_isDirectory = iob_path.toFile().isDirectory();
                    System.out.println("fileAdded: " + iob_path);
                    createFileOrDirectory(iob_path.toFile(), lva_isDirectory);
                }

                @Override
                public void fileDeleted(Path iob_path) {
                    if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileDeted(iob_path)) {
                        TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeDeleted(iob_path);
                    } else {
                        TreeItem<String> lob_itemToDelete = TreeTool.getInstance().getTreeItem(iob_path.toFile());
                        deleteFile(iob_path.toFile(), lob_itemToDelete);
                    }
                }

                @Override
                public void fileMoved(Path iob_oldPath, Path iob_newPath) {
                    if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFilesMoved(iob_oldPath)) {
                        TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeMoved(iob_oldPath);
                    } else {
                        moveFile(iob_oldPath, iob_newPath.getParent(), true, gob_restClient);
                    }
                }

                @Override
                public void fileRenamed(Path iob_path, String iva_newName) {
                    try {
                        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileRenamed(iob_path)) {
                            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeRenamed(iob_path);
                        } else {
                            System.out.println("RENAMED: " + iob_path + " TO: " + iva_newName);
                            TreeSingleton.getInstance().getTree().renameFile(iob_path.toFile(), iva_newName);
                            TreeItem<String> lob_item = TreeTool.getInstance().getTreeItem(iob_path.toFile());
                            lob_item.setValue(iva_newName);

                            String lva_relativePath = TreeTool.getInstance().getRelativePath(iob_path.toString());

                            gob_restClient.renameFile(lva_relativePath, iva_newName);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
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
                    String lva_relativePath = TreeTool.getInstance().getRelativePath(lob_renamedFile.getCanonicalPath());
                    gob_restClient.renameFile(lva_relativePath, event.getNewValue());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            w.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        gob_restClient.compareClientAndServerTree(TreeSingleton.getInstance().getTree());
    }

    private void addFilesToTree(File iob_file, TreeItem<String> iob_treeItem) {
        TreeItem<String> lob_newItem;

        //we have to add the child nodes of the file if the file is a direcotry
        if (iob_file.isDirectory()) {
            //add the directory itself
            addFile(iob_file, true);

            File[] lob_fileList = iob_file.listFiles();
            if (lob_fileList != null) {
                //add all files in the directory
                for (File lob_directoryChildFile : lob_fileList) {
                    //if the directory contains another directory, do the same for it
                    if (lob_directoryChildFile.isDirectory()) {
                        lob_newItem = TreeTool.getInstance().addTreeItem(iob_treeItem, lob_directoryChildFile);

                        addFilesToTree(lob_directoryChildFile, lob_newItem);
                    } else {
                        //add normal file
                        addFile(lob_directoryChildFile, false);
                        TreeTool.getInstance().addTreeItem(iob_treeItem, lob_directoryChildFile);
                    }
                }
            }
        } else {
            addFile(iob_file, false);
            TreeTool.getInstance().addTreeItem(iob_treeItem, iob_file);
        }
    }

    private void addFile(File iob_file, boolean iva_isDirectory) {
        this.gob_tree.addFile(iob_file, iva_isDirectory);
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
            if (deleteFile(lob_selectedFile, lob_selectedItem)) {
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
        File lob_selectedFile = buildFileFromItem(
                gob_treeView.getSelectionModel().getSelectedItem(), gob_tree
        );
        //--------------------------------------------------------------------------------

        if (gob_treeView.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        for (MenuItem lob_item : gob_contextMenu.getItems()) {
            if (!lob_selectedFile.isDirectory() && lob_item.getText().equals("Delete only Directory")) {
                lob_item.setDisable(true);
            } else if (gob_tree.getRoot().equals(lob_selectedFile) && lob_item.getText().equals("Delete")) {
                lob_item.setDisable(true);
            } else if (gob_tree.getRoot().equals(lob_selectedFile) && lob_item.getText().equals("Rename")) {
                lob_item.setDisable(true);
            } else if (gob_tree.getRoot().equals(lob_selectedFile) && lob_item.getText().equals("Delete only Directory")) {
                lob_item.setDisable(true);
            } else if (lob_item.getText().equals("New shared directory") || lob_item.getText().equals("Properties")) {
                if (lob_selectedFile.getName().equals("Shared")) {
                    lob_item.setDisable(false);
                    lob_item.setText("New shared directory");
                } else if (lob_selectedFile.getParentFile().getName().equals("Shared")){
                    lob_item.setDisable(false);
                    lob_item.setText("Properties");
                } else {
                    lob_item.setDisable(true);
                }


            } else {
                lob_item.setDisable(false);
            }
        }
    }

    private void renameFile() {
        gob_treeView.edit(gob_treeView.getSelectionModel().getSelectedItem());
    }

    /**
     * delete a file on the client, explorer and server
     *
     * @param iob_file         file to delete
     * @param iob_itemToDelete item to delete in the tree
     * @return true if all files were deleted, otherwise false
     */
    private boolean deleteFile(File iob_file, TreeItem<String> iob_itemToDelete) {
        //-------------------------------Variables----------------------------------------
        String lva_relativePath;
        //--------------------------------------------------------------------------------
        try {
            lva_relativePath = TreeTool.getInstance().getRelativePath(iob_file.getCanonicalPath());
            if (!gob_restClient.deleteOnServer(lva_relativePath)) {
                //return false;
            }

            iob_itemToDelete.getParent().getChildren().remove(iob_itemToDelete);
            return gob_tree.deleteFile(iob_file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
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
            lva_relativePath = TreeTool.getInstance().getRelativePath(lob_selectedFile.getCanonicalPath());

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
        createFileOrDirectory(lob_newFile, true);
        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putCreated(lob_newFile.toPath());
    }

    private void createNewFile() {
        File lob_newFile = buildNewFile("\\Neue Datei$.txt");
        createFileOrDirectory(lob_newFile, false);
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

    private boolean createFileOrDirectory(File iob_newFile, boolean isDirectory) {
        //-------------------------------Variables----------------------------
        String lva_relativeFilePath;
        //--------------------------------------------------------------------
        try {

            lva_relativeFilePath = TreeTool.getInstance().getRelativePath(iob_newFile.getCanonicalPath());

            gob_tree.addFile(iob_newFile, isDirectory);
            TreeTool.getInstance().addToTreeView(iob_newFile);
            if (isDirectory) {
                if (!gob_restClient.createDirectoryOnServer(lva_relativeFilePath)) {
                    return false;
                }
            } else {
                if (!gob_restClient.uploadFilesToServer(iob_newFile, lva_relativeFilePath)) {
                    return false;
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    private void sharedDirectoryScene(TreeItem iob_treeItem) {
        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/sharedDirectoryScreen.fxml"));
        GridPane lob_pane = null;
        try {
            lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle("Shared Directory");
            lob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
            lob_stage.setResizable(false);
            lob_stage.setScene(lob_scene);
            SharedDirectoryController lob_controller = lob_loader.getController();

            // TODO shared Directory übergeben
            lob_controller.initData(null);
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
}