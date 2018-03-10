package controller.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import models.classes.RestResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import models.classes.User;
import restful.clients.UserRestClient;
import tools.AlertWindows;
import tools.Utils;
import tools.Validation;
import tools.xmlTools.LastSessionStorage;

import static cache.DataCache.*;
import static restful.constants.HttpStatusCodes.GC_HTTP_OK;
import static controller.constants.SettingsConstants.GC_ADMIN_ADD;
import static controller.constants.SettingsConstants.GC_CHANGE_IP_PORT;
import static tools.constants.AlertConstants.*;

public class ChangePWController {
    private final controller.classes.ListView gob_listView = new controller.classes.ListView();

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
    @FXML
    private Button gob_btnSave;


    /**
     * Initialisation of ListView
     */
    public void initialize() {
        addKeyListener();
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
            // TODO schauen ob es eine besser Methode gibt, als ein leerer catch block
        } catch (Exception ignore) {}

    }

    public void onClickChangePassword() {
        Stage lob_stage;
        RestResponse lob_restResponse;
        UserRestClient lob_restClient;
        User lob_user;

        String lva_email = gob_dataCache.get(GC_EMAIL_KEY);
        String lva_oldCachedPassword = gob_dataCache.get(GC_PASSWORD_KEY);
        String lva_oldPassword = gob_tf_oldPassword.getText();
        String lva_newPassword = gob_tf_newPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword.getText();

        if (isPasswordDataValid(lva_oldPassword, lva_oldCachedPassword, lva_newPassword, lva_confirmPassword)) {

            lob_restClient = RestClientBuilder.buildUserClientWithAuth();

            lob_user = new User();
            lob_user.setEmail(lva_email);
            lob_user.setPassword(lva_newPassword);

            lob_restResponse = lob_restClient.changePassword(lob_user);

            if (lob_restResponse != null) {
                Utils.printResponseMessage(lob_restResponse);

                if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                    LastSessionStorage.setPassword(lva_newPassword);
                    gob_dataCache.replaceData(GC_PASSWORD_KEY, lva_newPassword);
                    lob_stage = (Stage) gob_btnSave.getScene().getWindow();
                    lob_stage.close();
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

    /**
     * Adds Key Listener to the TextFields
     * to react on key pressed ENTER
     */
    private void addKeyListener(){
        gob_tf_oldPassword.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClickChangePassword();
            }
        });

        gob_tf_newPassword.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClickChangePassword();
            }
        });

        gob_tf_confirmPassword.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClickChangePassword();
            }
        });
    }
}