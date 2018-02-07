package controller;

import builder.ModelBuilder;
import builder.RestClientBuilder;
import cache.UserDataCache;
import client.HttpMessage;
import client.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.interfaces.User;
import tools.Validation;


import java.io.IOException;

import static constants.SettingsConstants.*;

public class LoginController{

    @FXML
    private AnchorPane gob_rootPane;

    @FXML
    private Button btnLogin;

    @FXML
    private PasswordField pwPasswort;

    @FXML
    private TextField tfUserName;

    @FXML
    private TextField tfNewUserName;

    @FXML
    private TextField tfNewUserEmail;

    @FXML
    private TextField pwNewPassword;

    @FXML
    private TextField pwNewPassword1;


    @FXML
    private TextField tfIPAdress;

    @FXML
    private TextField tfPort;


    private boolean isAdmin = true;

    private String encodedString;

    private Stage stage = new Stage();

    private MainController mainController = new MainController();

    public void initialize()
    {
        btnLogin.setOnKeyPressed(
                event -> {
                    switch(event.getCode()) {
                        case ENTER: btnLogin.fire();
                    }
                }
        );
    }

    /**
        Beim Klicken des Buttons wird geprüft ob der User ein Admin ist,
        falls ja, dann öffne die View settingAdmin.fxml
        falls nein, dann kommt eine Fehlermeldung
     */

    public void onClick(ActionEvent event) throws IOException
    {
        User user = ModelBuilder.getUserObject();

            String ip = tfIPAdress.getText();
            String port = tfPort.getText();

            //TODO Vom Server uebergebenes Objekt auseinanderziehen und dann in den Cache setzen motherfucker
            if(Validation.ipValidation(ip) && Validation.portValidation(port)) {
                System.out.println("ip und Port gehen klar");

                if (Validation.passwordValidation(pwPasswort.getText()) && Validation.emailValidation(tfUserName.getText())) {

                    RestClient restClient = RestClientBuilder.buildRestClientWithAuth(user.getEmail(), user.getPassword(), ip, port);
                    restClient.loginUser(user);

                    if(/*was wir von flo bekommen ==*/ true) {

                        UserDataCache.put("IP", ip);
                        UserDataCache.put("PORT", port);
                        //UserDataCache.put("NAME",);
                        //passwort
                        //email
                        //admin
                        //... usw
                        tfUserName.setText("");
                        pwPasswort.setText("");
                        mainController.start(stage);
                        close();
                    }
                }
            }
            else{
                System.out.println("User setzt Login ein......... schlug fehl!");
            }
    }

    //TODO Messege Objekt statt println im Switch bitch case mase
    public void onClickRegister(ActionEvent event) throws IOException {
        User user;

        String ip = tfIPAdress.getText();
        String port = tfPort.getText();

        if (tfNewUserName.getText().length() >= 3) {
            if (Validation.emailValidation(tfNewUserEmail.getText())) {
                if (Validation.passwordValidation(pwNewPassword.getText()) && pwNewPassword.getText().equals(pwNewPassword1.getText())) {
                    user = ModelBuilder.getUserObject(tfNewUserEmail.getText(), pwNewPassword.getText(), tfNewUserName.getText());
                    System.out.println("Userdaten Korrekt");
                    if(Validation.ipValidation(ip) && Validation.portValidation(port)) {
                        RestClient restClient = RestClientBuilder.buildRestClient(ip, port);
                        HttpMessage httpMessage = restClient.registerNewUser(user);
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

    private void printMessage(HttpMessage status){
        switch (status.getHttpStatus()) {
            case 200:
                System.out.println("User registriert");
                break;
            case 400:
                System.out.println("Email wird bereits benutzt");
                break;

            case 409:
                System.out.println(status.getUserAddStatus());
        }
    }

//    public String encode(String input){
//        encodedString = Base64.getEncoder().encodeToString(input.getBytes());
//        return encodedString;
//    }


    public boolean getIsAdmin()
    {
        return isAdmin;
    }

    public void close() {
        ((Stage)tfUserName.getScene().getWindow()).close();
    }

    public void start(Stage stage) throws IOException{
        Parent root;
        root = FXMLLoader.load(getClass().getClassLoader().getResource("loginScreen.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle(VFS);
        stage.show();
    }

}
