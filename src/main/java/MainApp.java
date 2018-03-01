import builder.RestClientBuilder;
import cache.DataCache;
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import rest.RestClient;
import services.NotifyServerThread;
import tools.AlertWindows;

import static cache.DataCache.GC_EMAIL_KEY;


public class MainApp extends Application {
    private static final String NOTIFY_SERVER_THREAD_NAME = "notifyServer";

    private final LoginController loginController = new LoginController();
    private Thread gob_notifyThread;

    public static void main(String[] args) {
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

        super.stop();
    }
}


