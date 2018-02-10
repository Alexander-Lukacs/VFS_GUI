package controller;

import builder.ModelObjectBuilder;
import builder.RestClientBuilder;
import cache.DataCache;
import client.RestClient;
import client.RestResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import models.interfaces.User;
import tools.AlertWindows;
import tools.Utils;
import tools.Validation;
import tools.XmlTool;

import static cache.DataCache.*;
import static client.constants.HttpStatusCodes.GC_HTTP_OK;
import static controller.constants.SettingsConstants.GC_ADMIN_ADD;
import static controller.constants.SettingsConstants.GC_CHANGE_IP_PORT;
import static tools.constants.AlertConstants.*;

public class ChangePWController {
    private final controller.ListView gob_listView = new controller.ListView();

    @FXML
    private ListView<String> gob_lvOptions;
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
     * open selected View
     */
    public void loadView() {
        try {

            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_ADMIN_ADD)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/addAdmin.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }

            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_IP_PORT)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/changeIpPort.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
        } catch (Exception e) {

        }

    }

    public void onClickChangePassword() {
        RestResponse lob_restResponse;
        RestClient lob_restClient;
        User lob_user;

        String lva_ip = gob_dataCache.get(GC_IP_KEY);
        String lva_port = gob_dataCache.get(GC_PORT_KEY);

        String lva_email = gob_dataCache.get(GC_EMAIL_KEY);
        String lva_oldCachedPassword = gob_dataCache.get(GC_PASSWORD_KEY);
        String lva_oldPassword = gob_tf_oldPassword.getText();
        String lva_newPassword = gob_tf_newPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword.getText();

        if (isPasswordDataValid(lva_oldPassword, lva_oldCachedPassword, lva_newPassword, lva_confirmPassword)) {

            lob_restClient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port,
                    lva_email, lva_oldCachedPassword);

            lob_user = ModelObjectBuilder.getUserObject();
            lob_user.setEmail(lva_email);
            lob_user.setPassword(lva_newPassword);

            lob_restResponse = lob_restClient.changePassword(lob_user);

            if (lob_restResponse != null) {
                Utils.printResponseMessage(lob_restResponse);

                if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                    XmlTool.createXml(lva_ip, lva_port, lva_email, lva_newPassword);
                    gob_dataCache.replaceData(GC_PASSWORD_KEY, lva_newPassword);
                    // TODO close stage
                }
            }
        }
    }

    private boolean isPasswordDataValid(String iva_oldPassword, String iva_oldCachedPassword, String iva_newPassword,
                                        String iva_confirmPassword) {

        StringBuilder lob_sb = new StringBuilder();
        boolean lva_validationFailure = false;

        if (!Validation.passwordEqualsValidation(iva_oldPassword, iva_oldCachedPassword)) {
            lob_sb.append(GC_WARNING_OLD_PASSWORD_NOT_EQUAL);
            gob_tf_oldPassword.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.passwordEqualsValidation(iva_newPassword, iva_confirmPassword)) {
            lob_sb.append(GC_WARNING_NEW_PASSWORD_NOT_EQUAl);
            gob_tf_newPassword.setText("");
            gob_tf_confirmPassword.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.isPasswordValid(iva_newPassword)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            gob_tf_newPassword.setText("");
            lva_validationFailure = true;
        }

        if (lva_validationFailure) {
            new AlertWindows().createWarningAlert(lob_sb.toString());
            return false;
        }

        return true;
    }
}