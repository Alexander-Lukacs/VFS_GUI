package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;


import java.io.IOException;

import static constants.SettingsConstants.*;

public class AdminLoginController {

    @FXML
    private AnchorPane gob_rootPane;

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField pwPasswort;

    @FXML
    private TextField tfUserName;

    private boolean admin = false;

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
        if(!USER_NAME.equals(tfUserName.getText()) || !PASSWORD.equals(pwPasswort.getText()))
        {
            System.out.println("Benutzername oder Passwort falsch!");
        }

        if(USER_NAME.equals(tfUserName.getText()) && PASSWORD.equals(pwPasswort.getText()))
        {
            admin = true;
        }

        if(admin)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("settingsAdmin.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }
}
