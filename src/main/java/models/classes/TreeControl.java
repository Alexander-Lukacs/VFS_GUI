package models.classes;

import fileTree.interfaces.Tree;
import fileTree.models.TreeImpl;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tools.Utils;

import java.io.File;
import java.io.IOException;

import static models.constants.TreeControlConstants.*;

public class TreeControl {



    private Tree gob_tree;
    private TreeView<String> gob_treeView;
    private ContextMenu gob_contextMenu;

    public TreeControl(TreeView<String> iob_treeView, String iva_ip, String iva_port) {
        this.gob_treeView = iob_treeView;
        File lob_rootDirectory = new File(Utils.getUserBasePath());
        File lob_serverDirectory = new File(Utils.getUserBasePath() + "\\" + iva_ip + "_" + iva_port);

        //create the root directory if it does not exist
        if (!lob_rootDirectory.exists() || !lob_rootDirectory.isDirectory()) {
            lob_rootDirectory.mkdir();
        }

        //create the server directory if it does not exist
        if (!lob_serverDirectory.exists() || !lob_serverDirectory.isDirectory()) {
            lob_serverDirectory.mkdir();
        }

        try {
            gob_tree = new TreeImpl(lob_serverDirectory.getCanonicalPath());
            TreeItem<String> lob_root = new TreeItem<>(gob_tree.getRoot().getName());
            lob_root.setGraphic(getTreeIcon(GC_DIRECTORY_ICON));
            gob_treeView.setRoot(lob_root);
            addFilesToTree(gob_tree.getRoot(), gob_treeView.getRoot());
            buildContextMenu();
            gob_treeView.setContextMenu(gob_contextMenu);
            gob_treeView.setOnContextMenuRequested(event ->
                    onContextMenuRequest()
            );

            gob_treeView.setEditable(true);

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
                        lob_newItem = addTreeItem(iob_treeItem, lob_directoryChildFile);

                        addFilesToTree(lob_directoryChildFile, lob_newItem);
                    } else {
                        //add normal file
                        addFile(lob_directoryChildFile, false);
                        addTreeItem(iob_treeItem, lob_directoryChildFile);
                    }
                }
            }
        } else {
            addFile(iob_file, false);
            addTreeItem(iob_treeItem, iob_file);
        }
    }

    private void addFile(File iob_file, boolean iva_isDirectory) {
        this.gob_tree.addFile(iob_file, iva_isDirectory);
    }

    private TreeItem<String> addTreeItem(TreeItem<String> iob_parent, File iob_file) {
        String lva_fileType;
        TreeItem<String> rob_child = new TreeItem<>(iob_file.getName());
        if (iob_file.isDirectory()) {
            rob_child.setGraphic(getTreeIcon(GC_DIRECTORY_ICON));
        } else {
            lva_fileType = iob_file.getName().replaceFirst(".*\\.","");
            rob_child.setGraphic(getTreeIcon(lva_fileType));
        }

        iob_parent.getChildren().add(rob_child);
        return rob_child;
    }

    private Node getTreeIcon(String iva_iconName) {
        ImageView rob_imageView;
        switch (iva_iconName) {
            case GC_DIRECTORY_ICON:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_DIR.png")));
                break;
            case GC_TEXT_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_TXT.png")));
                break;
            case GC_EXCEL_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_EXCEL.png")));
                break;
            case GC_PDF_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_PDF.png")));
                break;
            case GC_WORD_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_WORD.png")));
                break;
            case GC_XML_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_XML.png")));
                break;
            default:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("ICON_FILE.png")));
        }

        return rob_imageView;
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
        //--------------------------------------------------------------------------------

        TreeItem<String> lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
        lob_selectedItem.getParent().getChildren().remove(lob_selectedItem);
        gob_tree.deleteFile(lob_selectedFile);
        gob_treeView.refresh();
    }

    private void deleteDirectoryOnly() {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromSelectedItem();
        //--------------------------------------------------------------------------------

        TreeItem<String> lob_selectedItem = gob_treeView.getSelectionModel().getSelectedItem();
        TreeItem<String> lob_parentItem = lob_selectedItem.getParent();
        lob_parentItem.getChildren().addAll(lob_selectedItem.getChildren());
        lob_parentItem.getChildren().remove(lob_selectedItem);
        gob_tree.deleteDirectoryOnly(lob_selectedFile);
        gob_treeView.refresh();
    }

    private void createNewDirectory() {
        createFileOrDirectory("\\neuer Ordner$", true);
    }

    private void createNewFile() {
        createFileOrDirectory("\\neue Datei$.txt", false);
    }

    private void createFileOrDirectory(String iva_name, boolean isDirectory) {
        //-------------------------------Variables----------------------------------------
        File lob_selectedFile = buildFileFromSelectedItem();
        File lob_newFile;
        String lva_newFilePath;
        int lva_counter = 1;
        TreeItem<String> lob_selectedItem;
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

            System.out.println(lob_newFile.getCanonicalPath());
            if (isDirectory) {
                gob_tree.addFile(lob_newFile, true);
            } else {
                gob_tree.addFile(lob_newFile, false);
            }

            addTreeItem(lob_selectedItem, lob_newFile);
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
}