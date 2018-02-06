package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import java.io.IOException;

import static constants.SettingsConstants.*;

public class LoginController {

    @FXML
    private AnchorPane gob_rootPane;

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField pwPasswort;

    @FXML
    private TextField tfUserName;

    private boolean isAdmin = false;

    private Stage stage = new Stage();

    private MainController mainController = new MainController();

    public void initialize()
    {

    }

    /**
        Beim Klicken des Buttons wird geprüft ob der User ein Admin ist,
        falls ja, dann öffne die View settingAdmin.fxml
        falls nein, dann kommt eine Fehlermeldung
     */

    public void onClick(ActionEvent event) throws IOException
    {

        if(!NAME.equals(tfUserName.getText()) || !PASSWORD.equals(pwPasswort.getText()))
        {
            System.out.println("Benutzername oder Passwort falsch!");
        }
        else {
            tfUserName.setText("");
            pwPasswort.setText("");
            mainController.start(stage);
            close();
        }
    }

    public boolean getIsAdmin()
    {
        return isAdmin;
    }

    public void close() {
        ((Stage)tfUserName.getScene().getWindow()).close();
    }

    public void start(Stage stage) throws IOException{
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("loginScreen.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle(VFS);
        stage.show();
    }
}
