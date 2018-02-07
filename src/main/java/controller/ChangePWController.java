package controller;

import builder.RestClientBuilder;
import cache.UserDataCache;
import client.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import javax.sound.midi.Soundbank;
import java.io.IOException;

import static constants.SettingsConstants.*;

public class ChangePWController {

    @FXML
    private ListView<String> gob_lvOptions;

    @FXML
    private Button gob_btnSave;

    @FXML
    private TextField txtOldPassword;

    @FXML
    private TextField txtNewPassword;

    @FXML
    private TextField txtConfirmPassword;

    @FXML
    private AnchorPane gob_rootPane;

    private controller.ListView listView = new controller.ListView();



    /**
     * Initiallisiert die ListView
     */
    public void initialize()
    {
        listView.loadList(gob_lvOptions);
    }
    /**
     * Öffnet die View die ausgewählt wurde
     *
     * @param event
     * @throws IOException
     */
    public void loadView(MouseEvent event) throws IOException {

        if(gob_lvOptions.getSelectionModel().getSelectedItem().equals(CHANGE_PW))
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("ChangePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(ADMIN_ADD)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("addAdmin.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(CHANGE_IP_PORT)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changeIpPort.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }

    public void OnClick(ActionEvent event){
        String lva_pwOld = txtOldPassword.getText();
        String lva_pwNew = txtNewPassword.getText();
        String lva_pwConfirm = txtConfirmPassword.getText();

        if(lva_pwNew.equals(lva_pwConfirm)){
            //RestClient restclient = RestClientBuilder.buildRestClientWithAuth(email, password, ip, port);
   //TODO         System.out.println(UserDataCache.getValue("IP"));
        }
    }
}
