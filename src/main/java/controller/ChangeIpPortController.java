package controller;

import cache.DataCache;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import tools.AlertWindows;
import tools.Validation;
import tools.XmlTool;

import java.io.IOException;

import static cache.DataCache.*;
import static controller.constants.SettingsConstants.GC_ADMIN_ADD;
import static controller.constants.SettingsConstants.GC_CHANGE_PW;
import static tools.constants.AlertConstants.GC_WARNING_IP_PORT;
import static tools.constants.AlertConstants.GC_WARNING_PORT;

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

        listView.loadSettingsList(gob_lvOptions);
    }

    /**
     * Öffnet die View die ausgewählt wurde
     *
     * @throws IOException
     */
    public void loadView() throws IOException {

        try {


            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_PW)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changePW.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_ADMIN_ADD)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("addAdmin.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
        } catch (Exception e) {

        }
    }

    public void onClick() {
        DataCache lob_datacache = DataCache.getDataCache();

        String lva_ip = gob_tfServerIp.getText();
        String lva_port = gob_tfPort.getText();

        if (Validation.isIpValid(lva_ip)) {
            if (Validation.isPortValid(lva_port)) {
                lob_datacache.replaceData(GC_IP_KEY, lva_ip);
                lob_datacache.replaceData(GC_PORT_KEY, lva_port);
                XmlTool.createXml(lva_ip, lva_port, lob_datacache.get(GC_EMAIL_KEY), lob_datacache.get(GC_PASSWORD_KEY));
            } else {
                AlertWindows.createWarningAlert(GC_WARNING_PORT);
            }
        } else {
            AlertWindows.createWarningAlert(GC_WARNING_IP_PORT);
        }
    }
}

