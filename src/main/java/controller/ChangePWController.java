package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import static constants.SettingsConstants.CHANGE_PW;

public class ChangePWController {

    @FXML
    private ListView<String> gob_lvOptions;

    private ObservableList<String> gob_oblist;

    @FXML
    private Button gob_btnSave;

    public void initialize()
    {
        gob_oblist = FXCollections.observableArrayList();
        gob_oblist.add(CHANGE_PW);
        gob_lvOptions.setItems(gob_oblist);
        gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
}
