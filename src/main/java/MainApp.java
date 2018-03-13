import builder.RestClientBuilder;
import cache.DataCache;
import cache.FileMapperCache;
import controller.classes.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import models.classes.MappedFile;
import restful.clients.RestClient;
import threads.classes.NotifyServerThread;
import tools.AlertWindows;
import tools.xmlTools.FileMapper;

import java.io.FileNotFoundException;
import java.net.ServerSocket;

import static cache.DataCache.GC_EMAIL_KEY;


public class MainApp extends Application {
    private static final String NOTIFY_SERVER_THREAD_NAME = "notifyServer";

    private final LoginController loginController = new LoginController();
    private Thread gob_notifyThread;

    public static void main(String[] args) {
        // Ensure that only one instance of the app is running
        try {
            ServerSocket serverSocket = new ServerSocket(32001);
            serverSocket.close();
        } catch (Exception ex) {
            System.exit(1);
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // NotifyServerThread for tree updates
        gob_notifyThread = new NotifyServerThread();
        gob_notifyThread.setName(NOTIFY_SERVER_THREAD_NAME);
        gob_notifyThread.start();

        try {
            loginController.start(primaryStage);
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }
    }

    @Override
    public void stop() throws Exception {
        DataCache lob_dataCache = DataCache.getDataCache();
        RestClient lob_restClient;

        try {
            if (lob_dataCache.get(GC_EMAIL_KEY) != null) {
                lob_restClient = RestClientBuilder.buildRestClientWithAuth();
                lob_restClient.unregisterClient();
            }
        } catch (Exception ex) {
            new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
        }

        // Stops the NotifyServerThread
        gob_notifyThread.interrupt();

        try {
            for (MappedFile lob_mappedFile : FileMapper.getAllFiles()) {
                FileMapper.removeFile(lob_mappedFile.getFilePath().toString());
            }

            for (MappedFile lob_mappedFile : FileMapperCache.getFileMapperCache().getAll()) {
//            FileMapper.removeFile(lob_mappedFile.getFilePath().toString());
                FileMapper.addFile(lob_mappedFile);
            }

        } catch (RuntimeException ignore) {

        }
        super.stop();
    }
}


