package controller;

import builder.RestClientBuilder;
import cache.DataCache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.classes.SharedDirectory;
import models.classes.User;
import rest.RestClient;
import tools.AlertWindows;
import tools.Validation;

import java.util.ArrayList;
import java.util.List;

import static controller.constants.SharedDirectoryConstants.*;


/**
 * Created by Mesut on 01.03.2018.
 * SharedDirectory Scene
 */
public class SharedDirectoryController {
    private final ObservableList<String> gob_member = FXCollections.observableArrayList();
    @FXML
    private TextField gob_tf_directory_name;
    @FXML
    private TextField gob_tf_email;
    @FXML
    private ListView<String> gob_list_member;
    private SharedDirectory gob_sharedDirectory;
    private Stage gob_stage;

    public void initData(SharedDirectory iob_sharedDirectory, Stage iob_stage) {
        gob_sharedDirectory = iob_sharedDirectory;
        gob_stage = iob_stage;
        initScene();
    }

    private void initScene() {
        if (gob_sharedDirectory != null) {
            gob_tf_directory_name.setText(gob_sharedDirectory.getDirectoryName());

            for (User lob_user : gob_sharedDirectory.getMembers()) {
                gob_member.add(lob_user.getEmail());
            }

            gob_list_member.setItems(gob_member);
        }
    }

    public void onClickCancel() {
        closeWindow();
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
        User lob_owner = new User();
        User lob_member;
        List<User> lli_memberList = new ArrayList<>();
        RestClient lob_restClient;
        DataCache lob_dataCache = DataCache.getDataCache();

        try {
            if (gob_tf_directory_name.getText().trim().isEmpty()) {
                throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NAME_EMPTY);
            }

            lob_sharedDirectory.setDirectoryName(gob_tf_directory_name.getText());
            lob_owner.setEmail(lob_dataCache.get(DataCache.GC_EMAIL_KEY));
            lob_sharedDirectory.setOwner(lob_owner);

            for (String lob_email : gob_list_member.getItems()) {
                lob_member = new User();
                lob_member.setEmail(lob_email);
                lli_memberList.add(lob_member);
            }

            lob_sharedDirectory.setMembers(lli_memberList);

            lob_restClient = RestClientBuilder.buildRestClientWithAuth();
            lob_restClient.addNewSharedDirectory(lob_sharedDirectory);

            // TODO create Directory
        } catch (IllegalArgumentException ex) {
            new AlertWindows().createWarningAlert(ex.getMessage());
        }
    }

    private void changeSharedDirectory() {
        List<User> lli_oldMemberList;
        boolean lva_found;

        if (!gob_sharedDirectory.getDirectoryName().equals(gob_tf_directory_name.getText())) {
            //TODO Directory Name Mapper
        }

        lli_oldMemberList = gob_sharedDirectory.getMembers();

        // check if member was removed
        for (User lob_user : lli_oldMemberList) {
            if (!gob_member.contains(lob_user.getEmail())) {
                // Delete Member
            }
        }

        // check if member was added
        for (String lob_userMail : gob_member) {
            lva_found = false;
            for (User lob_user : lli_oldMemberList) {
                if (lob_user.getEmail().equals(lob_userMail)) {
                    lva_found = true;
                }
            }

            if (!lva_found) {
                // Add new Member
            }
        }
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
                    if (!gob_member.contains(lob_user.getEmail())) {
                        gob_member.add(lob_user.getEmail());
                        gob_list_member.setItems(gob_member);
                    } else {
                        throw new IllegalArgumentException(GC_USER_IS_ALREADY_ADDED);
                    }

                    gob_tf_email.setText("");
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
            gob_member.remove(lva_email);

            gob_list_member.setItems(gob_member);
        }
    }

    private void closeWindow() {
        gob_stage.close();
    }
}
