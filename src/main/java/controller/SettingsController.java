package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import static controller.constants.SettingsConstants.*;


public class SettingsController {
    private final controller.ListView gob_listView = new controller.ListView();
    @FXML
    private ListView<String> gob_lvOptions;
    @FXML
    private AnchorPane gob_rootPane;

    private MainController gob_mainController;

    /**
     * Initialize of ListView
     */
    public void initialize() {
        gob_listView.loadSettingsList(gob_lvOptions);
    }

    public void initData(MainController iob_mainController) {
        gob_mainController = iob_mainController;
    }

    /**
     * shows the selection options in Settings View
     */
    public void loadView() {
        try {

            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_PW)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/changePW.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_ADMIN_ADD)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/addAdmin.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_IP_PORT)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/changeIpPort.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                ChangeIpPortController lob_controller = lob_loader.getController();
                lob_controller.initData(gob_mainController);
                gob_rootPane.getChildren().setAll(lob_pane);
            }
            // TODO schauen ob es evtl. eine besser Methode gibt, als ein leerer catch block
        } catch (Exception ignore) {
        }
    }
}
