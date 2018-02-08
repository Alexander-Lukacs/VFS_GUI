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
import tools.AlertWindows;
import tools.Utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static controller.constants.SettingsConstants.*;

public class MainController {

    @FXML
    private Button gob_btnSettings;
    @FXML
    private Button gob_btnLogout;

    @FXML
    private TreeView<Path> gob_treeView; //TODO Köönte lokal gemacht werden, nur wie?

    @FXML
    private VBox gob_vbox = new VBox();

    private DataCache userCache;



    /**
     * Beim Klicken des Buttons wird die View settings.fxml geöffnet
     */

   public void onClick(ActionEvent e) throws RuntimeException, IOException{

           if( ((Button)e.getSource()).getText().equals(GC_SETTINGS) ) {

               FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("settings.fxml"));
               AnchorPane lob_pane = lob_loader.load();
               Scene lob_scene = new Scene(lob_pane);
               Stage lob_stage = new Stage();
               lob_stage.setTitle(GC_SETTINGS);
               lob_stage.setResizable(false);
               lob_stage.setScene(lob_scene);
               lob_stage.show();
           }
           else if( ((Button)e.getSource()).getText().equals(GC_LOGOUT) ){
               userCache.clearDataCache();
               Stage stage = ((Stage) gob_btnSettings.getScene().getWindow());
               stage.close();
               LoginController ob_x = new LoginController();
               ob_x.start(stage);
           }
    }

    public void initialize()throws  IOException
    {
        userCache = DataCache.getDataCache();
        TreeImpl x = new TreeImpl(Utils.getUserBasePath());
        TreeItem<Path> root = new TreeItem<>(Paths.get(x.getRoot().getCanonicalPath()));
        createTree(root);
        gob_treeView = new TreeView<>(root);
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
            }
            catch (IOException e){
                AlertWindows.ExceptionAlert(e.getMessage(), e);
                throw new RuntimeException(e);
            }
    }

    private void createTree(TreeItem<Path> rootItem) throws IOException {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootItem.getValue())) {

            for (Path path : directoryStream) {

                TreeItem<Path> newItem = new TreeItem<>(path);
                newItem.setExpanded(true);

                rootItem.getChildren().add(newItem);

                if (Files.isDirectory(path)) {
                    createTree(newItem);
                }
            }
        }
    }

}
