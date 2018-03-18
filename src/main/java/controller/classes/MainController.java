package controller.classes;

import builder.RestClientBuilder;
import cache.DataCache;
import cache.SharedDirectoryCache;
import fileTree.classes.TreeSingleton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.classes.TreeControl;
import org.apache.commons.io.FileUtils;
import restful.clients.RestClient;
import threads.classes.DirectoryCounterThread;
import threads.classes.ThreadManager;
import tools.AlertWindows;
import tools.Utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static controller.constants.SettingsConstants.*;

/**
 * Created by Mesut on 01.03.2018.
 * MainController+
 * the Controller of the Main Scene
 * loads Tree View
 */
public class MainController {

    @FXML
    private Button gob_btnSettings;

    @FXML
    private VBox gob_vBox;

    @FXML
    private Label gob_label_type;

    @FXML
    private Label gob_label_name;

    @FXML
    private Label gob_label_size;

    @FXML
    private Label gob_label_content;

    @FXML
    private Label gob_txt_label_content;


//    private DataCache gob_userCache;
//    private TreeControl gob_treeControl;
    private TreeControl gob_treeControlVersionTwo;
    private MainController gob_mainController;

    private void initMainControllerData(MainController iob_mainController) {
        gob_mainController = iob_mainController;
    }

    /**
     * OnClick Method
     *
     * @param e event
     * @throws RuntimeException RuntimeException
     * @throws IOException      IOException
     */
    public void onClick(ActionEvent e) throws RuntimeException, IOException {
        DataCache lob_dataCache = DataCache.getDataCache();

        switch (((Button) e.getSource()).getText()) {
            case GC_SETTINGS:
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/settings.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                Scene lob_scene = new Scene(lob_pane);
                Stage lob_stage = new Stage();
                lob_stage.setTitle(GC_SETTINGS);
                lob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
                lob_stage.setResizable(false);
                lob_stage.setScene(lob_scene);
                SettingsController lob_settingsController = lob_loader.getController();
                lob_settingsController.initMainControllerData(gob_mainController);
                lob_stage.show();

                break;
            case GC_LOGOUT:
                logout();
                break;
            case GC_SHOW_IN_EXPLORER:
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();

//                    if (gob_treeControlV.getPathOfSelectedItem() != null) {
//                        desktop.open(new File(gob_treeControl.getPathOfSelectedItem()));
//                    } else {
//                        desktop.open(new File(Utils.getUserBasePath() + "\\" +
//                                lob_dataCache.get(DataCache.GC_IP_KEY) + "_" + lob_dataCache.get(DataCache.GC_PORT_KEY)));
//                    }

                    if (gob_treeControlVersionTwo.getPathOfSelectedItem() != null) {
                        desktop.open(new File(gob_treeControlVersionTwo.getPathOfSelectedItem()));
                    } else {
                        desktop.open(new File(Utils.getUserBasePath() + "\\" +
                                lob_dataCache.get(DataCache.GC_IP_KEY) + "_" + lob_dataCache.get(DataCache.GC_PORT_KEY)));
                    }
                }

                break;
        }
    }

    /**
     * close connection to server,
     * clear cache and close Main Scene.
     * starts Login Scene
     */

    public void logout() {
        SharedDirectoryCache lob_sharedDirectoryCache = SharedDirectoryCache.getInstance();
        RestClient lob_restClient;
        lob_restClient = RestClientBuilder.buildRestClientWithAuth();
        lob_restClient.unregisterClient();
        Stage stage = ((Stage) gob_btnSettings.getScene().getWindow());
        stage.close();

        lob_sharedDirectoryCache.clearDataCache();
        LoginController ob_x = new LoginController();
        try {
            ThreadManager.stopAndClear();
            ob_x.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
//        gob_userCache = DataCache.getDataCache();
        gob_treeControlVersionTwo = new TreeControl(this);
        TreeView<String> gob_treeView = TreeSingleton.getInstance().getTreeView();
        gob_vBox.getChildren().add(gob_treeView);
    }

    public void start(Stage lob_stage) {
        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/mainScreen.fxml"));

        try {
            SplitPane lob_pane = lob_loader.load();
            lob_pane.setDividerPosition(0, 0.55);
            Scene lob_scene = new Scene(lob_pane);
            lob_stage.setTitle(GC_VFS);
            lob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
            lob_stage.setScene(lob_scene);
            gob_mainController = lob_loader.getController();
            gob_mainController.initMainControllerData(gob_mainController);
            lob_stage.show();
        } catch (IOException e) {
            new AlertWindows().createExceptionAlert(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void setTypeLabel(File iob_file) {
        Thread lob_thread;
        long lva_fileSize_kb;

        if (iob_file.isDirectory()) {
            gob_label_type.setText("Directory");
            gob_txt_label_content.setVisible(true);
            gob_label_content.setVisible(true);
            lob_thread = new DirectoryCounterThread(iob_file, gob_label_content);
            lob_thread.setName("FileInformationThread");
            lob_thread.start();
        } else {
            gob_label_type.setText("File");
            gob_txt_label_content.setVisible(false);
            gob_label_content.setVisible(false);
        }

        gob_label_name.setText(iob_file.getName());

        // Bytes -> kBytes
        lva_fileSize_kb = getFileSize(iob_file) / 1024;

        gob_label_size.setText(String.valueOf(lva_fileSize_kb + " KB"));
    }

    //TODO evtl. in einen Thread auslagern
    private long getFileSize(File iob_file) {
        if (iob_file.isDirectory()) {
            return FileUtils.sizeOfDirectory(iob_file);
        }
        return iob_file.length();
    }

}
