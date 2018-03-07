package controller;

import builder.RestClientBuilder;
import cache.DataCache;
import cache.SharedDirectoryCache;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.classes.RestResponse;
import models.classes.SharedDirectory;
import models.classes.User;
import restful.clients.SharedDirectoryRestClient;
import restful.clients.UserRestClient;
import tools.AlertWindows;
import tools.Utils;
import tools.Validation;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static controller.constants.SharedDirectoryConstants.*;
import static restful.constants.HttpStatusCodes.GC_HTTP_OK;

/**
 * Created by Mesut on 01.03.2018.
 * SharedDirectory Scene
 */
public class SharedDirectoryController {
    private final ObservableList<String> gli_memberList = FXCollections.observableArrayList();
    @FXML
    private TextField gob_tf_directory_name;
    @FXML
    private TextField gob_tf_email;
    @FXML
    private ListView<String> gob_memberListView;
    private SharedDirectory gob_sharedDirectory;
    private Stage gob_stage;

    private List<User> gli_userList;
    private HashMap<String, User> gob_userMap;

    /**
     * Initialise the sharedDirectory currently selected in the treeView
     *
     * @param iob_sharedDirectory currently selected sharedDirectory
     * @param iob_stage           the current stage
     */
    public void initData(SharedDirectory iob_sharedDirectory, Stage iob_stage) {
        UserRestClient lob_restClient;

        gob_sharedDirectory = iob_sharedDirectory;
        gob_stage = iob_stage;

        // Get all users and cache them
        lob_restClient = RestClientBuilder.buildUserClientWithAuth();
        gli_userList = lob_restClient.getAllUser();

        gob_userMap = new HashMap<>();
        for (User lob_tmpUser : gli_userList) {
            gob_userMap.put(lob_tmpUser.getEmail(), lob_tmpUser);
        }

        initScene();
    }

    /**
     * Initialise the scene
     */
    private void initScene() {
        if (gob_sharedDirectory != null) {
            gob_tf_directory_name.setText(gob_sharedDirectory.getDirectoryName());

            for (User lob_user : gob_sharedDirectory.getMembers()) {
                gli_memberList.add(lob_user.getEmail());
            }

            gob_memberListView.setItems(gli_memberList);
        }
    }

    /**
     * Cancel button listener
     */
    public void onClickCancel() {
        closeWindow();
    }

    /**
     * Save button listener
     */
    public void onClickSave() {
        if (gob_sharedDirectory == null) {
            addNewSharedDirectory();
        } else {
            changeSharedDirectory();
        }
    }

