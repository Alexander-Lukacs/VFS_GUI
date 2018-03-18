package threads.classes;

import cache.FileMapperCache;
import models.classes.MappedFile;
import models.interfaces.FileChangeListener;
import models.classes.PreventDuplicateOperation;
import threads.constants.FileManagerConstants;
import threads.interfaces.ThreadControl;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;

public class DirectoryWatcher implements ThreadControl {
    private DirectoryWatchService gob_watchService;
    private final File gob_rootFile;
    private static final PreventDuplicateOperation lob_preventDuplicates = PreventDuplicateOperation.getDuplicateOperationPrevention();

    DirectoryWatcher(File iob_files) {
        gob_rootFile = iob_files;
    }

    private void init(File iob_file) {
        try {
            gob_watchService = new DirectoryWatchService(iob_file, new FileChangeListener() {
                @Override
                public void fileAdded(File iob_File) {
                    addFile(iob_File);
                }

                @Override
                public void fileDeleted(File iob_File) {
                    deleteFile(iob_File);
                }

                @Override
                public void fileRenamed(File iob_File, String iva_newName) {
                    renameFile(iob_File, iva_newName);
                }

                @Override
                public void fileMoved(File iob_oldFile, File iob_newFile) {
                    movedFile(iob_oldFile, iob_newFile);
                }

                @Override
                public void fileUpdate(File iob_file) {
                    updateFile(iob_file);
                }

                @Override
                public void startScan() {
//                    System.out.println(PreventDuplicateOperation.getDuplicateOperationPrevention());
                }

                @Override
                public void finishedScan() {
//                    PreventDuplicateOperation.getDuplicateOperationPrevention().clear();
                }
            });
            gob_watchService.start();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void updateFile(File iob_file) {
        if (lob_preventDuplicates.wasFileCreated(iob_file.toPath())) {
            lob_preventDuplicates.removeCreated(iob_file.toPath());
        } else {
            MappedFile lob_mappedFile = FileMapperCache.getFileMapperCache().get(iob_file.toPath());
            lob_mappedFile.setVersion(lob_mappedFile.getVersion() + 1);
            ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_UPLOAD_TO_SERVER, true, false);

        }
    }

    private void addFile(File iob_file) {
        if (lob_preventDuplicates.wasFileCreated(iob_file.toPath())) {
            lob_preventDuplicates.removeCreated(iob_file.toPath());
        } else {
            if (TreeTool.getTreeItem(iob_file) == null) {
                System.out.println("Added: " + iob_file);
                ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_ADD, true, iob_file.isDirectory(), false, 1);
            }
        }
    }

    private void deleteFile(File iob_file) {
        if (lob_preventDuplicates.wasFileDeleted(iob_file.toPath())) {
            lob_preventDuplicates.removeDeleted(iob_file.toPath());
        } else {
            ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_DELETE, true, true);
        }
    }

    private void movedFile(File iob_oldFile, File iob_newFile) {
        File lob_destinationFile = new File(iob_newFile.toString().replaceFirst("[^\\\\]*$", ""));
        if (lob_preventDuplicates.wasFilesMoved(iob_oldFile.toPath())) {
            lob_preventDuplicates.removeMoved(iob_oldFile.toPath());
        } else {
            ThreadManager.addCommandToFileManager(iob_oldFile, FileManagerConstants.GC_MOVE, true, lob_destinationFile, true);
        }
    }

    private void renameFile(File iob_file, String lva_newName) {
        if (lob_preventDuplicates.wasFileRenamed(iob_file.toPath())) {
            lob_preventDuplicates.removeRenamed(iob_file.toPath());
        } else {
            System.out.println("WATCHER: RENAME");
            ThreadManager.addCommandToFileManager(iob_file, FileManagerConstants.GC_RENAME, true, lva_newName, true);
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

    @Override
    public void clear() {
        this.gob_watchService.clear();
    }
}
