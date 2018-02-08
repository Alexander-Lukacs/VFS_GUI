package controller;

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
import tools.Utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static controller.constants.SettingsConstants.*;

public class MainController {

    @FXML
    private Button btnSettings;
    @FXML
    private Button btnLogout;

    @FXML
    private TreeView<Path> treeView;

    @FXML
    private VBox vbox = new VBox();


    /**
     * Beim Klicken des Buttons wird die View settings.fxml geöffnet
     */

   public void onClick(ActionEvent e) throws RuntimeException, IOException{

           if( ((Button)e.getSource()).getText().equals(SETTINGS) ) {

               FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("settings.fxml"));
               AnchorPane lob_pane = lob_loader.load();
               Scene lob_scene = new Scene(lob_pane);
               Stage lob_stage = new Stage();
               lob_stage.setTitle(SETTINGS);
               lob_stage.setResizable(false);
               lob_stage.setScene(lob_scene);
               lob_stage.show();
           }
           else if( ((Button)e.getSource()).getText().equals(LOGOUT) ){
               Stage stage = ((Stage)btnSettings.getScene().getWindow());
               stage.close();
               LoginController ob_x = new LoginController();
               ob_x.start(stage);
           }
    }

    public void initialize()throws  IOException
    {
        TreeImpl x = new TreeImpl(Utils.getUserBasePath());
        TreeItem<Path> root = new TreeItem<Path>(Paths.get(x.getRoot().getCanonicalPath()));
        createTree(root);
        treeView = new TreeView<>(root);
        vbox.getChildren().add(treeView);
    }

    public void start(Stage lob_stage) {

        FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("mainScreen.fxml"));
        try {
                SplitPane lob_pane = lob_loader.load();
                Scene lob_scene = new Scene(lob_pane);
                lob_stage.setTitle(VFS);
                lob_stage.setScene(lob_scene);
                lob_stage.show();
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
    }

    public static void createTree(TreeItem<Path> rootItem) throws IOException {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootItem.getValue())) {

            for (Path path : directoryStream) {

                TreeItem<Path> newItem = new TreeItem<Path>(path);
                newItem.setExpanded(true);

                rootItem.getChildren().add(newItem);

                if (Files.isDirectory(path)) {
                    createTree(newItem);
                }
            }
        }
    }

}
