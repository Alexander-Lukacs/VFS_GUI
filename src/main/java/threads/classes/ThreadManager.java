package threads.classes;

import fileTree.classes.TreeSingleton;
import javafx.scene.control.Label;
import threads.interfaces.ThreadControl;

import java.io.File;

public class ThreadManager {
    private static DirectoryWatcher gob_directoryWatcherInstance;
    private static FileManagerThreadControl gob_fileManagerInstance;
    private static DirectoryCounterThread gob_directoryCounterInstance;

    /**
     * if no instance of the directoryWatcher exists, create a new one
     * @param iob_files root file to monitor
     * @return the instance of the already existing or new directoryWatchService
     */
    public static ThreadControl getDirectoryWatcherThread(File iob_files) {
        if (gob_directoryWatcherInstance == null) {
            gob_directoryWatcherInstance = new DirectoryWatcher(iob_files);
        }
        return gob_directoryWatcherInstance;
    }

    /**
     * Before calling this Method, call the method that uses Parameters to create a new instance of the service
     * @return the instance of the directoryWatchService (can be null)
     */
    public static ThreadControl getDirectoryWatcherThread() {
        return gob_directoryWatcherInstance;
    }

    public static ThreadControl getFileManagerThread() {
        if (gob_fileManagerInstance == null) {
            gob_fileManagerInstance = new FileManagerThreadControl();
        }
        return gob_fileManagerInstance;
    }

    public static void addCommandToFileManager(File iob_file, String iva_commando, boolean iva_executeCommandOnServer, Object... iar_fileInformation) {
        if (gob_fileManagerInstance == null) {
            gob_fileManagerInstance = new FileManagerThreadControl();
        }
        gob_fileManagerInstance.addFileWithCommando(iob_file, iva_commando, iva_executeCommandOnServer, iar_fileInformation);
    }

    public static ThreadControl getDirectoryCounterThread(File iob_file, Label iob_label) {
        return gob_directoryCounterInstance = new DirectoryCounterThread(iob_file, iob_label);
    }

    public static void stopAndClear() {
        if (gob_directoryWatcherInstance != null) {
            gob_directoryWatcherInstance.stop();
            gob_directoryWatcherInstance.clear();
        }
        gob_directoryWatcherInstance = null;

        if (gob_fileManagerInstance != null) {
            gob_fileManagerInstance.stop();
            gob_fileManagerInstance.clear();
        }
        gob_fileManagerInstance = null;

        if (gob_directoryCounterInstance != null) {
            gob_directoryCounterInstance.stop();
        }

        gob_directoryCounterInstance = null;

        TreeSingleton.getInstance().reset();
    }
}
