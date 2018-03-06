package controller;

import builder.RestClientBuilder;
import cache.DataCache;
import fileTree.models.TreeControl;
import fileTree.models.TreeSingleton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import restful.clients.RestClient;
import tools.AlertWindows;
import tools.FileInformation;
import tools.Utils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static controller.constants.SettingsConstants.*;

public class MainController {

    @FXML
    private Button gob_btnSettings;

    @FXML
    private VBox gob_vBox = new VBox();

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

    private DataCache gob_userCache;
    private TreeControl gob_treeControl;


    /**
     * Beim Klicken des Buttons wird die View settings.fxml geöffnet
     */

    public void onClick(ActionEvent e) throws RuntimeException, IOException {
        RestClient lob_restClient;
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
                lob_stage.show();

                break;
            case GC_LOGOUT:
                lob_restClient = RestClientBuilder.buildRestClientWithAuth();
                lob_restClient.unregisterClient();
                Stage stage = ((Stage) gob_btnSettings.getScene().getWindow());
                stage.close();
                gob_userCache.clearDataCache();
                LoginController ob_x = new LoginController();
                ob_x.start(stage);
                break;
            case GC_SHOW_IN_EXPLORER:
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();

                    if (gob_treeControl.getPathOfSelectedItem() != null) {
                        desktop.open(new File(gob_treeControl.getPathOfSelectedItem()));
                    } else {
                        desktop.open(new File(Utils.getUserBasePath() + "\\" +
                                lob_dataCache.get(DataCache.GC_IP_KEY) + "_" + lob_dataCache.get(DataCache.GC_PORT_KEY)));
                    }

                }
                break;
        }
    }

    public void initialize() {
        gob_userCache = DataCache.getDataCache();
        gob_treeControl = new TreeControl(gob_userCache.get(DataCache.GC_IP_KEY), gob_userCache.get(DataCache.GC_PORT_KEY), this);
        TreeView<String> gob_treeView = TreeSingleton.getInstance().getTreeView();
        gob_vBox.getChildren().add(gob_treeView);
    }

    public void start(Stage lob_stage) {
        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/mainScreen.fxml"));

        try {
            SplitPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            lob_stage.setTitle(GC_VFS);
            lob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
            lob_stage.setScene(lob_scene);
            lob_stage.show();
        } catch (IOException e) {
            new AlertWindows().createExceptionAlert(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void setTypeLabel(File iob_file) {
        Thread lob_thread;

        if (iob_file.isDirectory()) {
            gob_label_type.setText("Directory");
            gob_txt_label_content.setVisible(true);
            gob_label_content.setVisible(true);
            lob_thread = new FileInformation(iob_file, gob_label_content);
            lob_thread.setName("FileInformationThread");
            lob_thread.start();
        }else{
            gob_label_type.setText("File");
            gob_txt_label_content.setVisible(false);
            gob_label_content.setVisible(false);
        }

        gob_label_name.setText(iob_file.getName());
        gob_label_size.setText(String.valueOf(getFileSize(iob_file)));


    }

    //TODO in einen Thread auslagern
    private long getFileSize(File iob_file) {
        if (iob_file.isDirectory()) {
            return FileUtils.sizeOfDirectory(iob_file);
        }
        return iob_file.length();
    }
}
