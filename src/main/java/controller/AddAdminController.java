package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import static controller.constants.SettingsConstants.GC_CHANGE_IP_PORT;
import static controller.constants.SettingsConstants.GC_CHANGE_PW;

/**
 * Created by Mesut on 31.01.2018.
 */
public class AddAdminController {

    @FXML
    private ListView<String> gob_lvOptions;

    @FXML
    private AnchorPane gob_rootPane;

    private final controller.ListView listView = new controller.ListView();

    public void initialize() {
        listView.loadList(gob_lvOptions);
    }

    public void loadView() throws IOException {

        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_PW)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("ChangePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_IP_PORT)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changeIpPort.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }

    public void onClick() {

    }
}
