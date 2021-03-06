package threads.classes;

import fileTree.interfaces.FileChangeListener;
import fileTree.classes.TreeSingleton;
import javafx.scene.control.TreeItem;
import threads.constants.FileManagerConstants;
import threads.interfaces.ThreadControl;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * this class contains 3 DirectoryWatchServices, one for the private, public and shared directory
 */
class MainDirectoryWatcher implements ThreadControl {
    private DirectoryWatchService gob_watchService;
    private File gob_rootFile;
    private boolean gva_isRunning = false;

    MainDirectoryWatcher(File iob_files) {
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
            return;
        }
        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileCreated(iob_path)) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeCreated(iob_path);
        } else {
            if (!gva_isRunning) {
                return;
            }
            ThreadManager.addCommandToFileManager(iob_path.toFile(), FileManagerConstants.GC_ADD, true);
        }
    }

    private void deleteFile(Path iob_path) {
        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileDeleted(iob_path)) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeDeleted(iob_path);
        } else {
            if (!gva_isRunning) {
                return;
            }
            ThreadManager.addCommandToFileManager(iob_path.toFile(), FileManagerConstants.GC_DELETE, true, 0);
        }
    }

    private void moveFile(Path iob_oldPath, Path iob_newPath) {
        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFilesMoved(iob_oldPath)) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeMoved(iob_oldPath);
        } else {
            if (!gva_isRunning) {
                return;
            }
            ThreadManager.addCommandToFileManager(iob_oldPath.toFile(), FileManagerConstants.GC_MOVE, true, iob_newPath.toFile(), true);
        }
    }

    private void renameFile(Path iob_path, String iva_newName) {
        if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileRenamed(iob_path)) {
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeRenamed(iob_path);
        } else {
            TreeSingleton.getInstance().getTree().renameFile(iob_path.toFile(), iva_newName);
            TreeItem<String> lob_item = TreeTool.getTreeItem(iob_path.toFile());
            lob_item.setValue(iva_newName);
            if (!gva_isRunning) {
                return;
            }
            ThreadManager.addCommandToFileManager(iob_path.toFile(), FileManagerConstants.GC_RENAME, true, iva_newName, true);
        }
    }

    @Override
    public void start() {
        gva_isRunning = true;
        init(gob_rootFile);
    }

    @Override
    public void stop() {
        gva_isRunning = false;
        gob_watchService.stop();
    }

    @Override
    public void clear() {
        this.gob_watchService.clear();
    }
}
