package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;

import static controller.constants.SettingsConstants.GC_ADMIN_ADD;
import static controller.constants.SettingsConstants.GC_CHANGE_IP_PORT;
import static controller.constants.SettingsConstants.GC_CHANGE_PW;

//TODO protected?
public class ListView {

    private final LoginController loginController = new LoginController();

    public void loadList(javafx.scene.control.ListView<String> gob_lvOptions){

        ObservableList<String> lob_oblist;

        if(loginController.getIsAdmin()){
            lob_oblist = FXCollections.observableArrayList();
            lob_oblist.add(GC_CHANGE_PW);
            lob_oblist.add(GC_ADMIN_ADD);
            lob_oblist.add(GC_CHANGE_IP_PORT);
            gob_lvOptions.setItems(lob_oblist);
            gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }else {
            lob_oblist = FXCollections.observableArrayList();
            lob_oblist.add(GC_CHANGE_PW);
            lob_oblist.add(GC_CHANGE_IP_PORT);
            gob_lvOptions.setItems(lob_oblist);
            gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
    }
}
