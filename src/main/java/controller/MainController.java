package controller;

import cache.DataCache;
import fileTree.models.TreeImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.classes.TreeControl;
import tools.AlertWindows;
import tools.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static controller.constants.SettingsConstants.*;

public class MainController {

    @FXML
    private Button gob_btnSettings;

    @FXML
    private TreeView<String> gob_treeView; //TODO Könte lokal gemacht werden, nur wie?

    @FXML
    private VBox gob_vbox = new VBox();

    private DataCache userCache;


    /**
     * Beim Klicken des Buttons wird die View settings.fxml geöffnet
     */

    public void onClick(ActionEvent e) throws RuntimeException, IOException {

        if (((Button) e.getSource()).getText().equals(GC_SETTINGS)) {

            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("settings.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle(GC_SETTINGS);
            lob_stage.setResizable(false);
            lob_stage.setScene(lob_scene);
            lob_stage.show();
        } else if (((Button) e.getSource()).getText().equals(GC_LOGOUT)) {
            userCache.clearDataCache();
            Stage stage = ((Stage) gob_btnSettings.getScene().getWindow());
            stage.close();
            LoginController ob_x = new LoginController();
            ob_x.start(stage);
        }
    }

    public void initialize() throws IOException {
        userCache = DataCache.getDataCache();
        gob_treeView = new TreeView<>();
        TreeControl lob_treeControl = new TreeControl(gob_treeView, userCache.get(DataCache.GC_IP_KEY), userCache.get(DataCache.GC_PORT_KEY));
        gob_vbox.getChildren().add(gob_treeView);
    }

    public void start(Stage lob_stage) {

        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("mainScreen.fxml"));
        try {
            SplitPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            lob_stage.setTitle(GC_VFS);
            lob_stage.setScene(lob_scene);
            lob_stage.show();
        } catch (IOException e) {
            AlertWindows.createExceptionAlert(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
