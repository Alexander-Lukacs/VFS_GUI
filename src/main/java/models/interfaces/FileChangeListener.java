package models.interfaces;

import java.io.File;

public interface FileChangeListener {
    /**
     * if a file is added in the root directory or a subdirectory this method is called
     * @param iob_path path of the new file
     */
    void fileAdded(File iob_path);

    /**
     * if a file is removed in the root directory or a subdirectory this method is called
     * @param iob_path path of the file that was deleted
     */
    void fileDeleted(File iob_path);

    /**
     * if a file was renamed in the root directory or a subdirectory this method is called
     * @param iob_path path of the file that was renamed (file with old name)
     * @param iva_newName new file name
     */
    void fileRenamed(File iob_path, String iva_newName);

    /**
     * if a file was moved  in the root directory or a subdirectory this method is called
     * @param iob_oldPath old path of the file
     * @param iob_newPath new path of the file
     */
    void fileMoved(File iob_oldPath, File iob_newPath);

    void fileUpdate(File iob_path);

    /**
     * called when scan started
     */
    void startScan();

    /**
     * called when the scan is finished
     */
    void finishedScan();
}
