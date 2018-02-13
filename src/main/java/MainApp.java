
import builder.RestClientBuilder;
import cache.DataCache;
import client.RestClient;
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import tools.AlertWindows;

import java.io.IOException;

import static cache.DataCache.*;


public class MainApp extends Application {

    private final LoginController loginController = new LoginController();

    @Override
    public void start(Stage primaryStage) {
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

        if (lob_dataCache.get(GC_EMAIL_KEY) != null) {
            lob_restClient = RestClientBuilder.buildRestClientWithAuth();
            lob_restClient.unregisterClient();
        }

        super.stop();
    }


    public static void main(String[] args) {
        launch(args);
    }
}


