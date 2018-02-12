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
     * @param iob_path
     */
    void fileDeleted(Path iob_path);

    /**
     * if a file was moved or renamed in the root directory or a subdirectoy this method is called
     * @param iob_oldPath old path of the file
     * @param iob_newPath new path of the file
     */
    void renamed(Path iob_oldPath, Path iob_newPath);
}
