
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import tools.AlertWindows;

import java.io.IOException;

public class MainApp extends Application {

    private final LoginController loginController = new LoginController();

    @Override
    public void start(Stage primaryStage) {
        try {
            loginController.start(primaryStage);
        }
        catch (IOException e){
            AlertWindows.ExceptionAlert(e.getMessage(), e);
        }
        catch(Exception ex){
            AlertWindows.ExceptionAlert(ex.getMessage(), ex);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}


