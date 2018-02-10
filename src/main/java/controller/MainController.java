package controller;

import cache.DataCache;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.classes.TreeControl;
import tools.AlertWindows;

import java.io.IOException;

import static controller.constants.SettingsConstants.*;

public class MainController {

    @FXML
    private Button gob_btnSettings;

    @FXML
    private VBox gob_vBox = new VBox();

    private DataCache gob_userCache;


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
            gob_userCache.clearDataCache();
            Stage stage = ((Stage) gob_btnSettings.getScene().getWindow());
            stage.close();
            LoginController ob_x = new LoginController();
            ob_x.start(stage);
        }
    }

    public void initialize() {
        gob_userCache = DataCache.getDataCache();
        TreeView<String> gob_treeView = new TreeView<>();
        TreeControl lob_treeControl = new TreeControl(gob_treeView, gob_userCache.get(DataCache.GC_IP_KEY), gob_userCache.get(DataCache.GC_PORT_KEY));
//        TreeImpl x = new TreeImpl(Utils.getUserBasePath());
//        TreeItem<String> root = new TreeItem<>(x.getRoot().getCanonicalPath());
//        createTree(root);
        gob_vBox.getChildren().add(gob_treeView);
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

//    private void createTree(TreeItem<String> rootItem) throws IOException {
//
//        try (DirectoryStream<String> directoryStream = Files.newDirectoryStream(rootItem.getValue())) {
//
//            for (String path : directoryStream) {
//
//                TreeItem<String> newItem = new TreeItem<>(path);
//                newItem.setExpanded(true);
//
//                rootItem.getChildren().add(newItem);
//
//                if (Files.isDirectory(path)) {
//                    createTree(newItem);
//                }
//            }
//        }
//    }
}
