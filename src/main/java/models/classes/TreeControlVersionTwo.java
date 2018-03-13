package models.classes;

import cache.DirectoryCache;
import cache.FileMapperCache;
import fileTree.classes.TreeSingleton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import threads.classes.ThreadManager;
import threads.constants.FileManagerConstants;
import tools.TreeTool;
import tools.xmlTools.FileMapper;

import java.io.File;
import java.util.Collection;

public class TreeControlVersionTwo {

    public TreeControlVersionTwo() {
        init();
    }

    /**
     *
     initialize all needed resources
     */
    private void init() {
        FileMapperCache lob_FileMapperCache = FileMapperCache.getFileMapperCache();
        Collection<File> lco_files;

        initTreeView();
        ThreadManager.getFileManagerThread().start();
        lco_files = FileService.readAllFilesFromRootDirectory();

        for (File lob_file : lco_files) {
            ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_ADD, false, true, true);
        }
        FileService.compareFilesToServer();
    }

    private void initTreeView() {
        TreeView<String> lob_treeView = TreeSingleton.getInstance().getTreeView();
        lob_treeView.setPrefHeight(1080);
        lob_treeView.setShowRoot(false);

        TreeItem<String> lob_root = new TreeItem<>(DirectoryCache.getDirectoryCache().getUserDirectory().getName());
        lob_root.setGraphic(TreeTool.getTreeIcon(DirectoryCache.getDirectoryCache().getUserDirectory().getAbsolutePath()));
        lob_treeView.setRoot(lob_root);
        lob_treeView.setShowRoot(false);
    }
}
