package controller;

import builder.ModelObjectBuilder;
import builder.RestClientBuilder;
import cache.DataCache;
import client.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import models.interfaces.User;
import tools.Validation;

import java.io.IOException;

import static cache.DataCache.*;
import static constants.SettingsConstants.*;

public class ChangePWController {

    @FXML
    private ListView<String> gob_lvOptions;

    @FXML
    private Button gob_btnSave;

    @FXML
    private TextField gob_tf_oldPassword;

    @FXML
    private TextField gob_tf_newPassword;

    @FXML
    private TextField gob_tf_confirmPassword;

    @FXML
    private AnchorPane gob_rootPane;

    private controller.ListView listView = new controller.ListView();
    private DataCache gob_dataCache;



    /**
     * Initialisation of ListView
     */
    public void initialize() {
        listView.loadList(gob_lvOptions);
        gob_dataCache = DataCache.getDataCache();
    }

    /**
     * open choosen View
     *
     * @param event
     * @throws IOException
     */
    public void loadView(MouseEvent event) throws IOException {
        if(gob_lvOptions.getSelectionModel().getSelectedItem().equals(CHANGE_PW)) {
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

    public void onChangePasswordButtonClick(ActionEvent event){
        User lob_user;

        String lva_ip   = gob_dataCache.get(GC_IP_KEY);
        String lva_port = gob_dataCache.get(GC_PORT_KEY);

        String lva_email             = gob_dataCache.get(GC_EMAIL_KEY);
        String lva_oldCachedPassword = gob_dataCache.get(GC_PASSWORD_KEY);
        String lva_oldPassword       = gob_tf_oldPassword.getText();
        String lva_newPassword       = gob_tf_newPassword.getText();
        String lva_confirmPassword   = gob_tf_confirmPassword.getText();

        if (Validation.passwordEqualsValidation(lva_oldPassword, lva_oldCachedPassword)) {
            if(Validation.passwordEqualsValidation(lva_newPassword, lva_confirmPassword)){
                RestClient restclient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port,
                        lva_email, lva_oldCachedPassword);

                lob_user = ModelObjectBuilder.getUserObject();
                lob_user.setEmail(lva_email);
                lob_user.setPassword(lva_newPassword);

                restclient.changePassword(lob_user);
            }
        }
    }
}
