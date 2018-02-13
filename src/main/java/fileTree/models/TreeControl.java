package fileTree.models;

import builder.RestClientBuilder;
import cache.DataCache;
import client.RestClient;
import fileTree.interfaces.FileChangeListener;
import fileTree.interfaces.Tree;
import javafx.scene.control.*;
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
        DataCache lob_dataCache = DataCache.getDataCache();
        gob_restClient = RestClientBuilder.buildRestClientWithAuth(lob_dataCache.get(DataCache.GC_IP_KEY),
                lob_dataCache.get(DataCache.GC_PORT_KEY),
                lob_dataCache.get(DataCache.GC_EMAIL_KEY),
                lob_dataCache.get(DataCache.GC_PASSWORD_KEY));

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

            NewWatchService w = new NewWatchService(gob_tree.getRoot().toPath(), new FileChangeListener() {
                @Override
                public void fileAdded(Path iob_path) {
                    boolean lob_isDirectory = iob_path.toFile().isDirectory();
                    System.out.println("fileAdded: " + iob_path);
                    TreeTool.getInstance().addToTreeView(iob_path.toFile());
                    TreeSingleton.getInstance().getTree().addFile(iob_path.toFile(), lob_isDirectory);
                    try {
                        if (iob_path.toFile().isDirectory()) {
                            gob_restClient.createDirectoryOnServer(getRelativePath(iob_path.toString()));
                        } else {
                            gob_restClient.uploadFilesToServer(iob_path.toFile(), getRelativePath(iob_path.toString()));
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void fileDeleted(Path iob_path) {
                    System.out.println("fileDeleted: " + iob_path);
                    try {
                        String[] test = getRelativePath(iob_path.toString()).split("\\\\");
                        int counter = 0;
                        TreeItem<String> item = TreeSingleton.getInstance().getTreeView().getRoot();

                        while (counter < test.length) {
                            for (TreeItem<String> lob_child : item.getChildren()) {
                                if (lob_child.getValue().equals(test[counter])) {
                                    item = lob_child;
                                    break;
                                }
                            }
                            counter++;
                        }
                        TreeItem<String> parent = item.getParent();
                        parent.getChildren().remove(item);

                        gob_restClient.deleteOnServer(getRelativePath(iob_path.toString()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void renamedOrMoved(Path iob_oldPath, Path iob_newPath) {
                    System.out.println("renamedOrMoved: " + iob_oldPath + " TO " + iob_newPath);
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
        lob_deleteFile.setOnAction(event ->
            deleteFile()
        );

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
        File lob_selectedFile = buildFileFromSelectedItem();
        //--------------------------------------------------------------------------------

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

    private void deleteFile() {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromSelectedItem();
        String lva_relativePath;
        //--------------------------------------------------------------------------------
        try {
            lva_relativePath = getRelativePath(lob_selectedFile.getCanonicalPath());
            gob_restClient.deleteOnServer(lva_relativePath);

            TreeItem<String> lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
            lob_selectedItem.getParent().getChildren().remove(lob_selectedItem);
            addAllDeleted(lob_selectedFile);
            gob_tree.deleteFile(lob_selectedFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void addAllDeleted(File iob_file) {
        TreeSingleton.getInstance().getDuplicateFilePrevention().putDeleted(iob_file.toPath());

        if (iob_file.isDirectory()) {
            for (File file : iob_file.listFiles()) {
                addAllDeleted(file);
            }
        }
    }

    private void addAllMovedOrRenamed(File iob_file) {
        TreeSingleton.getInstance().getDuplicateFilePrevention().putRenamedOrMove(iob_file.toPath());

        if (iob_file.isDirectory()) {
            for (File file : iob_file.listFiles()) {
                addAllDeleted(file);
            }
        }
    }

    private void deleteDirectoryOnly() {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromSelectedItem();
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

            TreeSingleton.getInstance().getDuplicateFilePrevention().putDeleted(lob_selectedFile.toPath());
            gob_restClient.deleteDirectoryOnly(lva_relativePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createNewDirectory() {
        createFileOrDirectory("\\Neuer Ordner$", true);
    }

    private void createNewFile() {
        createFileOrDirectory("\\Neue Datei$.txt", false);
    }

    private void createFileOrDirectory(String iva_name, boolean isDirectory) {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromSelectedItem();
        File lob_newFile;
        String lva_newFilePath;
        int lva_counter = 1;
        TreeItem<String> lob_selectedItem;
        String lva_relativeFilePath;
        //--------------------------------------------------------------------------------
        try {
            lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
            if (lob_selectedFile.isFile()) {
                lva_newFilePath = lob_selectedFile.getParentFile().getCanonicalPath();
                lob_selectedItem = lob_selectedItem.getParent();
            } else {
                lva_newFilePath = lob_selectedFile.getCanonicalPath();
            }

            lva_newFilePath += iva_name;
            lob_newFile = new File(lva_newFilePath.replaceFirst("\\$", ""));

            if (lob_newFile.exists()) {
                do {
                    lob_newFile = new File(lva_newFilePath.replaceFirst("\\$", "(" + lva_counter + ")"));
                    lva_counter++;
                } while (lob_newFile.exists());
            }

            lva_relativeFilePath = getRelativePath(lob_newFile.getCanonicalPath());

            TreeSingleton.getInstance().getDuplicateFilePrevention().putCreated(lob_newFile.toPath());

            if (isDirectory) {
                gob_tree.addFile(lob_newFile, true);
                gob_restClient.createDirectoryOnServer(lva_relativeFilePath);
            } else {
                gob_tree.addFile(lob_newFile, false);
                gob_restClient.uploadFilesToServer(lob_newFile, lva_relativeFilePath);
            }


            TreeTool.getInstance().addTreeItem(lob_selectedItem, lob_newFile);
            gob_treeView.refresh();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private File buildFileFromSelectedItem() {
        TreeItem<String> lob_treeItem = gob_treeView.getSelectionModel().getSelectedItem();
        StringBuilder lob_path = new StringBuilder();

        while (lob_treeItem != null) {
            lob_path.insert(0, lob_treeItem.getValue());
            lob_path.insert(0, "\\");
            lob_treeItem = lob_treeItem.getParent();
        }
        lob_path.insert(0, Utils.getUserBasePath());
        return gob_tree.getFile(lob_path.toString());
    }

    private String getRelativePath(String iva_filePath) throws IOException {
        String lva_regex = gob_tree.getRoot().getCanonicalPath();
        lva_regex = lva_regex.replaceAll("\\\\", "\\\\\\\\");
        return iva_filePath.replaceFirst(lva_regex + "\\\\", "");
    }
}