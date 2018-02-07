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
    private DataCache dataCache;

    public void initialize() {
        dataCache = DataCache.getDataCache();

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
     * Beim Klicken des Buttons wird geprüft ob der User ein Admin ist,
     * falls ja, dann öffne die View settingAdmin.fxml
     * falls nein, dann kommt eine Fehlermeldung
     */

    public void onClick(ActionEvent event) throws IOException {
        User lob_user = ModelObjectBuilder.getUserObject();
        String lva_ip = gob_tf_ipAddress.getText();
        String lva_port = gob_tf_port.getText();
        String lva_password = gob_tf_loginPassword.getText();
        String lva_email = gob_tf_userName.getText();

        //TODO Vom Server uebergebenes Objekt auseinanderziehen und dann in den Cache setzen motherfucker
        if (Validation.ipValidation(lva_ip) && Validation.portValidation(lva_port)) {
            dataCache.put(GC_IP_KEY, lva_ip);
            dataCache.put(GC_PORT_KEY, lva_port);
            System.out.println("ip und Port gehen klar");

            if (Validation.passwordValidation(lva_password) && Validation.emailValidation(lva_email)) {

                RestClient restClient = RestClientBuilder.buildRestClientWithAuth(lob_user.getEmail(),
                        lob_user.getPassword(), lva_ip, lva_port);

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

    //TODO Messege Objekt statt println im Switch bitch case mase
    public void onClickRegister(ActionEvent event) throws IOException {
        String lva_ip = dataCache.get(GC_IP_KEY);
        String lva_port = dataCache.get(GC_PORT_KEY);
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
                    System.out.println("Userdaten Korrekt");

                    if (Validation.ipValidation(lva_ip) && Validation.portValidation(lva_port)) {
                        dataCache.put(GC_IP_KEY, lva_ip);
                        dataCache.put(GC_PORT_KEY, lva_port);

                        RestClient restClient = RestClientBuilder.buildRestClient(lva_ip, lva_port);
                        HttpMessage httpMessage = restClient.registerNewUser(lob_user);
                        printMessage(httpMessage);
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
