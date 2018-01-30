
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static constants.SettingsConstants.*;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("loginScreen.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(VFS);
        //primaryStage.setResizable(false);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

    public void close(){
        primaryStage.close();

    }
}


