package threads.classes;

import models.interfaces.FileChangeListener;
import models.classes.PreventDuplicateOperation;
import threads.constants.FileManagerConstants;
import threads.interfaces.ThreadControl;

import java.io.File;
import java.io.IOException;

public class DirectoryWatcherNew implements ThreadControl {
    private DirectoryWatchServiceNew gob_watchService;
    private File gob_rootFile;
    private boolean gva_isRunning = false;
    private static PreventDuplicateOperation lob_preventDuplicates = PreventDuplicateOperation.getDuplicateOperationPrevention();

    DirectoryWatcherNew(File iob_files) {
        gob_rootFile = iob_files;
    }

    private void init(File iob_file) {
        try {
            gob_watchService = new DirectoryWatchServiceNew(iob_file, new FileChangeListener() {
                @Override
                public void fileAdded(File iob_File) {
//                    addFile(iob_path);
                    addFile(iob_File);
                }

                @Override
                public void fileDeleted(File iob_File) {
//                    deleteFile(iob_path);
                    deleteFile(iob_File);
                }

                @Override
                public void fileRenamed(File iob_File, String iva_newName) {
//                    renameFile(iob_path, iva_newName);
                }
                @Override
                public void fileMoved(File iob_oldPath, File iob_newPath) {

                }

                @Override
                public void fileUpdate(File iob_File) {
                    System.out.println("Updated: " + iob_File);
                }

                @Override
                public void startScan() {
                    //on purpose empty
                }

                @Override
                public void finishedScan() {
                    PreventDuplicateOperation.getDuplicateOperationPrevention().clear();
                }
            });
            gob_watchService.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void addFile(File iob_file) {
        if (lob_preventDuplicates.wasFileCreated(iob_file.toPath())) {
            lob_preventDuplicates.removeCreated(iob_file.toPath());
        } else {
            System.out.println("Added: " + iob_file);
            ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_ADD, true, iob_file.isDirectory(), false, 1);
        }
    }

    private void deleteFile(File iob_file) {
        if (lob_preventDuplicates.wasFileDeleted(iob_file.toPath())) {
            lob_preventDuplicates.removeDeleted(iob_file.toPath());
        } else {
            System.out.println("Deleted: " + iob_file);
        }
    }

    private void movedFile(File iob_oldFile, File iob_newFile) {
        if (lob_preventDuplicates.wasFilesMoved(iob_oldFile.toPath())) {
            lob_preventDuplicates.removeMoved(iob_oldFile.toPath());
        } else {
            System.out.println("Moved: " + iob_oldFile + " TO " + iob_newFile);
            if (!iob_oldFile.getName().equals(iob_newFile.getName())) {
                System.out.println("MOVED AND RENAMED");
            }
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
