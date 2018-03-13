package controller.classes;

import builder.RestClientBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.classes.RestResponse;
import models.classes.User;
import restful.clients.UserRestClient;
import tools.Utils;

import java.util.List;

import static controller.constants.SettingsConstants.GC_CHANGE_IP_PORT;
import static controller.constants.SettingsConstants.GC_CHANGE_PW;
import static restful.constants.HttpStatusCodes.GC_HTTP_OK;

/**
 * Created by Mesut on 10.02.2018.
 * This Controller applies Admins to add other Addmins
 */
public class AddAdminController {
    private final controller.classes.ListView listView = new controller.classes.ListView();
    @FXML
    private ListView<String> gob_lvOptions;
    @FXML
    private AnchorPane gob_rootPane;
    @FXML
    private ListView<String> gob_lvUser;
    private List<User> gob_userList;

    @FXML
    public void initialize() {
        UserRestClient lob_restClient;
        lob_restClient = RestClientBuilder.buildUserClientWithAuth();

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
        UserRestClient lob_restClient;

        lob_restClient = RestClientBuilder.buildUserClientWithAuth();

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
