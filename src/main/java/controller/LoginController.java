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
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.interfaces.User;
import tools.Validation;

import java.io.IOException;
import java.util.Objects;

import static cache.DataCache.GC_IP_KEY;
import static cache.DataCache.GC_PORT_KEY;
import static constants.SettingsConstants.VFS;

public class LoginController {
    @FXML
    private AnchorPane gob_rootPane;

    @FXML
    private Button gob_btnLogin;

    @FXML
    private PasswordField gob_tf_loginPassword;

    @FXML
    private TextField gob_tf_userName;

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

    private boolean isAdmin = true;
    private String encodedString;
    private Stage stage = new Stage();
    private MainController mainController = new MainController();
    private DataCache gob_dataCache;
    private RestClient gob_restClient;
    private HttpMessage gob_httpMessage;

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
     *
     * Validate inputs
     *
     * sends inputs to Server, to Login.
     *
     */

    public void onClick(ActionEvent event) throws IOException {
        User lob_user = ModelObjectBuilder.getUserObject();
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_password = gob_tf_loginPassword.getText();
        String lva_email = gob_tf_userName.getText();

        //TODO Vom Server uebergebenes Objekt auseinanderziehen und dann in den Cache setzen motherfucker
        if (Validation.ipValidation(lva_ip) && Validation.portValidation(lva_port)) {
            gob_dataCache.put(GC_IP_KEY, lva_ip);
            gob_dataCache.put(GC_PORT_KEY, lva_port);
            System.out.println("ip und Port Validation gehen klar");

            if (Validation.passwordValidation(lva_password) && Validation.emailValidation(lva_email)) {

                RestClient restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);

                restClient.loginUser(lob_user);

                if (/*was wir von flo bekommen ==*/ true) {

                    //UserDataCache.put("NAME",);
                    //passwort
                    //email
                    //admin
                    //... usw
                    gob_tf_userName.setText("");
                    gob_tf_loginPassword.setText("");
                    mainController.start(stage);
                    close();
                }
            }
        } else {
            System.out.println("User setzt Login ein......... schlug fehl!");
        }
    }

    public void onClickRegister(ActionEvent event) throws IOException {
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_name = gob_tf_newUserName.getText();
        String lva_email = gob_tf_newUserEmail.getText();
        String lva_password = gob_tf_registerPassword.getText();
        String lva_confirmPassword = gob_tf_confirmPassword1.getText();

        User lob_user;

        if (Validation.nameValidation(lva_name)) {
            if (Validation.emailValidation(lva_email)) {

                if (Validation.passwordValidation(lva_password) &&
                    Validation.passwordEqualsValidation(lva_password, lva_confirmPassword)) {

                    lob_user = ModelObjectBuilder.getUserObject(lva_email, lva_password, lva_name);

                    if (Validation.ipValidation(lva_ip) && Validation.portValidation(lva_port)) {
                        gob_dataCache.put(GC_IP_KEY, lva_ip);
                        gob_dataCache.put(GC_PORT_KEY, lva_port);

                        gob_restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);
                        gob_httpMessage = gob_restClient.registerNewUser(lob_user);
                        printMessage(gob_httpMessage);
                    }

                } else {
                    System.out.println("Kein korrektes Passwort oder Passwort stimmt nicht überein");
                }
            } else {
                System.out.println("Kein korrekte E-Mail-Adresse");
            }
        } else {
            System.out.println("Username muss min. 3 Buchstaben lang sein");
        }
    }

    private void printMessage(HttpMessage status) {
        // TODO kein sout sondern Nachrichten im Client
        switch (status.getHttpStatus()) {
            case 200:
                System.out.println(status.getUserAddStatus());
                break;
            case 400:
                System.out.println(status.getUserAddStatus());
                break;

            case 409:
                System.out.println(status.getUserAddStatus());
                break;
        }
    }

//    public String encode(String input){
//        encodedString = Base64.getEncoder().encodeToString(input.getBytes());
//        return encodedString;
//    }


    public boolean getIsAdmin() {
        return isAdmin;
    }

    private void close() {
        ((Stage) gob_tf_userName.getScene().getWindow()).close();
    }

    public void start(Stage stage) throws IOException {
        Parent root;
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("loginScreen.fxml")));
        stage.setScene(new Scene(root));
        stage.setTitle(VFS);
        stage.show();
    }

}
