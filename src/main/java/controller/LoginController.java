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

import java.io.IOException;
import java.util.Objects;

import static cache.DataCache.*;
import static controller.constants.AlertConstants.GC_ERROR_PASSWORD;
import static controller.constants.SettingsConstants.GC_VFS;
import static tools.Validation.*;

public class LoginController {

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

    private final Stage stage = new Stage();
    private final MainController mainController = new MainController();
    private DataCache gob_dataCache;
    private RestClient gob_restClient; //TODO Könnte lokal gemacht werden in OnClickRegister..
    private HttpMessage gob_httpMessage; //TODO Könnte lokal gemacht werden in OnClickRegister..

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

        //TODO Vom Server uebergebenes Objekt auseinanderziehen und dann in den Cache setzen motherfucker
        if (checkIfLoginDataValid(lva_ip, lva_port, lva_email, lva_password)) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            RestClient restClient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port, lva_email, lva_password);
            try {
                lob_user.setEmail(lva_email);
                lob_user.setPassword(lva_password);
                lob_user = restClient.loginUser(lob_user);

                gob_dataCache.put(GC_PASSWORD_KEY, lva_password);
                cacheUser(lob_user);
                gob_tf_userLoginEmail.setText("");
                gob_tf_loginPassword.setText("");
                mainController.start(stage);
                close();
            } catch (IllegalArgumentException ex) {
                    /*TODO Alert Fenster Tests...*/
                AlertWindows.ExceptionAlert(ex.getMessage(), ex);
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


        if (checkIfRegisteDataValid(lva_ip, lva_port, lva_name, lva_email, lva_password, lva_confirmPassword)) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);
            User lob_user;

            lob_user = ModelObjectBuilder.getUserObject(lva_email, lva_password, lva_name);

            try {
                gob_restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);
                gob_httpMessage = gob_restClient.registerNewUser(lob_user);
                printMessage(gob_httpMessage);
                gob_tabPane.getSelectionModel().selectFirst();
            } catch (IOException e) {
                AlertWindows.ExceptionAlert(e.getMessage(), e);
            }
        }
    }

    private boolean checkIfLoginDataValid(String iva_ip, String iva_port, String iva_email, String iva_password) {

        return checkIfEmailValid(iva_email) &&
                checkIfIpPortValid(iva_ip, iva_port) &&
                checkIfPasswordValid(iva_password);

    }

    private boolean checkIfRegisteDataValid(String iva_ip, String iva_port, String iva_name, String iva_email,
                                            String iva_password, String iva_confirmPassword) {

        return checkIfEmailValid(iva_email) &&
                checkIfIpPortValid(iva_ip, iva_port) &&
                checkIfNameValid(iva_name) &&
                checkIfPasswordValid(iva_password) &&
                checkIfPasswordEqual(iva_password, iva_confirmPassword);
    }

    private void cacheUser(User iob_user) {
        gob_dataCache.put(GC_EMAIL_KEY, iob_user.getEmail());
        gob_dataCache.put(GC_NAME_KEY, iob_user.getName());
        gob_dataCache.put(GC_ADMIN_ID_KEY, String.valueOf(iob_user.getAdminId()));
        gob_dataCache.put(GC_USER_ID_KEY, String.valueOf(iob_user.getUserId()));
        gob_dataCache.put(GC_IS_ADMIN_KEY, String.valueOf(iob_user.getIsAdmin()));

        // System.out.println(gob_dataCache.get(GC_IS_ADMIN_KEY));
    }

    private void printMessage(HttpMessage status) {
        // TODO kein sout sondern Nachrichten im Client
        switch (status.getHttpStatus()) {
            case 200:
                System.out.println(status.getUserAddStatus());
                break;
            case 400:
                //TODO UserStatus oder unser String?
                AlertWindows.ErrorAlert(GC_ERROR_PASSWORD);
                System.out.println(status.getUserAddStatus());
                break;

            case 409:
                System.out.println(status.getUserAddStatus());
                break;
        }
    }

    private void close() {
        ((Stage) gob_tf_userLoginEmail.getScene().getWindow()).close();
    }

    public void start(Stage stage) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("loginScreen.fxml")));
        stage.setScene(new Scene(root));
        stage.setTitle(GC_VFS);
        stage.show();
    }
}
