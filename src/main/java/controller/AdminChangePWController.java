package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

import static constants.SettingsConstants.*;

public class AdminChangePWController {

    @FXML
    private ListView<String> gob_lvOptions;

    private ObservableList<String> gob_oblist;

    @FXML
    private Button gob_btnSave;

    @FXML
    private AnchorPane gob_rootPane;

    /**
     * Initiallisiert die ListView
     */
    public void initialize()
    {
        gob_oblist = FXCollections.observableArrayList();
        gob_oblist.add(CHANGE_PW);
        gob_oblist.add(LOGOUT);
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
        if(gob_lvOptions.getSelectionModel().getSelectedItem() == LOGOUT)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("loginScreen.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
        if(gob_lvOptions.getSelectionModel().getSelectedItem() == CHANGE_PW)
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("adminChangePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
        }
    }
}