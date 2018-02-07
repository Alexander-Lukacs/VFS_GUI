
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;

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


