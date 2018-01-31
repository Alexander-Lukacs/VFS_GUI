package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import static constants.SettingsConstants.ADMIN_ADD;
import static constants.SettingsConstants.CHANGE_IP_PORT;
import static constants.SettingsConstants.CHANGE_PW;

/**
 * Created by Mesut on 31.01.2018.
 */
public class ChangeIpPortController {


    private LoginController loginController = new LoginController();

    @FXML
    private ListView<String> gob_lvOptions;

    private ObservableList<String> gob_oblist;

    @FXML
    private AnchorPane gob_rootPane;


    public void initialize(){
        if(loginController.getIsAdmin()){
            gob_oblist = FXCollections.observableArrayList();
            gob_oblist.add(CHANGE_PW);
            gob_oblist.add(ADMIN_ADD);
            gob_oblist.add(CHANGE_IP_PORT);
            gob_lvOptions.setItems(gob_oblist);
            gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }else {
            gob_oblist = FXCollections.observableArrayList();
            gob_oblist.add(CHANGE_PW);
            gob_oblist.add(CHANGE_IP_PORT);
            gob_lvOptions.setItems(gob_oblist);
            gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
    }

    /**
     * Öffnet die View die ausgewählt wurde
     *
     * @param event
     * @throws IOException
     */
    public void loadView(MouseEvent event) throws IOException {

        if(gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_PW)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("ChangePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem() == ADMIN_ADD) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("addAdmin.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }
}