    /**
     * Add member button listener
     */
    public void onClickAddMember() {
        // Declaration block -----------------------------------------------------------------------------------------------

        DataCache lob_dataCache = DataCache.getDataCache();
        String lva_email;
        boolean lva_userNotExists = true;
        boolean lva_valid;

        // -----------------------------------------------------------------------------------------------------------------

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

            for (User lob_user : gli_userList) {
                if (lob_user.getEmail().equals(gob_tf_email.getText())) {
                    if (!gli_memberList.contains(lob_user.getEmail())) {
                        gli_memberList.add(lob_user.getEmail());
                        gob_memberListView.setItems(gli_memberList);
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

    /**
     * Remove member button listener
     */
    public void onClickRemoveMember() {
        String lva_email;
        if (gob_memberListView.getSelectionModel().getSelectedItem() != null) {
            lva_email = gob_memberListView.getSelectionModel().getSelectedItem();
            gli_memberList.remove(lva_email);

            gob_memberListView.setItems(gli_memberList);
        }
    }

    /**
     * Adds a new shared directory to the explorer and to the server
     */
    private void addNewSharedDirectory() {
        // Declaration block -----------------------------------------------------------------------------------------------

        SharedDirectory lob_sharedDirectory = new SharedDirectory();
        User lob_owner = new User();
        User lob_member;
        List<User> lli_memberList = new ArrayList<>();
        SharedDirectoryRestClient lob_restClient;
        RestResponse lob_restResponse;
        DataCache lob_dataCache = DataCache.getDataCache();

        // -----------------------------------------------------------------------------------------------------------------

        try {
            if (gob_tf_directory_name.getText().trim().isEmpty()) {
                throw new IllegalArgumentException(GC_SHARED_DIRECTORY_NAME_EMPTY);
            }

            lob_sharedDirectory.setDirectoryName(gob_tf_directory_name.getText());
            lob_owner.setEmail(lob_dataCache.get(DataCache.GC_EMAIL_KEY));
            lob_sharedDirectory.setOwner(lob_owner);

            for (String lob_email : gob_memberListView.getItems()) {
                lob_member = new User();
                lob_member.setEmail(lob_email);
                lli_memberList.add(lob_member);
            }

            lob_sharedDirectory.setMembers(lli_memberList);

            // Adds the shared directory to the server
            lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();
            lob_restResponse = lob_restClient.addNewSharedDirectory(lob_sharedDirectory);

            if (lob_restResponse.getHttpStatus() != GC_HTTP_OK) {
                Utils.printResponseMessage(lob_restResponse);
            } else {
                new AlertWindows().createInformationAlert("Shared directory successful created!");

                // If the shared directory was successfully to the server, add the shared directory
                // to the tree view and to the explorer
                lob_sharedDirectory.setId(Integer.parseInt(lob_restResponse.getResponseMessage()));
                createSharedDirectory(lob_sharedDirectory);
            }

        } catch (IllegalArgumentException ex) {
            new AlertWindows().createWarningAlert(ex.getMessage());
        }
    }

    /**
     * Create a shared directory in the explorer and tree view
     *
     * @param iob_sharedDirectory the shared directory
     */
    private void createSharedDirectory(SharedDirectory iob_sharedDirectory) {
        // Declaration block -----------------------------------------------------------------------------------------------

        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        File lob_file;
        int lva_counter = 1;
        String lva_filePath;

        // -----------------------------------------------------------------------------------------------------------------

        lva_filePath = buildPathToSharedDirectory(iob_sharedDirectory);

        lob_file = new File(lva_filePath.replace("$", ""));

        // if the directory already exists extend the dir name with (x).
        // x = autoincrement integer
        if (lob_file.exists()) {
            do {
                lob_file = new File(lva_filePath.replace("$", "(" + lva_counter + ")"));
                lva_counter++;
            } while (lob_file.exists());
        }

        lob_sharedDirectoryCache.put(iob_sharedDirectory.getId(), iob_sharedDirectory);
        DirectoryNameMapper.addNewSharedDirectory(iob_sharedDirectory.getId(), lob_file.getName());

        if (!lob_file.mkdir()) {
            new AlertWindows().createWarningAlert(GC_COULD_NOT_CREATE_DIR);
        }
    }

    /**
     * Change the shared directory
     */
    private void changeSharedDirectory() {
        // Declaration block -----------------------------------------------------------------------------------------------
        List<User> lli_oldMemberList;
        // -----------------------------------------------------------------------------------------------------------------

        // Rename shared directory locally in shared directory name mapper
        if (!gob_sharedDirectory.getDirectoryName().equals(gob_tf_directory_name.getText())) {
            DirectoryNameMapper.setNameOfSharedDirectory(gob_sharedDirectory.getId(), gob_tf_directory_name.getText());
        }

        lli_oldMemberList = gob_sharedDirectory.getMembers();

        checkIfMemberWasRemovedFromSharedDirectory(lli_oldMemberList);
        checkIfMemberWasAddedToSharedDirectory(lli_oldMemberList);

        closeWindow();
    }

    private void checkIfMemberWasAddedToSharedDirectory(List<User> ili_oldMemberList) {
        // Declaration block -----------------------------------------------------------------------------------------------
        boolean lva_found;
        User lob_user;
        SharedDirectoryRestClient lob_restClient;
        RestResponse lob_restResponse;
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        // -----------------------------------------------------------------------------------------------------------------

        lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();

        // check if member was added
        for (String lob_userMail : gli_memberList) {
            lva_found = false;

            for (User lob_tmpUser : ili_oldMemberList) {
                if (lob_tmpUser.getEmail().equals(lob_userMail)) {
                    lva_found = true;
                }
            }

            if (!lva_found) {
                lob_user = gob_userMap.get(lob_userMail);

                if (lob_user != null) {
                    lob_restResponse = lob_restClient.addNewMemberToSharedDirectory(gob_sharedDirectory, lob_user);
                    Utils.printResponseMessage(lob_restResponse);

                    if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                        gob_sharedDirectory.getMembers().add(lob_user);
                        lob_sharedDirectoryCache.replaceData(gob_sharedDirectory.getId(), gob_sharedDirectory);

                        // TODO löschen
                        System.out.println(lob_sharedDirectoryCache.get(gob_sharedDirectory.getId()));
                    }
                }
            }
        }
    }

    private void checkIfMemberWasRemovedFromSharedDirectory(List<User> ili_oldMemberList) {
    // Declaration block -----------------------------------------------------------------------------------------------

        SharedDirectoryRestClient lob_restClient;
        RestResponse lob_restResponse;
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        String lva_email;
        User lob_user;

    // -----------------------------------------------------------------------------------------------------------------

        lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();

        // check if member was removed
        for (int i = 0 ; i < ili_oldMemberList.size() ; i++) {
            lob_user = ili_oldMemberList.get(i);
            lva_email = lob_user.getEmail();

            if (!gli_memberList.contains(lva_email)) {

                lob_restResponse = lob_restClient.removeMemberFromSharedDirectory(gob_sharedDirectory, lob_user);
                Utils.printResponseMessage(lob_restResponse);

                if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                    gob_sharedDirectory.getMembers().remove(lob_user);
                    lob_sharedDirectoryCache.replaceData(gob_sharedDirectory.getId(), gob_sharedDirectory);

                    // TODO löschen
                    System.out.println(Arrays.toString(ili_oldMemberList.toArray()));
                    System.out.println(lob_sharedDirectoryCache.get(gob_sharedDirectory.getId()));
                }
            }
        }
    }

    /**
     * Close the current window
     */
    private void closeWindow() {
        gob_stage.close();
    }

    /**
     * Builds the absolute path to the shared directory
     *
     * @param iob_sharedDirectory the shared directory
     * @return the absolute path
     */
    private String buildPathToSharedDirectory(SharedDirectory iob_sharedDirectory) {
        DataCache lob_dataCache = DataCache.getDataCache();

        return Utils.getUserBasePath() + "\\" + lob_dataCache.get(DataCache.GC_IP_KEY) + "_" +
                lob_dataCache.get(DataCache.GC_PORT_KEY) + "\\" + lob_dataCache.get(DataCache.GC_EMAIL_KEY) +
                "\\" + "Shared" + "\\" + iob_sharedDirectory.getDirectoryName() + "$";
    }
}