package models.classes;

import builder.RestClientBuilder;
import cache.DirectoryCache;
import cache.FileMapperCache;
import cache.SharedDirectoryCache;
import controller.classes.SharedDirectoryController;
import fileTree.classes.TreeSingleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import restful.clients.SharedDirectoryRestClient;
import threads.classes.ThreadManager;
import threads.constants.FileManagerConstants;
import tools.TreeTool;
import tools.xmlTools.DirectoryNameMapper;
import tools.xmlTools.FileMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static models.classes.FileService.readAllFilesFromDirectory;
import static models.constants.TreeControlVersionTwoConstants.*;
import static tools.TreeTool.buildFileFromItem;

public class TreeControlVersionTwo {

    public TreeControlVersionTwo() {
        init();
    }

    /**
     //     * Returns the path of the current selected treeItem
     //     * If the selected is a file, the parent directory path gets returned
     //     * If nothing is selected return null
     //     *
     //     * @return path of selected treeItem
     //     */
    public String getPathOfSelectedItem() {
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        TreeItem<String> lob_treeItem = lob_treeView.getSelectionModel().getSelectedItem();
        File lob_file;

        if (lob_treeItem != null) {
            lob_file = buildFileFromItem(lob_treeItem);
            return lob_file.toPath().toString();
        }

        return null;
    }


//----------------------------------------------------------------------------------------------------------------------
//context menu methods
//----------------------------------------------------------------------------------------------------------------------
    private void onContextMenuRequest() {
        //-------------------------------Variables----------------------------------------
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        TreeItem lob_treeItem = lob_treeView.getSelectionModel().getSelectedItem();
        File lob_selectedFile = buildFileFromItem(lob_treeItem);
        ContextMenu lob_contextMenu = lob_treeView.getContextMenu();
        //--------------------------------------------------------------------------------

        if (lob_treeView.getSelectionModel().getSelectedItem() == null) {
            return;
        }

        if (!lob_selectedFile.exists()) {
            return;
        }

        for (MenuItem lob_item : lob_contextMenu.getItems()) {
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

        private void createNewDirectory() {
        File lob_newFile = buildNewFile("\\new Directory$");
        ThreadManager.addCommandToFileManager(lob_newFile, FileManagerConstants.GC_ADD,
                true, true, true, 1);
            PreventDuplicateOperation.getDuplicateOperationPrevention().putCreated(lob_newFile.toPath());

    }

    private void createNewFile() {
        File lob_newFile = buildNewFile("\\new File$.txt");
        ThreadManager.addCommandToFileManager(lob_newFile, FileManagerConstants.GC_ADD,
                true, false, true, 1);

        PreventDuplicateOperation.getDuplicateOperationPrevention().putCreated(lob_newFile.toPath());

    }

    private void renameFile() {
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        lob_treeView.edit(lob_treeView.getSelectionModel().getSelectedItem());
    }

    private void sharedDirectoryScene(TreeItem iob_treeItem) {
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/sharedDirectoryScreen.fxml"));
        GridPane lob_pane;
        int lva_sharedDirectoryId;
        String lva_selectedItemName;
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        SharedDirectory lob_sharedDirectory;

        lva_selectedItemName = lob_treeView.getSelectionModel().getSelectedItem().getValue();

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
                        buildFileFromItem(lob_treeView.getSelectionModel().getSelectedItem()));
            }

            lob_stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//----------------------------------------------------------------------------------------------------------------------
//helper methods
//----------------------------------------------------------------------------------------------------------------------
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

    private boolean isRootChildElement(TreeItem iob_selectedItem) {
        return iob_selectedItem.getValue().equals(DirectoryNameMapper.getPrivateDirectoryName()) ||
                iob_selectedItem.getValue().equals(DirectoryNameMapper.getPublicDirectoryName()) ||
                iob_selectedItem.getValue().equals(DirectoryNameMapper.getSharedDirectoryName());
    }

    private File buildNewFile(String iva_name) {
        //-------------------------------Variables----------------------------
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        File lob_selectedFile = buildFileFromItem(
                lob_treeView.getSelectionModel().getSelectedItem()
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

//----------------------------------------------------------------------------------------------------------------------
//initialize methods
//----------------------------------------------------------------------------------------------------------------------

    /**
     * initialize all needed resources
     */
    private void init() {
        Collection<File> lco_files;
        //init all needed directories
        DirectoryCache.getDirectoryCache();

        initFileMapperCache();
        initTreeView();
        initSharedDirectoryCache();
        ThreadManager.getFileManagerThread().start();
        lco_files = FileService.readAllFilesFromDirectory(DirectoryCache.getDirectoryCache().getUserDirectory());

        for (File lob_file : lco_files) {
            ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_ADD, false, true, true, FileMapperCache.getFileMapperCache().get(lob_file.toPath()).getVersion());
        }
        ThreadManager.addCommandToFileManager(null, FileManagerConstants.GC_COMPARE_TREE, true);

        ThreadManager.getDirectoryWatcherThread(DirectoryCache.getDirectoryCache().getUserDirectory()).start();
    }


    private void initFileMapperCache() {
        Collection<File> lco_files = readAllFilesFromDirectory(DirectoryCache.getDirectoryCache().getUserDirectory());
        MappedFile lob_mappedFile;
        long lva_lastModified;
        FileMapperCache lob_fileMapperCache = FileMapperCache.getFileMapperCache();

        for (File lob_file : lco_files) {
            lob_mappedFile = FileMapper.getFile(lob_file.toPath().toString());
            try {
                lva_lastModified = Files.readAttributes(lob_file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis();
                if (lob_mappedFile.getFilePath() == null) {
                    lob_mappedFile = new MappedFile(lob_file.toPath(), 1, lva_lastModified);
                    lob_fileMapperCache.put(lob_mappedFile);
                } else {
                    if (lob_mappedFile.getLastModified() < lva_lastModified) {
                        lob_mappedFile.setLastModified(lva_lastModified);
                        lob_mappedFile.setVersion(lob_mappedFile.getVersion() + 1);
                    }
                    lob_fileMapperCache.put(lob_mappedFile);
                }
            } catch (IOException ignore) {

            }
        }
    }

    private void initTreeView() {
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        ContextMenu lob_contextMenu;
        lob_treeView.setPrefHeight(1080);
        lob_treeView.setShowRoot(false);
        lob_treeView.setEditable(true);

        TreeItem<String> lob_root = new TreeItem<>(DirectoryCache.getDirectoryCache().getUserDirectory().getName());
        lob_root.setGraphic(TreeTool.getTreeIcon(DirectoryCache.getDirectoryCache().getUserDirectory().getAbsolutePath()));
        lob_treeView.setRoot(lob_root);

        lob_contextMenu = initContextMenu();
        lob_treeView.setContextMenu(lob_contextMenu);

        lob_treeView.setOnContextMenuRequested( event ->
            onContextMenuRequest()
        );

        lob_treeView.setCellFactory(siTreeView ->
            new TreeCellImpl()
        );

        lob_treeView.setOnEditCommit(event -> {
            if (!event.getOldValue().equals(event.getNewValue())) {
                File lob_renamedFile = buildFileFromItem(event.getTreeItem());
                PreventDuplicateOperation.getDuplicateOperationPrevention().putRenamed(lob_renamedFile.toPath());
                ThreadManager.addCommandToFileManager(lob_renamedFile, FileManagerConstants.GC_RENAME, true, event.getNewValue(), false);
            }
        });
    }

    private ContextMenu initContextMenu() {
        //-----------------------Variables---------------------------------
        MenuItem lob_deleteFile;
        MenuItem lob_newDirectory;
        MenuItem lob_newFile;
        MenuItem lob_deleteDirectoryOnly;
        MenuItem lob_renameFile;
        MenuItem lob_sharedDirectory;
        ContextMenu rob_contextMenu;
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        //-----------------------------------------------------------------

        rob_contextMenu = new ContextMenu();
        lob_deleteFile = new MenuItem(GC_CONTEXT_DELETE);
        lob_deleteFile.setOnAction(event -> {
            File lob_selectedFile = buildFileFromItem(
                    lob_treeView.getSelectionModel().getSelectedItem()
            );
            ThreadManager.addCommandToFileManager(lob_selectedFile, FileManagerConstants.GC_DELETE,
                    true, true);
        });

        lob_newDirectory = new MenuItem(GC_CONTEXT_NEW_DIRECTORY);
        lob_newDirectory.setOnAction(event ->
                createNewDirectory()
        );

        lob_deleteDirectoryOnly = new MenuItem(GC_CONTEXT_DELETE_ONLY_DIR);
        lob_deleteDirectoryOnly.setOnAction(event -> {
                    File lob_file = buildFileFromItem(lob_treeView.getSelectionModel().getSelectedItem());
                    ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_DELETE_DIR_ONLY, true);
//                    TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(lob_file.toPath());
                }
        );

        lob_renameFile = new MenuItem(GC_CONTEXT_RENAME);
        lob_renameFile.setOnAction(event ->
                renameFile()
        );

        lob_newFile = new MenuItem(GC_CONTEXT_NEW_FILE);
        lob_newFile.setOnAction(event ->
                createNewFile()
        );

        lob_sharedDirectory = new MenuItem(GC_CONTEXT_PROPERTIES);
        lob_sharedDirectory.setOnAction(event ->
                sharedDirectoryScene(lob_treeView.getSelectionModel().getSelectedItem())
        );

        rob_contextMenu.getItems().addAll(lob_deleteFile,
                lob_newDirectory,
                lob_newFile,
                lob_deleteDirectoryOnly,
                lob_renameFile,
                lob_sharedDirectory);

        return rob_contextMenu;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initSharedDirectoryCache() {
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        SharedDirectoryRestClient lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();
        List<SharedDirectory> lli_sharedDirectories;
        String lva_sharedDirectoryPath = DirectoryCache.getDirectoryCache().getSharedDirectory().toString();
        File lob_sharedDirectoryFile;
        MappedFile lob_mappedFile;
        long lva_lastModified;

        lli_sharedDirectories = lob_restClient.getAllSharedDirectoriesOfUser();

        for (SharedDirectory lob_sharedDirectory : Objects.requireNonNull(lli_sharedDirectories)) {
            lob_sharedDirectoryCache.put(lob_sharedDirectory.getId(), lob_sharedDirectory);

            try {
                DirectoryNameMapper.getRenamedSharedDirectoryName(lob_sharedDirectory.getId());
            } catch (IllegalArgumentException ex) {
                DirectoryNameMapper.addNewSharedDirectory(lob_sharedDirectory.getId(), lob_sharedDirectory.getDirectoryName());
            }

            lob_sharedDirectoryFile = new File(
                    lva_sharedDirectoryPath + "\\" +
                            DirectoryNameMapper.getRenamedSharedDirectoryName(lob_sharedDirectory.getId())
            );

            if (!lob_sharedDirectoryFile.exists()) {
                try {
                    lob_sharedDirectoryFile.mkdir();

                    lva_lastModified = Files.readAttributes(lob_sharedDirectoryFile.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis();
                    lob_mappedFile = new MappedFile(lob_sharedDirectoryFile.toPath(), 1, lva_lastModified);
                    FileMapperCache.getFileMapperCache().put(lob_mappedFile);
                } catch (IOException ignore) {

                }
            }
        }
    }
}