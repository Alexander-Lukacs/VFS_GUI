
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static constants.SettingsConstants.*;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("mainScreen.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(VFS);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
