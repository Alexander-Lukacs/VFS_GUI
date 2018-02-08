package controller;

import builder.ModelObjectBuilder;
import builder.RestClientBuilder;
import cache.DataCache;
import client.HttpMessage;
import client.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.interfaces.User;
import tools.AlertWindows;
import tools.Validation;

import java.io.IOException;
import java.util.Objects;

import static cache.DataCache.*;
import static controller.constants.AlertConstants.*;
import static controller.constants.SettingsConstants.GC_VFS;

public class LoginController {
    @FXML
    private AnchorPane gob_rootPane;

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

    private Stage stage = new Stage();
    private MainController mainController = new MainController();
    private static DataCache gob_dataCache;
    private RestClient gob_restClient;
    private HttpMessage gob_httpMessage;
    private boolean gva_isInputValid = false;

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

    public void onClick(ActionEvent event) throws IOException {
        User lob_user = ModelObjectBuilder.getUserObject();
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_password = gob_tf_loginPassword.getText();
        String lva_email = gob_tf_userLoginEmail.getText();

        //ein User Objekt "dummyUser" um die Valid Funktion zu ueberpruefen. da beim Login kein confirmpasswort oder username existiert
        //wird anschließend direkt null gesetzt
        User lob_dummyUser = checkIfInputsValid(lva_ip,lva_port,"test",lva_email,lva_password, lva_password);
        lob_dummyUser = null;

        //TODO Vom Server uebergebenes Objekt auseinanderziehen und dann in den Cache setzen motherfucker
        if (gva_isInputValid) {
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
                    /*TODO Alert Fenster*/
                            AlertWindows.ExceptionAlert(GC_EXCEPTION_TITLE, GC_EXCEPTION_HEADER, ex.getMessage(), ex);
                        }
                    }
                    gva_isInputValid=false;
    }

    public void onClickRegister(ActionEvent event) throws IOException {
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_name = gob_tf_newUserName.getText();
        String lva_email = gob_tf_newUserEmail.getText();
        String lva_password = gob_tf_registerPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword1.getText();

        User lob_user = checkIfInputsValid(lva_ip, lva_port, lva_name, lva_email, lva_password, lva_confirmPassword);


        if (gva_isInputValid) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            try {
                gob_restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);
                gob_httpMessage = gob_restClient.registerNewUser(lob_user);
                printMessage(gob_httpMessage);
                gob_tabPane.getSelectionModel().selectFirst();
            } catch (IOException e) {
                AlertWindows.ExceptionAlert(GC_EXCEPTION_TITLE, GC_EXCEPTION_HEADER, e.getMessage(), e);
            }
        }
        gva_isInputValid = false;
    }

    private User checkIfInputsValid(String lva_ip, String lva_port, String lva_name, String lva_email, String lva_password, String lva_confirmPassword) {

        User lob_user;

        if (Validation.nameValidation(lva_name)) {
            if (Validation.emailValidation(lva_email)) {
                if (Validation.passwordValidation(lva_password)) {
                    if (Validation.passwordEqualsValidation(lva_password, lva_confirmPassword)) {
                        lob_user = ModelObjectBuilder.getUserObject(lva_email, lva_password, lva_name);
                        if (Validation.ipValidation(lva_ip)) {
                            if (Validation.portValidation(lva_port)) {

                                gva_isInputValid = true;
                                return lob_user;

                            }
                            else {
                                AlertWindows.WarningAlert(GC_WARNING_TITLE, GC_WARNING_HEADER, GC_WARNING_PORT);
                            }
                        } else {
                            AlertWindows.WarningAlert(GC_WARNING_TITLE, GC_WARNING_HEADER, GC_WARNING_IP);
                        }
                    } else {
                        AlertWindows.WarningAlert(GC_WARNING_TITLE, GC_WARNING_HEADER, GC_WARNING_PASSWORD_NOT_EQUAL);
                    }
                } else {
                    AlertWindows.WarningAlert(GC_WARNING_TITLE, GC_WARNING_HEADER, GC_WARNING_PASSWORD);
                }
            } else {
                AlertWindows.WarningAlert(GC_WARNING_TITLE, GC_WARNING_HEADER, GC_WARNING_EMAIL);
            }
        } else {
            AlertWindows.WarningAlert(GC_WARNING_TITLE, GC_WARNING_HEADER, GC_WARNING_USERNAME);
           }
           return null;
    }

    private void cacheUser(User iob_user){
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
                AlertWindows.ErrorAlert(GC_ERROR_TITLE, GC_ERROR_HEADER, GC_ERROR_PASSWORD);
                System.out.println(status.getUserAddStatus());
                break;

            case 409:
                System.out.println(status.getUserAddStatus());
                break;
        }
    }

    public boolean getIsAdmin() {
        return Boolean.parseBoolean(gob_dataCache.get(GC_IS_ADMIN_KEY));
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
