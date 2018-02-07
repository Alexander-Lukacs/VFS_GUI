package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.classes.UserImpl;


import java.awt.*;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Base64;
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


    private boolean isAdmin = false;

    private Pattern pattern;
    private Matcher matcher;

    private String encodedString;

    private Stage stage = new Stage();
    private UserImpl user = new UserImpl();

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

        if(!NAME.equals(tfUserName.getText()) || !PASSWORD.equals(pwPasswort.getText()))
        {
            System.out.println("Benutzername oder Passwort falsch!");
        }
        else {
            tfUserName.setText("");
            pwPasswort.setText("");
            mainController.start(stage);
            close();
        }
    }

    public void onClickRegister(ActionEvent event) throws IOException {

        if (tfNewUserName.getText().length() >= 3) {
            if (validateEmail(tfNewUserEmail.getText())) {
                if (validatePasswort(pwNewPassword.getText()) && pwNewPassword.getText().equals(pwNewPassword1.getText())) {
                    user = new UserImpl(tfNewUserEmail.getText(), pwNewPassword.getText(), tfNewUserName.getText());
                    System.out.println("User registriert");
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

    public String encode(String input){
        encodedString = Base64.getEncoder().encodeToString(input.getBytes());
        return encodedString;
    }

    private boolean validateEmail(String email) {
        System.out.println("Case1: " + pwNewPassword.getText());
        System.out.println("Case2: " + pwNewPassword.getText());
        System.out.println("equal: " + pwNewPassword.getText().equals(pwNewPassword1.getText()));
        System.out.println("validation: " + validatePasswort(pwNewPassword.getText()));

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
