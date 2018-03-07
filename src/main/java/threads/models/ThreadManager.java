package threads.models;

import restful.clients.FileRestClient;
import threads.interfaces.ThreadControl;

import java.io.File;

public class ThreadManager {
    private static MainDirectoryWatcher gob_directoryWatcherInstance;
    private static FileManagerThreadControl gob_fileManagerInstance;

    /**
     * if no instance of the directryWatcher exists, create a new one
     * @param iob_restClient restClient for the watchService
     * @param iob_files root file to monitor
     * @return the instance of the already existing or new directoryWatchService
     */
    public static ThreadControl getDirectoryWatcherThread(FileRestClient iob_restClient, File iob_files) {
        if (gob_directoryWatcherInstance == null) {
            gob_directoryWatcherInstance = new MainDirectoryWatcher(iob_restClient, iob_files);
        }
        return gob_directoryWatcherInstance;
    }

    /**
     * Before calling this Method, call the method that uses Parameters to create a new instance of the service
     * @return the instance of the directoryWatchService (can be null)
     */
    public static ThreadControl getDrectoryWatcherThread() {
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
}
