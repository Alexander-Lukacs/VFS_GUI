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


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static constants.SettingsConstants.*;
import static models.constants.UserConstants.*;

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

    private Pattern pattern;
    private Matcher matcher;

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

            RestClient restClient = RestClientBuilder.buildRestClientWithAuth(user.getEmail(), user.getPassword(), ip, port);
            restClient.loginUser(user);


            //TODO Vom Server uebergebenes Objekt auseinanderziehen und dann in den Cache setzen motherfucker
            if(true) {

                UserDataCache.put("IP",ip);
                UserDataCache.put("PORT",port);
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
            if (validateEmail(tfNewUserEmail.getText())) {
                if (validatePasswort(pwNewPassword.getText()) && pwNewPassword.getText().equals(pwNewPassword1.getText())) {
                    user = ModelBuilder.getUserObject(tfNewUserEmail.getText(), pwNewPassword.getText(), tfNewUserName.getText());
                    System.out.println("Userdaten Korrekt");
                    RestClient restClient = RestClientBuilder.buildRestClient(ip, port);
                    HttpMessage httpMessage = restClient.registerNewUser(user);

                    switch (httpMessage.getHttpStatus()){
                        case 200:
                            System.out.println("User registriert");
                            break;
                        case 400:
                            System.out.println("Email wird bereits benutzt");
                            break;

                        case 409:
                            System.out.println(httpMessage.getUserAddStatus());
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

//    public String encode(String input){
//        encodedString = Base64.getEncoder().encodeToString(input.getBytes());
//        return encodedString;
//    }

    private boolean validateEmail(String email) {

        pattern = Pattern.compile(VALID_EMAIL_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(email);
        return matcher.find();
    }

    private boolean validatePasswort(String password){
        pattern = Pattern.compile(VALID_PASSWORD_REGEX);
        matcher = pattern.matcher(password);
        return matcher.find();
    }


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
