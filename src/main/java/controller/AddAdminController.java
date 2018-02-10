package controller;

import builder.RestClientBuilder;
import cache.DataCache;
import client.HttpMessage;
import client.RestClient;
import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.classes.UserImpl;
import models.interfaces.User;
import tools.AlertWindows;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.util.List;

import static cache.DataCache.*;
import static client.constants.HttpStatusCodes.GC_HTTP_BAD_REQUEST;
import static client.constants.HttpStatusCodes.GC_HTTP_CONFLICT;
import static client.constants.HttpStatusCodes.GC_HTTP_OK;
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

    @FXML
    private ListView<String> gob_lvUser;

    private final controller.ListView listView = new controller.ListView();

    private List<UserImpl> gob_userList;

    private DataCache gob_dataCache;

    public void initialize() {
        gob_dataCache = DataCache.getDataCache();
        String lva_email = gob_dataCache.get(GC_EMAIL_KEY);
        String lva_password = gob_dataCache.get(GC_PASSWORD_KEY);
        String lva_ip = gob_dataCache.get(GC_IP_KEY);
        String lva_port = gob_dataCache.get(GC_PORT_KEY);


        RestClient restClient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port, lva_email, lva_password);

        try {
            gob_userList = restClient.getAllUser();
        } catch (ProcessingException ex) {
            AlertWindows.createExceptionAlert(ex.getMessage(), ex);
        }


        listView.loadSettingsList(gob_lvOptions);
        listView.loadUserList(gob_lvUser, gob_userList);

    }

    public void loadView() throws IOException {
    try {
        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_PW)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_IP_PORT)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changeIpPort.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }catch (Exception e){

    }
    }

    public void onClick() {
        String lva_ip = gob_dataCache.get(GC_IP_KEY);
        String lva_port = gob_dataCache.get(GC_PORT_KEY);
        String lva_password = gob_dataCache.get(GC_PASSWORD_KEY);
        String lva_email = gob_dataCache.get(GC_EMAIL_KEY);
        HttpMessage lob_httpMessage;

        RestClient restClient = RestClientBuilder.buildRestClientWithAuth(lva_ip, lva_port, lva_email, lva_password);

        for (User lob_user : gob_userList) {
            if (lob_user.getEmail().equals(gob_lvUser.getSelectionModel().getSelectedItem())) {
                try {
                    lob_httpMessage = restClient.addNewAdmin(lob_user);
                    printHttpMessage(lob_httpMessage);
                    ((Stage) gob_rootPane.getScene().getWindow()).close();
                } catch (ProcessingException ex) {
                    AlertWindows.createExceptionAlert(ex.getMessage(), ex);
                }
            }
        }
    }

    private void printHttpMessage(HttpMessage iob_httpMessage) {
        switch (iob_httpMessage.getHttpStatus()) {
            case GC_HTTP_OK:
                AlertWindows.createInformationAlert(iob_httpMessage.getAddAdminStatus());
                break;

            case GC_HTTP_BAD_REQUEST:
                AlertWindows.createErrorAlert(iob_httpMessage.getAddAdminStatus());
                break;

            case GC_HTTP_CONFLICT:
                AlertWindows.createErrorAlert(iob_httpMessage.getAddAdminStatus());
                break;
        }
    }
}
