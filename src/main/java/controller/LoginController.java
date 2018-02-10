package controller;

import builder.ModelObjectBuilder;
import builder.RestClientBuilder;
import cache.DataCache;
import client.HttpMessage;
import client.RestClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.interfaces.User;
import tools.AlertWindows;
import tools.Validation;
import tools.XmlTool;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.util.Objects;

import static cache.DataCache.*;
import static client.constants.HttpStatusCodes.*;
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
    private RestClient gob_restClient; //TODO Könnte lokal gemacht werden in OnClickRegister..

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
        System.out.println(gob_ipPortEmailPasswordArray[0]);
        System.out.println(gob_ipPortEmailPasswordArray[1]);
        System.out.println(gob_ipPortEmailPasswordArray[2]);
        System.out.println(gob_ipPortEmailPasswordArray[3]);
        setTextFromXmlToTf();
    }

    private void setTextFromXmlToTf() {
        gob_tf_ipAddress.setText(gob_ipPortEmailPasswordArray[0]);
        gob_tf_port.setText(gob_ipPortEmailPasswordArray[1]);
        gob_tf_userLoginEmail.setText(gob_ipPortEmailPasswordArray[2]);
        gob_tf_loginPassword.setText(gob_ipPortEmailPasswordArray[3]);
    }

    /**
     * reads the Textfields on Button Click
     * <p>
     * Validate inputs
     * <p>
     * sends inputs to Server, to Login.
     */

    public void onClick() {
        User lob_user = ModelObjectBuilder.getUserObject();
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_password = gob_tf_loginPassword.getText();
        String lva_email = gob_tf_userLoginEmail.getText();

        if (checkIfLoginDataValid(lva_ip, lva_port, lva_email, lva_password)) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);
            XmlTool.createXml(lva_ip, lva_port, lva_email, lva_password);

            RestClient restClient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port, lva_email, lva_password);
            try {
                lob_user.setEmail(lva_email);
                lob_user.setPassword(lva_password);
                lob_user = restClient.loginUser(lob_user);

                gob_dataCache.put(GC_PASSWORD_KEY, lva_password);
                cacheUser(lob_user);

                mainController.start(gob_stage);
                close();
            } catch (ProcessingException | IllegalArgumentException ex) {

                AlertWindows.createExceptionAlert(ex.getMessage(), ex);
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
        HttpMessage lob_httpMessage;

        if (checkIfRegisterDataValid(lva_ip, lva_port, lva_name, lva_email, lva_password, lva_confirmPassword)) {
            // TODO wenn man nach dem registrieren nicht direkt eingeloggt wird, ist das hier falsch
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            XmlTool.createXml(lva_ip, lva_port, lva_email, lva_password);
            User lob_user;

            lob_user = ModelObjectBuilder.getUserObject(lva_email, lva_password, lva_name);

            gob_restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);
            lob_httpMessage = gob_restClient.registerNewUser(lob_user);

            if (lob_httpMessage != null) {
                printMessage(lob_httpMessage);
                gob_tabPane.getSelectionModel().selectFirst();

                XmlTool.createXml(lva_ip, lva_port, lva_email, lva_password);
            }
        }
    }

    private boolean checkIfLoginDataValid(String iva_ip, String iva_port, String iva_email, String iva_password) {
        StringBuilder lob_sb = new StringBuilder();
        boolean validationFailure = false;

        if (!Validation.isEmailValid(iva_email)) {
            lob_sb.append(GC_WARNING_EMAIL);
            validationFailure = true;
        }

        if (!Validation.isIpValid(iva_ip)) {
            lob_sb.append(GC_WARNING_IP);
            validationFailure = true;
        }

        if (!Validation.isPortValid(iva_port)) {
            lob_sb.append(GC_WARNING_PORT);
            validationFailure = true;
        }

        if (!Validation.isPasswordValid(iva_password)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            validationFailure = true;
        }

        if (validationFailure) {
            AlertWindows.createWarningAlert(lob_sb.toString());
            return false;
        }

        return true;
    }

    private boolean checkIfRegisterDataValid(String iva_ip, String iva_port, String iva_name, String iva_email,
                                             String iva_password, String iva_confirmPassword) {
        StringBuilder lob_sb = new StringBuilder();
        boolean validationFailure = false;

        if (!Validation.isEmailValid(iva_email)) {
            lob_sb.append(GC_WARNING_EMAIL);
            validationFailure = true;
        }

        if (!Validation.isIpValid(iva_ip)) {
            lob_sb.append(GC_WARNING_IP);
            validationFailure = true;
        }
        if (!Validation.isPortValid(iva_port)) {
            lob_sb.append(GC_WARNING_PORT);
            validationFailure = true;
        }

        if (!Validation.nameValidation(iva_name)) {
            lob_sb.append(GC_WARNING_USERNAME);
            validationFailure = true;
        }

        if (!Validation.isPasswordValid(iva_password)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            validationFailure = true;
        }

        if (!Validation.passwordEqualsValidation(iva_password, iva_confirmPassword)) {
            lob_sb.append(GC_WARNING_PASSWORD_NOT_EQUAL);
            validationFailure = true;
        }

        if (validationFailure) {
            AlertWindows.createWarningAlert(lob_sb.toString());
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

    private void printMessage(HttpMessage status) {
        switch (status.getHttpStatus()) {
            case GC_HTTP_OK:
                AlertWindows.createInformationAlert(status.getUserAddStatus());
                break;
            case GC_HTTP_BAD_REQUEST:
                AlertWindows.createErrorAlert(status.getUserAddStatus());
                break;

            case GC_HTTP_CONFLICT:
                AlertWindows.createErrorAlert(status.getUserAddStatus());
                break;
        }
    }

    private void close() {
        ((Stage) gob_tf_userLoginEmail.getScene().getWindow()).close();
    }

    public void start(Stage stage) throws IOException {

        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().
                getResource("loginScreen.fxml")));

        stage.setScene(new Scene(root));
        stage.setTitle(GC_VFS);
        stage.show();
    }
}
