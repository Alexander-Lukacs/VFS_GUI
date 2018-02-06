package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;

import static constants.SettingsConstants.ADMIN_ADD;
import static constants.SettingsConstants.CHANGE_IP_PORT;
import static constants.SettingsConstants.CHANGE_PW;

public class ListView {

    private LoginController loginController = new LoginController();

    private ObservableList<String> gob_oblist;

    public void loadList(javafx.scene.control.ListView<String> gob_lvOptions){

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
}