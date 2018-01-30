package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    private boolean isAdmin = true;

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

 /*       if(NAME.equals(tfUserName.getText()) && PASSWORD.equals(pwPasswort.getText()))
        {
            admin = getAdmin();
        }*/
    else{
            tfUserName.setText("");
            pwPasswort.setText("");
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("mainScreen.fxml"));
            SplitPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle(VFS);
           // lob_stage.setResizable(false);
            lob_stage.setScene(lob_scene);
            lob_stage.show();

            //gob_rootPane.getChildren().setAll(lob_pane);
        }
    }

    public boolean getIsAdmin()
    {
        return isAdmin;
    }

    public void close() {
        //gob_rootPane = gob_rootPane.getScene().getWindow();
        ((Stage)tfUserName.getScene().getWindow()).close();
    }
}
