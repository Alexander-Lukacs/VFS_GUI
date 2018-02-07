package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import static constants.SettingsConstants.*;


public class SettingsController {

    @FXML
    private ListView<String> gob_lvOptions;

    @FXML
    private AnchorPane gob_rootPane;

    private controller.ListView listView = new controller.ListView();

    /**
     * Initialize of ListView
     */
    public void initialize()
    {
        listView.loadList(gob_lvOptions);
    }


    /**
     * shows the choosen Settings View
     *
     * @param event
     * @throws IOException
     */
    public void loadView(MouseEvent event) throws IOException {

        if (gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_PW) {
           FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("ChangePW.fxml"));
           AnchorPane lob_pane = lob_loader.load();
           gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem() == ADMIN_ADD) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("addAdmin.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_IP_PORT) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changeIpPort.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }
}
