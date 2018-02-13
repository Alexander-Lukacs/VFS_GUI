package controller;

import builder.RestClientBuilder;
import cache.DataCache;
import client.RestClient;
import client.RestResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.classes.User;
import tools.AlertWindows;
import tools.Utils;
import tools.Validation;
import tools.XmlTool;

import java.io.IOException;
import java.util.Objects;

import static cache.DataCache.*;
import static client.constants.HttpStatusCodes.GC_HTTP_OK;
import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static controller.constants.SettingsConstants.GC_VFS;
import static tools.constants.AlertConstants.*;

public class LoginController {
    private final Stage gob_stage = new Stage();
    private final MainController mainController = new MainController();

    @FXML
    private Button gob_btnLogin;
    @FXML
    private PasswordField gob_tf_loginPassword;
    @FXML
    private TextField gob_tf_userLoginEmail;
    @FXML
    private TextField gob_tf_newUserName;
    @FXML
    private TextField gob_tf_newUserEmail;
    @FXML
    private TextField gob_tf_registerPassword;
    @FXML
    private TextField gob_tf_confirmPassword1;
    @FXML
    private TextField gob_tf_ipAddress;
    @FXML
    private TextField gob_tf_port;
    @FXML
    private TabPane gob_tabPane = new TabPane();
    private String[] gob_ipPortEmailPasswordArray = new String[4];
    private DataCache gob_dataCache;
    private Thread gob_notifyThread;

    @FXML
    public void initialize() {
        gob_dataCache = DataCache.getDataCache();
        gob_btnLogin.setOnKeyPressed(
                event -> {
                    switch (event.getCode()) {
                        case ENTER:
                            gob_btnLogin.fire();
                    }
                }
        );
        gob_ipPortEmailPasswordArray = XmlTool.readFromXml();
        setTextFromXmlToTf();
    }

    private void setTextFromXmlToTf() {
        gob_tf_ipAddress.setText(gob_ipPortEmailPasswordArray[0]);
        gob_tf_port.setText(gob_ipPortEmailPasswordArray[1]);
        gob_tf_userLoginEmail.setText(gob_ipPortEmailPasswordArray[2]);
        gob_tf_loginPassword.setText(gob_ipPortEmailPasswordArray[3]);
    }

    /**
     * reads the text fields on Button Click
     * Validate inputs
     * sends inputs to Server, to Login.
     */

    public void onClick() {
        RestClient lob_restClient;
        User lob_user = new User();

        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_password = gob_tf_loginPassword.getText();
        String lva_email = gob_tf_userLoginEmail.getText();

        if (checkIfLoginDataValid(lva_ip, lva_port, lva_email, lva_password)) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            lob_restClient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port, lva_email, lva_password);

            try {
                lob_user.setEmail(lva_email);
                lob_user.setPassword(lva_password);
                lob_user = lob_restClient.loginUser(lob_user);

                if (lob_user != null) {
                    gob_dataCache.put(GC_PASSWORD_KEY, lva_password);
                    cacheUser(lob_user);
                    XmlTool.createXml(lva_ip, lva_port, lva_email, lva_password);

                    mainController.start(gob_stage);
                    close();
                }
            } catch (IllegalArgumentException ex) {
                new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
            }
        }
    }

    public void onClickRegister() {
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_name = gob_tf_newUserName.getText();
        String lva_email = gob_tf_newUserEmail.getText();
        String lva_password = gob_tf_registerPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword1.getText();

        RestResponse lob_restResponse;
        RestClient lob_restClient;
        User lob_user;

        if (checkIfRegisterDataValid(lva_ip, lva_port, lva_name, lva_email, lva_password, lva_confirmPassword)) {
            // TODO wenn man nach dem registrieren nicht direkt eingeloggt wird, ist das hier falsch
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            lob_user = new User(lva_email, lva_password, lva_name);

            lob_restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);
            lob_restResponse = lob_restClient.registerNewUser(lob_user);

            if (lob_restResponse != null) {
                Utils.printResponseMessage(lob_restResponse);

                if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                    gob_tabPane.getSelectionModel().selectFirst();
                    XmlTool.createXml(lva_ip, lva_port, lva_email, lva_password);
                }

            }
        }
    }

    private boolean checkIfLoginDataValid(String iva_ip, String iva_port, String iva_email, String iva_password) {
        StringBuilder lob_sb = new StringBuilder();
        boolean lva_validationFailure = false;

        if (!Validation.isEmailValid(iva_email)) {
            lob_sb.append(GC_WARNING_EMAIL);
            gob_tf_userLoginEmail.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.isIpValid(iva_ip)) {
            lob_sb.append(GC_WARNING_IP);
            gob_tf_ipAddress.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.isPortValid(iva_port)) {
            lob_sb.append(GC_WARNING_PORT);
            gob_tf_port.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.isPasswordValid(iva_password)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            gob_tf_loginPassword.setText("");
            lva_validationFailure = true;
        }

        if (lva_validationFailure) {
            new AlertWindows().createWarningAlert(lob_sb.toString());
            return false;
        }

        return true;
    }

    private boolean checkIfRegisterDataValid(String iva_ip, String iva_port, String iva_name, String iva_email,
                                             String iva_password, String iva_confirmPassword) {
        StringBuilder lob_sb = new StringBuilder();
        boolean lva_validationFailure = false;

        if (!Validation.isEmailValid(iva_email)) {
            lob_sb.append(GC_WARNING_EMAIL);
            gob_tf_newUserEmail.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.isIpValid(iva_ip)) {
            lob_sb.append(GC_WARNING_IP);
            gob_tf_ipAddress.setText("");
            lva_validationFailure = true;
        }
        if (!Validation.isPortValid(iva_port)) {
            lob_sb.append(GC_WARNING_PORT);
            gob_tf_port.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.nameValidation(iva_name)) {
            lob_sb.append(GC_WARNING_USERNAME);
            gob_tf_newUserName.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.isPasswordValid(iva_password)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            gob_tf_registerPassword.setText("");
            gob_tf_confirmPassword1.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.passwordEqualsValidation(iva_password, iva_confirmPassword)) {
            lob_sb.append(GC_WARNING_PASSWORD_NOT_EQUAL);
            gob_tf_confirmPassword1.setText("");
            gob_tf_registerPassword.setText("");
            lva_validationFailure = true;
        }

        if (lva_validationFailure) {
            new AlertWindows().createWarningAlert(lob_sb.toString());
            return false;
        }

        return true;
    }

    private void cacheUser(User iob_user) {
        gob_dataCache.put(GC_EMAIL_KEY, iob_user.getEmail());
        gob_dataCache.put(GC_NAME_KEY, iob_user.getName());
        gob_dataCache.put(GC_ADMIN_ID_KEY, String.valueOf(iob_user.getAdminId()));
        gob_dataCache.put(GC_USER_ID_KEY, String.valueOf(iob_user.getUserId()));
        gob_dataCache.put(GC_IS_ADMIN_KEY, String.valueOf(iob_user.getIsAdmin()));
    }

    private void close() {
        ((Stage) gob_tf_userLoginEmail.getScene().getWindow()).close();
    }

    public void start(Stage iob_stage) throws IOException {
        Parent lob_root;
        lob_root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().
                getResource("views/loginScreen.fxml")));

        iob_stage.setScene(new Scene(lob_root));
        iob_stage.setTitle(GC_VFS);
        iob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
        iob_stage.show();
    }
}
