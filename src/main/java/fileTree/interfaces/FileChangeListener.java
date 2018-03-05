package fileTree.interfaces;

import java.nio.file.Path;
import java.util.EventListener;

public interface FileChangeListener extends EventListener{

    /**
     * if a file is added in the root directory or a subdirectoy this method is called
     * @param iob_path path of the new file
     */
    void fileAdded(Path iob_path);

    /**
     * if a file is removed in the root directory or a subdirectoy this method is called
     * @param iob_path path of the file that was deleted
     */
    void fileDeleted(Path iob_path);

    /**
     * if a file was moved  in the root directory or a subdirectoy this method is called
     * @param iob_oldPath old path of the file
     * @param iob_newPath new path of the file
     */
    void fileMoved(Path iob_oldPath, Path iob_newPath);

    /**
     * if a file was renamed in the root directory or a subdirectoy this method is called
     * @param iob_path path of the file that was renamed (file with old name)
     * @param iva_newName new file name
     */
    void fileRenamed(Path iob_path, String iva_newName);

    /**
     * called when scan started
     */
    void startScan();

    /**
     * called when the scan is finished
     */
    void finishedScan();
}
