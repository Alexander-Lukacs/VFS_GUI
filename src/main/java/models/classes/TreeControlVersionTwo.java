package models.classes;

import builder.RestClientBuilder;
import cache.DirectoryCache;
import cache.FileMapperCache;
import cache.SharedDirectoryCache;
import fileTree.classes.TreeSingleton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import restful.clients.SharedDirectoryRestClient;
import threads.classes.ThreadManager;
import threads.constants.FileManagerConstants;
import tools.TreeTool;
import tools.xmlTools.FileMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;

import static models.classes.FileService.readAllFilesFromDirectory;

public class TreeControlVersionTwo {

    public TreeControlVersionTwo() {
        init();
    }

    /**
     *
     initialize all needed resources
     */
    private void init() {
        Collection<File> lco_files;

        initFileMapperCache();
        initTreeView();
        initSharedDirectoryCache();
        ThreadManager.getFileManagerThread().start();
        lco_files = FileService.readAllFilesFromDirectory(DirectoryCache.getDirectoryCache().getUserDirectory());

        for (File lob_file : lco_files) {
            ThreadManager.addCommandToFileManager(lob_file, FileManagerConstants.GC_ADD, false, true, true, FileMapperCache.getFileMapperCache().get(lob_file.toPath()).getVersion());
        }
        ThreadManager.addCommandToFileManager(null, FileManagerConstants.GC_COMPARE_TREE, true);
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

    private void initSharedDirectoryCache() {
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        SharedDirectoryRestClient lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();
        List<SharedDirectory> lli_sharedDirectories;

        lli_sharedDirectories = lob_restClient.getAllSharedDirectoriesOfUser();

        for (SharedDirectory lob_sharedDirectory : lli_sharedDirectories) {
            lob_sharedDirectoryCache.put(lob_sharedDirectory.getId(), lob_sharedDirectory);
        }
    }
}
