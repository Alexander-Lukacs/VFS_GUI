package controller;

import cache.DataCache;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import tools.Validation;

import java.io.IOException;

import static cache.DataCache.GC_IP_KEY;
import static cache.DataCache.GC_PORT_KEY;
import static controller.constants.SettingsConstants.GC_ADMIN_ADD;
import static controller.constants.SettingsConstants.GC_CHANGE_PW;

/**
 * Created by Mesut on 31.01.2018.
 */
public class ChangeIpPortController {


    @FXML
    private ListView<String> gob_lvOptions;

    @FXML
    private AnchorPane gob_rootPane;

    @FXML
    private TextField gob_tfServerIp;

    @FXML
    private TextField gob_tfPort;

    private final controller.ListView listView = new controller.ListView();

    public void initialize() {

        listView.loadList(gob_lvOptions);
    }

    /**
     * Öffnet die View die ausgewählt wurde
     *
     * @throws IOException
     */
    public void loadView() throws IOException {

        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_PW)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("ChangePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_ADMIN_ADD)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("addAdmin.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }

    public void onClick() {
        DataCache lob_datacache = DataCache.getDataCache();

        String lva_ip = gob_tfServerIp.getText();
        String lva_port = gob_tfPort.getText();

        if (!Validation.isIpNotValid(lva_ip)) {
            lob_datacache.replaceData(GC_IP_KEY, lva_ip);
        }
        if (!Validation.isPortNotValid(lva_port)) {
            lob_datacache.replaceData(GC_PORT_KEY, lva_port);
        }
        System.out.println(lob_datacache.get(GC_IP_KEY));
        System.out.println(lob_datacache.get(GC_PORT_KEY));
    }
}

