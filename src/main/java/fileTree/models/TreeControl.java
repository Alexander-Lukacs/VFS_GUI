package fileTree.models;

import builder.RestClientBuilder;
import fileTree.interfaces.FileChangeListener;
import fileTree.interfaces.Tree;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import rest.RestClient;
import tools.TreeTool;
import tools.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class TreeControl {
    private Tree gob_tree;
    private TreeView<String> gob_treeView;
    private ContextMenu gob_contextMenu;
    private RestClient gob_restClient;

    public TreeControl(String iva_ip, String iva_port) {
        File lob_rootDirectory = new File(Utils.getUserBasePath());
        File lob_serverDirectory = new File(Utils.getUserBasePath() + "\\" + iva_ip + "_" + iva_port);

        gob_restClient = RestClientBuilder.buildRestClientWithAuth();

        //create the root directory if it does not exist
        if (!lob_rootDirectory.exists() || !lob_rootDirectory.isDirectory()) {
            lob_rootDirectory.mkdir();
        }

        //create the server directory if it does not exist
        if (!lob_serverDirectory.exists() || !lob_serverDirectory.isDirectory()) {
            lob_serverDirectory.mkdir();
        }

        try {
            TreeSingleton.setTreeRootPath(lob_serverDirectory.getCanonicalPath());
            gob_tree = TreeSingleton.getInstance().getTree();
            gob_treeView = TreeSingleton.getInstance().getTreeView();

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
                        moveFile(iob_oldPath, iob_newPath.getParent(), true);
                    }
                }

                @Override
                public void fileRenamed(Path iob_path, String iva_newName) {
                    try {
                        System.out.println("RENAMED: " + iob_path + " TO: " + iva_newName);
                        TreeSingleton.getInstance().getTree().renameFile(iob_path.toFile(), iva_newName);
                        TreeItem<String> lob_item = TreeTool.getInstance().getTreeItem(iob_path.toFile());
                        lob_item.setValue(iva_newName);

                        String lva_relativePath = TreeTool.getInstance().getRelativePath(iob_path.toString());

                        gob_restClient.renameFile(lva_relativePath, iva_newName);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            gob_treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
                @Override
                public TreeCell<String> call(TreeView<String> siTreeView) {
                    TreeCell<String> lob_cell = new TreeCell<String>() {
                        @Override
                        protected void updateItem(String iva_item, boolean iva_empty) {
                            super.updateItem(iva_item, iva_empty);

                            if (!iva_empty) {
                                setGraphic(getTreeItem().getGraphic());
                                setText(getItem() == null ? "" : getItem());
                            } else {
                                setText(null);
                                setGraphic(null);
                            }
                        }
                    };

                    lob_cell.setOnDragDetected(event -> {
                        TreeItem<String> lob_selectedItem = lob_cell.getTreeItem();
                        Dragboard lob_dragBoard;
                        ClipboardContent lob_content;

                        if (lob_selectedItem != null) {
                            lob_dragBoard = lob_cell.startDragAndDrop(TransferMode.MOVE);
                            lob_content = new ClipboardContent();
                            lob_content.putString(lob_selectedItem.getValue());
                            lob_dragBoard.setContent(lob_content);
                        }

                        event.consume();
                    });

                    lob_cell.setOnDragDropped(event -> {
                        TreeItem<String> treeItemHovered = lob_cell.getTreeItem();
                        TreeCell b = (TreeCell) event.getGestureSource();
                        TreeItem treeItemDragged = b.getTreeItem();

                        File fileHovered = buildFileFromItem(treeItemHovered);
                        File fileDragged = buildFileFromItem(treeItemDragged);
                        moveFile(fileDragged.toPath(), fileHovered.toPath(), false);
                        TreeSingleton.getInstance().getDuplicateOperationsPrevention().putMoved(fileDragged.toPath());

                        gob_treeView.getSelectionModel().select(treeItemHovered);
                        event.setDropCompleted(true);
                        event.consume();
                    });

                    // -------------------------------------------------------------------------------------------------
                    // Drag Animations
                    // -------------------------------------------------------------------------------------------------

                    lob_cell.setOnDragEntered(event -> {
                        TreeItem<String> lob_selectedItem = lob_cell.getTreeItem();
                        Object lva_cellDraggedValue;
                        Object lva_cellHoveredValue;
                        TreeCell lob_cellDragged;

                        if (lob_selectedItem != null && !buildFileFromItem(lob_cell.getTreeItem()).isFile() &&
                                event.getGestureSource() != lob_cell) {

                            lob_cellDragged = (TreeCell) event.getGestureSource();
                            lva_cellDraggedValue = lob_cellDragged.getTreeItem().getParent().getValue();
                            lva_cellHoveredValue = lob_cell.getTreeItem().getValue();

                            if (lva_cellDraggedValue != lva_cellHoveredValue) {
                                lob_cell.setStyle("-fx-background-color: powderblue;");
                            }
                        }

                        event.consume();
                    });

                    lob_cell.setOnDragOver(event -> {
                        TreeItem<String> lob_selectedItem = lob_cell.getTreeItem();
                        Object lva_cellDraggedValue;
                        Object lva_cellHoveredValue;
                        TreeCell lob_cellDragged;

                        if (lob_selectedItem != null && event.getGestureSource() != lob_cell &&
                                !buildFileFromItem(lob_cell.getTreeItem()).isFile()) {

                            lob_cellDragged = (TreeCell) event.getGestureSource();
                            lva_cellDraggedValue = lob_cellDragged.getTreeItem().getParent().getValue();
                            lva_cellHoveredValue = lob_cell.getTreeItem().getValue();

                            if (lva_cellDraggedValue != lva_cellHoveredValue) {
                                event.acceptTransferModes(TransferMode.MOVE);
                            }
                        }

                        event.consume();
                    });

                    lob_cell.setOnDragExited(event -> {
                        lob_cell.setStyle("-fx-background-color: white");
                        lob_cell.setStyle("-fx-focus-color: black");

                        event.consume();
                    });

                    return lob_cell;
                }
            });

            w.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        //-----------------------------------------------------------------

        gob_contextMenu = new ContextMenu();
        lob_deleteFile = new MenuItem("Delete");
        lob_deleteFile.setOnAction(event -> {
            File lob_selectedFile = buildFileFromItem(
                    gob_treeView.getSelectionModel().getSelectedItem()
            );
            TreeItem<String> lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
            if (deleteFile(lob_selectedFile, lob_selectedItem)) {
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
        gob_contextMenu.getItems().addAll(lob_deleteFile,
                lob_newDirectory,
                lob_newFile,
                lob_deleteDirectoryOnly,
                lob_renameFile);
    }

    private void onContextMenuRequest() {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromItem(
                gob_treeView.getSelectionModel().getSelectedItem()
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
            } else {
                lob_item.setDisable(false);
            }
        }
    }

    private void renameFile() {

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
                return false;
            }

            iob_itemToDelete.getParent().getChildren().remove(iob_itemToDelete);
            return gob_tree.deleteFile(iob_file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void moveFile(Path iob_path, Path iob_destination, boolean iva_moveJustInTree) {
        //-------------------------Variables------------------------------------
        TreeTool lob_tool = TreeTool.getInstance();
        TreeItem<String> lob_item = lob_tool.getTreeItem(iob_path.toFile());
        TreeItem<String> lob_parent = TreeTool.getInstance().getTreeItem(iob_destination.toFile());
        //----------------------------------------------------------------------
        try {
            lob_tool.removeFromTreeView(iob_path.toFile());
            lob_parent.getChildren().add(lob_item);

            String lva_destination = iob_destination.toString();
            TreeSingleton.getInstance().getTree().moveFile(iob_path.toFile(), lva_destination, iva_moveJustInTree);

            String lva_oldRelativePath = TreeTool.getInstance().getRelativePath(iob_path.toString());
            String lva_newRelativePath = TreeTool.getInstance().getRelativePath(lva_destination + "\\");
            gob_restClient.moveFile(lva_oldRelativePath, lva_newRelativePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
                gob_treeView.getSelectionModel().getSelectedItem()
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
                gob_treeView.getSelectionModel().getSelectedItem()
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
        File lob_newFile;
        String lva_newFilePath;
        int lva_counter = 1;
        String lva_relativeFilePath;
        //--------------------------------------------------------------------
        try {
//            if (iob_newFile.isDirectory()) {
//                lva_newFilePath = iob_newFile.getCanonicalPath();
//            } else {
//                lva_newFilePath = iob_newFile.getParentFile().getCanonicalPath();
//            }

//            lva_newFilePath += iva_name;
//            lob_newFile = new File(lva_newFilePath.replaceFirst("\\$", ""));

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

    private File buildFileFromItem(TreeItem<String> iob_treeItem) {
        StringBuilder lob_path = new StringBuilder();

        while (iob_treeItem != null) {
            lob_path.insert(0, iob_treeItem.getValue());
            lob_path.insert(0, "\\");
            iob_treeItem = iob_treeItem.getParent();
        }
        lob_path.insert(0, Utils.getUserBasePath());
        gob_tree.getFile("hier_pfad");
        return gob_tree.getFile(lob_path.toString());

    }

    /**
     * Returns the path of the current selected treeItem
     * If the selected is a file, the parent directory path gets returned
     * If nothing is selected return null
     * @return path of selected treeItem
     */
    public String getPathOfSelectedItem() {
        TreeItem<String> lob_treeItem = gob_treeView.getSelectionModel().getSelectedItem();
        File lob_file;

        if (lob_treeItem != null) {
            lob_file = buildFileFromItem(lob_treeItem);

            if (lob_file.isFile()) {
                lob_treeItem = lob_treeItem.getParent();
            }

            lob_file = buildFileFromItem(lob_treeItem);

            return lob_file.toPath().toString();
        }

        return null;
    }
}