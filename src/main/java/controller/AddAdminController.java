package controller;

import builder.RestClientBuilder;
import rest.RestClient;
import models.classes.RestResponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.classes.User;
import tools.Utils;

import java.util.List;

import static rest.constants.HttpStatusCodes.GC_HTTP_OK;
import static controller.constants.SettingsConstants.GC_CHANGE_IP_PORT;
import static controller.constants.SettingsConstants.GC_CHANGE_PW;

/**
 * Created by Mesut on 31.01.2018.
 */
public class AddAdminController {
    private final controller.ListView listView = new controller.ListView();
    @FXML
    private ListView<String> gob_lvOptions;
    @FXML
    private AnchorPane gob_rootPane;
    @FXML
    private ListView<String> gob_lvUser;
    private List<User> gob_userList;

    @FXML
    public void initialize() {
        RestClient lob_restClient;
        lob_restClient = RestClientBuilder.buildRestClientWithAuth();

        gob_userList = lob_restClient.getAllUser();

        if (gob_userList == null) {

        } else {
            listView.loadSettingsList(gob_lvOptions);
            listView.loadUserList(gob_lvUser, gob_userList);
        }
    }

    public void loadView() {
        try {
            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_PW)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/changePW.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
            if (gob_lvOptions.getSelectionModel().getSelectedItem().equals(GC_CHANGE_IP_PORT)) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/changeIpPort.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
            // TODO schauen ob es eine besser Methode gibt, als ein leerer catch block
        } catch (Exception ignore) {
        }
    }

    public void onClick() {
        RestResponse lob_restResponse;
        RestClient lob_restClient;

        lob_restClient = RestClientBuilder.buildRestClientWithAuth();

        for (User lob_user : gob_userList) {
            if (lob_user.getEmail().equals(gob_lvUser.getSelectionModel().getSelectedItem())) {

                lob_restResponse = lob_restClient.addNewAdmin(lob_user);

                if (lob_restResponse != null) {
                    Utils.printResponseMessage(lob_restResponse);

                    if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                        ((Stage) gob_rootPane.getScene().getWindow()).close();
                    }
                }
            }
        }
    }
}
