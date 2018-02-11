package controller;

import cache.DataCache;
import fileTree.models.TreeSingleton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.classes.TreeControl;
import tools.AlertWindows;

import java.io.IOException;

import static controller.constants.ApplicationConstants.GC_APPLICATION_ICON_PATH;
import static controller.constants.SettingsConstants.*;

public class MainController {

    @FXML
    private Button gob_btnSettings;

    @FXML
    private TreeView<String> gob_treeView; //TODO Könte lokal gemacht werden, nur wie?

    @FXML
    private VBox gob_vBox = new VBox();

    private DataCache gob_userCache;


    /**
     * Beim Klicken des Buttons wird die View settings.fxml geöffnet
     */

    public void onClick(ActionEvent e) throws RuntimeException, IOException {
        if (((Button) e.getSource()).getText().equals(GC_SETTINGS)) {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("views/settings.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle(GC_SETTINGS);
            lob_stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(GC_APPLICATION_ICON_PATH)));
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

    public void initialize() throws IOException {
        gob_userCache = DataCache.getDataCache();
        TreeControl lob_treeControl = new TreeControl(gob_userCache.get(DataCache.GC_IP_KEY), gob_userCache.get(DataCache.GC_PORT_KEY));
        gob_treeView = TreeSingleton.getInstance().getTreeView();
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
}
