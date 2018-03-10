package controller.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import cache.SharedDirectoryCache;
import fileTree.classes.TreeSingleton;
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
import threads.constants.FileManagerConstants;
import threads.classes.ThreadManager;
import tools.AlertWindows;
import tools.Utils;
import tools.Validation;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private File gob_sharedDirectoryFile;

    /**
     * Initialise the sharedDirectory currently selected in the treeView
     *
     * @param iob_sharedDirectory currently selected sharedDirectory
     * @param iob_stage           the current stage
     */
    public void initData(SharedDirectory iob_sharedDirectory, Stage iob_stage, File iobSharedDirectoryFile) {
        UserRestClient lob_restClient;

        gob_sharedDirectory = iob_sharedDirectory;
        gob_sharedDirectoryFile = iobSharedDirectoryFile;
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

    public void onClickDelete() {
        DataCache lob_dataCache = DataCache.getDataCache();
        SharedDirectoryCache lob_sharedDirCache = SharedDirectoryCache.getInstance();
        User lob_user;
        SharedDirectoryRestClient lob_restClient;
        RestResponse lob_restResponse;

        lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();

        lob_user = gob_userMap.get(lob_dataCache.get(DataCache.GC_EMAIL_KEY));

        if (gob_sharedDirectory.getOwner().getEmail().equals(lob_user.getEmail())) {
            lob_restResponse = lob_restClient.deleteSharedDirectory(gob_sharedDirectory);
        } else {
            lob_restResponse = lob_restClient.removeMemberFromSharedDirectory(gob_sharedDirectory, lob_user);
        }

        Utils.printResponseMessage(lob_restResponse);

        if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
            ThreadManager.getFileManagerThread().start();
            TreeSingleton.getInstance().getDuplicateOperationsPrevention().putDeleted(gob_sharedDirectoryFile.toPath());
            ThreadManager.addCommandToFileManager(gob_sharedDirectoryFile, FileManagerConstants.GC_DELETE,
                    true, gob_sharedDirectory.getId());

        }

        closeWindow();
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
                new AlertWindows().createInformationAlert(GC_SHARED_DIR_CREATED);

                // If the shared directory was successfully to the server, add the shared directory
                // to the tree view and to the explorer
                lob_sharedDirectory.setId(Integer.parseInt(lob_restResponse.getResponseMessage()));
                Utils.createSharedDirectory(lob_sharedDirectory);
                closeWindow();
            }

        } catch (IllegalArgumentException ex) {
            new AlertWindows().createWarningAlert(ex.getMessage());
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

    /**
     * Checks if member was added
     *
     * @param ili_oldMemberList old member list
     */
    private void checkIfMemberWasAddedToSharedDirectory(List<User> ili_oldMemberList) {
        // Declaration block -----------------------------------------------------------------------------------------------
        boolean lva_found;
        User lob_user;
        SharedDirectoryRestClient lob_restClient;
        RestResponse lob_restResponse;
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        // -----------------------------------------------------------------------------------------------------------------

        lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();

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
                    }
                }
            }
        }
    }

    /**
     * Checks if a member was removed
     *
     * @param ili_oldMemberList old member list
     */
    private void checkIfMemberWasRemovedFromSharedDirectory(List<User> ili_oldMemberList) {
        // Declaration block -----------------------------------------------------------------------------------------------

        SharedDirectoryRestClient lob_restClient;
        RestResponse lob_restResponse;
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        String lva_email;
        User lob_user;

        // -----------------------------------------------------------------------------------------------------------------

        lob_restClient = RestClientBuilder.buildSharedDirectoryClientWithAuth();

        for (Iterator<User> lob_iterator = ili_oldMemberList.iterator(); lob_iterator.hasNext();) {
            lob_user = lob_iterator.next();
            lva_email = lob_user.getEmail();

            if (!gli_memberList.contains(lva_email)) {

                lob_restResponse = lob_restClient.removeMemberFromSharedDirectory(gob_sharedDirectory, lob_user);
                Utils.printResponseMessage(lob_restResponse);

                if (lob_restResponse.getHttpStatus() == GC_HTTP_OK) {
                    lob_iterator.remove();
                    lob_sharedDirectoryCache.replaceData(gob_sharedDirectory.getId(), gob_sharedDirectory);
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
}