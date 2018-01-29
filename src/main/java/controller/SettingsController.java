package controller;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static constants.SettingsConstants.*;


public class SettingsController {

    private LoginController loginController = new LoginController();

    @FXML
    private ListView<String> gob_lvOptions;

    private ObservableList<String> gob_oblist;

    @FXML
    private AnchorPane gob_rootPane;

    /**
     * Initiallisiert die ListView
     */
    public void initialize()
    {
        if(loginController.getIsAdmin()){
        gob_oblist = FXCollections.observableArrayList();
        gob_oblist.add(CHANGE_PW);
        gob_oblist.add(LOGOUT);
        gob_oblist.add(ADMIN_ADD);
        gob_lvOptions.setItems(gob_oblist);
        gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }else {
        gob_oblist = FXCollections.observableArrayList();
        gob_oblist.add(CHANGE_PW);
        gob_oblist.add(LOGOUT);
        gob_lvOptions.setItems(gob_oblist);
        gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    }


    /**
     * Öffnet die View die ausgewählt wurde
     *
     * @param event
     * @throws IOException
     */

    public void loadView(MouseEvent event) throws IOException {
        if(loginController.getIsAdmin()) {
            if (gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_PW) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("adminChangePW.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
        }else{
            if (gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_PW) {
                FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("UserChangePW.fxml"));
                AnchorPane lob_pane = lob_loader.load();
                gob_rootPane.getChildren().setAll(lob_pane);
            }
        }

        if(gob_lvOptions.getSelectionModel().getSelectedItem() == LOGOUT)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("loginScreen.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle(VFS);
            lob_stage.setScene(lob_scene);
            lob_stage.show();
            closeScreens();
        }
    }

    private void closeScreens() {
        Stage aktuell = new Stage();
        aktuell.close();
    }


}
