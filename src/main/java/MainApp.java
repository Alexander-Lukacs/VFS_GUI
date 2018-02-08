
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import tools.AlertWindows;

import java.io.IOException;

import static controller.constants.AlertConstants.*;

public class MainApp extends Application {

    private LoginController loginController = new LoginController();

    @Override
    public void start(Stage primaryStage) {
        try {
            loginController.start(primaryStage);
        }
        catch (IOException e){
            AlertWindows.ExceptionAlert(GC_EXCEPTION_TITLE, GC_EXCEPTION_HEADER, e.getMessage(), e);
        }
        catch(Exception ex){
            AlertWindows.ExceptionAlert(GC_EXCEPTION_TITLE, GC_EXCEPTION_HEADER, ex.getMessage(), ex);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}


