package controller.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import models.classes.RestResponse;
import models.classes.User;
import restful.clients.UserRestClient;
import tools.AlertWindows;
import tools.Utils;
import tools.Validation;
import tools.xmlTools.LastSessionStorage;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.util.Objects;

import static cache.DataCache.*;
import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static controller.constants.SettingsConstants.GC_VFS;
import static restful.constants.HttpStatusCodes.GC_HTTP_OK;
import static tools.constants.AlertConstants.*;

/**
 * Created by Mesut on 01.03.2018.
 * LoginController
 * Controlls the Login Scene
 * allows the User to Login
 */
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
    private TextField gob_tf_confirmPassword;
    @FXML
    private TextField gob_tf_ipAddress;
    @FXML
    private TextField gob_tf_port;
    @FXML
    private TabPane gob_tabPane = new TabPane();

    private DataCache gob_dataCache;

    @FXML
    public void initialize() {

        addKeyListener();


        gob_dataCache = DataCache.getDataCache();
        gob_btnLogin.setOnKeyPressed(
                event -> {
                    switch (event.getCode()) {
                        case ENTER:
                            gob_btnLogin.fire();
                    }
                }
        );

        setTextFromXmlToTf();
    }

    private void setTextFromXmlToTf() {
        gob_tf_ipAddress.setText(LastSessionStorage.getIp());
        gob_tf_port.setText(LastSessionStorage.getPort());
        gob_tf_userLoginEmail.setText(LastSessionStorage.getEmail());
        gob_tf_loginPassword.setText(LastSessionStorage.getPassword());
    }

    /**
     * reads the text fields on Button Click
     * Validate inputs
     * sends inputs to Server, to Login.
     */
    public void onClick() {
        UserRestClient lob_restClient;
        User lob_user = new User();

        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_password = gob_tf_loginPassword.getText();
        String lva_email = gob_tf_userLoginEmail.getText();

        if (checkIfLoginDataValid(lva_ip, lva_port, lva_email, lva_password)) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            lob_restClient = RestClientBuilder.buildUserClientWithAuth(lva_ip, lva_port, lva_email, lva_password);

            try {
                lob_user.setEmail(lva_email);
                lob_user.setPassword(lva_password);
                lob_user = lob_restClient.loginUser(lob_user);

                if (lob_user != null) {
                    gob_dataCache.put(GC_PASSWORD_KEY, lva_password);
                    cacheUser(lob_user);

                    LastSessionStorage.setIp(lva_ip);
                    LastSessionStorage.setPort(lva_port);
                    LastSessionStorage.setEmail(lva_email);
                    LastSessionStorage.setPassword(lva_password);

                    mainController.start(gob_stage);
                    close();
                }
            } catch (IllegalArgumentException | ProcessingException ex) {
                new AlertWindows().createExceptionAlert(ex.getMessage(), ex);
            }
        }
    }

    /**
     * reads the text fields on Button Click
     * Validate inputs
     * sends inputs to Server, to Register.
     */
    public void onClickRegister() {
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_name = gob_tf_newUserName.getText();
        String lva_email = gob_tf_newUserEmail.getText();
        String lva_password = gob_tf_registerPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword.getText();

        RestResponse lob_restResponse;
        UserRestClient lob_restClient;
        User lob_user;

        if (checkIfRegisterDataValid(lva_ip, lva_port, lva_name, lva_email, lva_password, lva_confirmPassword)) {
            // TODO wenn man nach dem registrieren nicht direkt eingeloggt wird, ist das hier falsch
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);

            lob_user = new User(lva_email, lva_password, lva_name);

            lob_restClient = RestClientBuilder.buildUserClient(lva_ip, lva_port);
            lob_restResponse = lob_restClient.registerNewUser(lob_user);

            if (lob_restResponse != null) {
                Utils.printResponseMessage(lob_restResponse);

                if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                    gob_tabPane.getSelectionModel().selectFirst();

                    LastSessionStorage.setPort(lva_port);
                    LastSessionStorage.setIp(lva_ip);
                    LastSessionStorage.setEmail(lva_email);
                    LastSessionStorage.setPassword(lva_password);
                }

            }
        }
        // setTextFromXmlToTf(); --> ist voll doof wegen Ip und port.... lol
    }

    /**
     * test if User inputs are Valid for Login
     *
     * @param iva_ip       contains the server ip
     * @param iva_port     contains the server port
     * @param iva_email    contains the Users email address
     * @param iva_password contains the Users passowrd
     * @return true if the inputs are valid
     */
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

        if (Validation.isPasswordInvalid(iva_password)) {
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

    /**
     * test if User inputs are Valid for register
     *
     * @param iva_ip              contains the server ip
     * @param iva_port            contains the server port
     * @param iva_name            contains the User name
     * @param iva_email           contains the Users email address
     * @param iva_password        contains the Users passowrd
     * @param iva_confirmPassword contains the confirmed passowrd
     * @return true if the inputs are valid
     */
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

        if (Validation.isPasswordInvalid(iva_password)) {
            lob_sb.append(GC_WARNING_PASSWORD);
            gob_tf_registerPassword.setText("");
            gob_tf_confirmPassword.setText("");
            lva_validationFailure = true;
        }

        if (!Validation.passwordEqualsValidation(iva_password, iva_confirmPassword)) {
            lob_sb.append(GC_WARNING_PASSWORD_NOT_EQUAL);
            gob_tf_confirmPassword.setText("");
            gob_tf_registerPassword.setText("");
            lva_validationFailure = true;
        }

        if (lva_validationFailure) {
            new AlertWindows().createWarningAlert(lob_sb.toString());
            return false;
        }

        return true;
    }

    /**
     * saves User data in Cache
     *
     * @param iob_user a User object, contains the USer data (name, email, adminId, id, admin(boolean))
     */
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

    /**
     * Adds Key Listener to the TextFields
     * to react on key pressed ENTER
     */
    private void addKeyListener() {
        gob_tf_userLoginEmail.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClick();
            }
        });

        gob_tf_loginPassword.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClick();
            }
        });

        gob_tf_newUserEmail.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClickRegister();
            }
        });

        gob_tf_registerPassword.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClickRegister();
            }
        });

        gob_tf_confirmPassword.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                onClickRegister();
            }
        });
    }
}
