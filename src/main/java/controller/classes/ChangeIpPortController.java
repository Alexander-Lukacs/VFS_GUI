package controller.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import restful.clients.RestClient;
import tools.AlertWindows;
import tools.Validation;
import tools.xmlTools.LastSessionStorage;

import static cache.DataCache.GC_IP_KEY;
import static cache.DataCache.GC_PORT_KEY;
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

    @FXML
    private Button gob_btn_connect;

    private MainController gob_mainController;


    private final controller.classes.ListView listView = new controller.classes.ListView();

    public void initialize() {
        listView.loadSettingsList(gob_lvOptions);
    }

    /**
     * Öffnet die View die ausgewählt wurde
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
            // TODO schauen ob es eine besser Methode gibt, als ein leerer catch block
        } catch (Exception ignore) {
        }
    }

    public void onClick() {
        RestClient lob_restClient;
        DataCache lob_dataCache = DataCache.getDataCache();

        String lva_ip = gob_tfServerIp.getText();
        String lva_port = gob_tfPort.getText();

        if (Validation.isIpValid(lva_ip)) {
            if (Validation.isPortValid(lva_port)) {

                lob_dataCache.replaceData(GC_IP_KEY, lva_ip);
                lob_dataCache.replaceData(GC_PORT_KEY, lva_port);
                LastSessionStorage.setIp(lva_ip);
                LastSessionStorage.setPort(lva_port);


                Stage stage = ((Stage) gob_btn_connect.getScene().getWindow());
                stage.close();
                Platform.runLater(() -> gob_mainController.logout());

                try {
                    lob_restClient = RestClientBuilder.buildRestClientWithAuth();
                    lob_restClient.unregisterClient();
                    Platform.exit();
                } catch (Exception ex) {
                    new AlertWindows().createWarningAlert("Error while unregister client from server");
                }

            } else {
                new AlertWindows().createWarningAlert(GC_WARNING_PORT);
            }
        } else {
            new AlertWindows().createWarningAlert(GC_WARNING_IP_PORT);
        }
    }

    public void initMainControllerData(MainController iob_mainController) {
        gob_mainController = iob_mainController;
    }
}

