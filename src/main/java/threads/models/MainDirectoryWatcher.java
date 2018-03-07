package threads.models;

import fileTree.interfaces.FileChangeListener;
import fileTree.models.TreeSingleton;
import javafx.scene.control.TreeItem;
import restful.clients.FileRestClient;
import threads.interfaces.ThreadControl;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * this class contains 3 DirectoryWatchServices, one for the private, public and shared directory
 */
class MainDirectoryWatcher implements ThreadControl {
    private FileRestClient gob_restClient;
    private DirectoryWatchService gob_watchService;
    private File gob_rootFile;

    MainDirectoryWatcher(FileRestClient iob_restClient, File iob_files) {
        gob_restClient = iob_restClient;
        gob_rootFile = iob_files;
    }

    private void init(File iob_file) {
        try {
            gob_watchService = new DirectoryWatchService(iob_file.toPath(), new FileChangeListener() {
                @Override
                public void fileAdded(Path iob_path) {
                    addFile(iob_path);
                }

                @Override
                public void fileDeleted(Path iob_path) {
                    deleteFile(iob_path);
                }

                @Override
                public void fileMoved(Path iob_oldPath, Path iob_newPath) {
                    moveFile(iob_oldPath, iob_newPath);
                }

                @Override
                public void fileRenamed(Path iob_path, String iva_newName) {
                    renameFile(iob_path, iva_newName);
                }

                @Override
                public void startScan() {
                    //on purpose empty
                }

                @Override
                public void finishedScan() {
                    TreeSingleton.getInstance().getDuplicateOperationsPrevention().clear();
                }
            });
            gob_watchService.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void addFile(Path iob_path) {
        if (TreeTool.filterRootFiles(iob_path)){
//            System.out.println("Filtered:" + iob_path);
            return;
        }

        boolean lva_isDirectory = iob_path.toFile().isDirectory();
//        System.out.println("fileAdded: " + iob_path);
        TreeTool.createFileOrDirectory(iob_path.toFile(), lva_isDirectory, gob_restClient);
    }

    private void deleteFile(Path iob_path) {
        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileDeted(iob_path)) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeDeleted(iob_path);
        } else {
            TreeItem<String> lob_itemToDelete = TreeTool.getTreeItem(iob_path.toFile());
            TreeTool.deleteFile(iob_path.toFile(), lob_itemToDelete, gob_restClient);
        }
    }

    private void moveFile(Path iob_oldPath, Path iob_newPath) {
        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFilesMoved(iob_oldPath)) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeMoved(iob_oldPath);
        } else {
            TreeTool.moveFile(iob_oldPath, iob_newPath.getParent(), true, gob_restClient);
        }
    }

    private void renameFile(Path iob_path, String iva_newName) {
        try {
            if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileRenamed(iob_path)) {
                TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeRenamed(iob_path);
            } else {
//                System.out.println("RENAMED: " + iob_path + " TO: " + iva_newName);
                TreeSingleton.getInstance().getTree().renameFile(iob_path.toFile(), iva_newName);
                TreeItem<String> lob_item = TreeTool.getTreeItem(iob_path.toFile());
                lob_item.setValue(iva_newName);

                String lva_relativePath = TreeTool.getRelativePath(iob_path.toString());

                gob_restClient.renameFile(lva_relativePath, iva_newName);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start() {
        init(gob_rootFile);
    }

    @Override
    public void stop() {
        gob_watchService.stop();
    }
}
