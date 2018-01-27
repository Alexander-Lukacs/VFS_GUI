package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import static constants.SettingsConstants.*;

public class SettingsAdminController {


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
        gob_oblist = FXCollections.observableArrayList();
        gob_oblist.add(CHANGE_PW);
        gob_oblist.add(ADMIN_LOGOUT);
        gob_oblist.add(ADMIN_ADD);
        gob_lvOptions.setItems(gob_oblist);
        gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }


    /**
     * Öffnet die View die ausgewählt wurde
     *
     * @param event
     * @throws IOException
     */
    public void loadView(MouseEvent event) throws IOException {
        if(gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_PW)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("changePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if(gob_lvOptions.getSelectionModel().getSelectedItem() == ADMIN_LOGOUT)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("settings.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }
}
