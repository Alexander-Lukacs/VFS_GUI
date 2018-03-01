package controller;

import builder.RestClientBuilder;
import cache.DataCache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.classes.SharedDirectory;
import models.classes.User;
import rest.RestClient;
import tools.AlertWindows;
import tools.Validation;

import java.util.List;

import static controller.constants.SharedDirectoryConstants.*;


/**
 * Created by Mesut on 01.03.2018.
 * SharedDirectory Scene
 */
public class SharedDirectoryController {
    @FXML
    private TextField gob_tf_directory_name;
    @FXML
    private TextField gob_tf_email;
    @FXML
    private ListView<String> gob_list_member;
    @FXML
    private Button gob_btn_cancel;
    private SharedDirectory gob_sharedDirectory;


    private final ObservableList<String> member = FXCollections.observableArrayList();

    public void initData(SharedDirectory iob_sharedDirectory) {
        gob_sharedDirectory = iob_sharedDirectory;
        initScene();
    }

    private void initScene() {
        if (gob_sharedDirectory != null) {
            gob_tf_directory_name.setText(gob_sharedDirectory.getDirectoryName());

            for (User lob_user : gob_sharedDirectory.getMembers()) {
                member.add(lob_user.getEmail());
            }

            gob_list_member.setItems(member);
        }
    }

    public void onClickCancel() {
        Stage lob_stage = (Stage) gob_btn_cancel.getScene().getWindow();
        lob_stage.close();
    }

    public void onClickSave() {
        if (gob_sharedDirectory == null) {
            addNewSharedDirectory();
        } else {
            changeSharedDirectory();
        }
    }

    private void addNewSharedDirectory() {
        SharedDirectory lob_sharedDirectory = new SharedDirectory();

        try {
            if (gob_tf_directory_name.getText().trim().isEmpty()) {
                throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NAME_EMPTY);
            }

            lob_sharedDirectory.setDirectoryName(gob_tf_directory_name.getText());

        } catch (IllegalArgumentException ex) {
            new AlertWindows().createWarningAlert(ex.getMessage());
        }
    }

    private void changeSharedDirectory() {
        // TODO implement
    }

    public void onClickAddMember() {
        RestClient lob_restClient;
        List<User> lob_userList;
        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_email;
        boolean lva_userNotExists = true;
        boolean lva_valid;

        lob_restClient = RestClientBuilder.buildRestClientWithAuth();
        lob_userList = lob_restClient.getAllUser();

        lva_email = gob_tf_email.getText().trim();
        lva_valid = Validation.isEmailValid(lva_email);

        try {
            if (lva_email.isEmpty() || !lva_valid) {
                throw new IllegalArgumentException(GC_NO_CORRECT_EMAIL);
            }

            if (lva_email.equals(lob_dataCache.get(DataCache.GC_EMAIL_KEY))) {
                gob_tf_email.setText("");
                throw new IllegalArgumentException(GC_USER_CANT_ADD_HIMSELF);
            }

            for (User lob_user : lob_userList) {
                if (lob_user.getEmail().equals(gob_tf_email.getText())) {
                    if (!member.contains(lob_user.getEmail())) {
                        member.add(lob_user.getEmail());
                        gob_list_member.setItems(member);
                    } else {
                        gob_tf_email.setText("");
                        throw new IllegalArgumentException(GC_USER_IS_ALREADY_ADDED);
                    }

                    lva_userNotExists = false;
                }
            }

            if (lva_userNotExists) {
                throw new IllegalArgumentException(GC_NOT_VALID_USER);
            }

        } catch (IllegalArgumentException ex) {
            new AlertWindows().createWarningAlert(ex.getMessage());
        }
    }

    public void onClickRemoveMember() {
        String lva_email;
        if (gob_list_member.getSelectionModel().getSelectedItem() != null) {
            lva_email = gob_list_member.getSelectionModel().getSelectedItem();
            member.remove(lva_email);

            gob_list_member.setItems(member);
        }
    }
}
