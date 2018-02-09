
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import tools.AlertWindows;


public class MainApp extends Application {

    private final LoginController loginController = new LoginController();

    @Override
    public void start(Stage primaryStage) {
        try {
            loginController.start(primaryStage);
        } catch (Exception ex) {
            AlertWindows.createExceptionAlert(ex.getMessage(), ex);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}


