
import controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static constants.SettingsConstants.*;

public class MainApp extends Application {

    private LoginController loginController = new LoginController();

    @Override
    public void start(Stage primaryStage) throws Exception{
        loginController.start(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }
}


