package threads.models;

import rest.RestClient;
import threads.interfaces.Thread;

import java.io.File;

public class ThreadManager {
    private static MainDirectoryWatcher gob_directoryWatcherInstance;

    /**
     * if no instance of the directryWatcher exists, create a new one
     * @param iob_restClient restClient for the watchService
     * @param iob_files root file to monitor
     * @return the instance of the already existing or new directoryWatchService
     */
    public static Thread getDirectoryWatcherThread(RestClient iob_restClient, File iob_files) {
        if (gob_directoryWatcherInstance == null) {
            gob_directoryWatcherInstance = new MainDirectoryWatcher(iob_restClient, iob_files);
        }
        return gob_directoryWatcherInstance;
    }

    /**
     * Before calling this Method, call the method that uses Parameters to create a new instance of the service
     * @return the instance of the directoryWatchService (can be null)
     */
    public static Thread getDrectoryWatcherThread() {
        return gob_directoryWatcherInstance;
    }
}
