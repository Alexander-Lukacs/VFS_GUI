package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static constants.SettingsConstants.SETTINGS;

public class MainController {

    private LoginController loginController = new LoginController();
    @FXML
    private Button btnSettings;

    private boolean close = false;

    public void initialize()
    {
        loginController.close();
    }

    /**
     Beim Klicken des Buttons wird die View settings.fxml geöffnet
     */

   public void onClick(ActionEvent e) throws RuntimeException, IOException{
       FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("settings.fxml"));
       AnchorPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle(SETTINGS);
            lob_stage.setResizable(false);
            lob_stage.setScene(lob_scene);
            lob_stage.show();

    }

    public boolean getClose()
    {
        return close;
    }

    public void setClose(boolean close)
    {
        this.close = close;
    }
}
