package controller;

import cache.DataCache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import models.classes.UserImpl;
import models.interfaces.User;

import java.util.ArrayList;
import java.util.List;

import static cache.DataCache.GC_IS_ADMIN_KEY;
import static controller.constants.SettingsConstants.*;

public class ListView {
    public void loadSettingsList(javafx.scene.control.ListView<String> gob_lvOptions) {
        DataCache lob_dataCache = DataCache.getDataCache();
        boolean lva_isAdmin = Boolean.parseBoolean(lob_dataCache.get(GC_IS_ADMIN_KEY));
        ObservableList<String> lob_observableList;

        if (lva_isAdmin) {
            lob_observableList = FXCollections.observableArrayList();
            lob_observableList.add(GC_CHANGE_PW);
            lob_observableList.add(GC_ADMIN_ADD);
            lob_observableList.add(GC_CHANGE_IP_PORT);
            gob_lvOptions.setItems(lob_observableList);
            gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        } else {
            lob_observableList = FXCollections.observableArrayList();
            lob_observableList.add(GC_CHANGE_PW);
            lob_observableList.add(GC_CHANGE_IP_PORT);
            gob_lvOptions.setItems(lob_observableList);
            gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
    }

    public void loadUserList(javafx.scene.control.ListView<String> iob_lvUser, List<UserImpl> iob_userList){

        List<String> lli_userList = new ArrayList<>();

        for (User user : iob_userList) {
            if (!user.getIsAdmin()) {
                lli_userList.add(user.getEmail());
            }
        }

        ObservableList<String> lob_observableList = FXCollections.observableArrayList(lli_userList);

        iob_lvUser.setItems(lob_observableList);
    }
}
