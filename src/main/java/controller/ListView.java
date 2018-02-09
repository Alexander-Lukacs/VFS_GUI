package controller;

import cache.DataCache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;

import static cache.DataCache.GC_IS_ADMIN_KEY;
import static controller.constants.SettingsConstants.*;

public class ListView {
    public void loadList(javafx.scene.control.ListView<String> gob_lvOptions) {
        DataCache dataCache = DataCache.getDataCache();
        boolean lva_isAdmin = Boolean.parseBoolean(dataCache.get(GC_IS_ADMIN_KEY));
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
}
