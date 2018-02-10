package controller;

import builder.ModelObjectBuilder;
import builder.RestClientBuilder;
import cache.DataCache;
import client.HttpMessage;
import client.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import models.interfaces.User;
import tools.AlertWindows;
import tools.Validation;
import tools.XmlTool;

import java.io.IOException;

import static cache.DataCache.*;
import static client.constants.HttpStatusCodes.GC_HTTP_BAD_REQUEST;
import static client.constants.HttpStatusCodes.GC_HTTP_CONFLICT;
import static client.constants.HttpStatusCodes.GC_HTTP_OK;
import static controller.constants.SettingsConstants.*;
import static tools.constants.AlertConstants.*;

public class ChangePWController {

    private final controller.ListView gob_listView = new controller.ListView();
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
    private DataCache gob_dataCache;


    /**
     * Initialisation of ListView
     */
    public void initialize() {
        gob_listView.loadSettingsList(gob_lvOptions);
        gob_dataCache = DataCache.getDataCache();
    }

    /**
     * open choosen View
     *
     * @throws IOException
     */
    public void loadView() throws IOException {
        try {

            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_ADMIN_ADD)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("addAdmin.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }

            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_IP_PORT)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changeIpPort.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
        }catch (Exception e){

        }

    }

    public void onClickChangePassword() {
        HttpMessage httpMessage;
        User lob_user;

        String lva_ip = gob_dataCache.get(GC_IP_KEY);
        String lva_port = gob_dataCache.get(GC_PORT_KEY);

        String lva_email = gob_dataCache.get(GC_EMAIL_KEY);
        String lva_oldCachedPassword = gob_dataCache.get(GC_PASSWORD_KEY);
        String lva_oldPassword = gob_tf_oldPassword.getText();
        String lva_newPassword = gob_tf_newPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword.getText();

        if (isPasswordDataValid(lva_oldPassword, lva_oldCachedPassword, lva_newPassword, lva_confirmPassword)) {
            RestClient restclient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port,
                    lva_email, lva_oldCachedPassword);
            XmlTool.createXml(lva_ip, lva_port, lva_email, lva_newPassword);

            lob_user = ModelObjectBuilder.getUserObject();
            lob_user.setEmail(lva_email);
            lob_user.setPassword(lva_newPassword);

            httpMessage = restclient.changePassword(lob_user);

            printHttpMessage(httpMessage);
            gob_tf_oldPassword.setText("");
            gob_tf_newPassword.setText("");
            gob_tf_confirmPassword.setText("");
        }
    }

    private boolean isPasswordDataValid(String iva_oldPassword, String iva_oldCachedPassword, String iva_newPassword,
                                        String iva_confirmPassword) {

        StringBuilder lob_sb = new StringBuilder();
        boolean validationFailure = false;

        if (!Validation.passwordEqualsValidation(iva_oldPassword, iva_oldCachedPassword)) {
            lob_sb.append(GC_WARNING_OLD_PASSWORD_NOT_EQUAL);
            validationFailure = true;
        }

        if (!Validation.passwordEqualsValidation(iva_newPassword, iva_confirmPassword)) {
            lob_sb.append(GC_WARNING_NEW_PASSWORD_NOT_EQUAl);
            validationFailure = true;
        }

        if (!Validation.isPasswordValid(iva_newPassword)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            validationFailure = true;
        }

        if (validationFailure) {
            AlertWindows.createWarningAlert(lob_sb.toString());
            return false;
        }

        return true;
    }

    private void printHttpMessage(HttpMessage httpMessage) {
        switch (httpMessage.getHttpStatus()) {
            case GC_HTTP_OK:
                AlertWindows.createInformationAlert(httpMessage.getPasswordChangeStatus());
                break;

            case GC_HTTP_BAD_REQUEST:
                AlertWindows.createErrorAlert(httpMessage.getPasswordChangeStatus());
                break;

            case GC_HTTP_CONFLICT:
                AlertWindows.createErrorAlert(httpMessage.getPasswordChangeStatus());
                break;
        }
    }
}
