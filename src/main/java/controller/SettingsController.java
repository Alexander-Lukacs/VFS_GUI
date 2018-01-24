package controller;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;

import java.awt.event.ActionEvent;
import java.io.IOException;


public class SettingsController {

    @FXML
    private ListView<String> gob_lvOptions;

    private ObservableList<String> gob_oblist;

    @FXML
    private AnchorPane gob_rootPane;

    public void initialize()
    {
        gob_oblist = FXCollections.observableArrayList();
        gob_oblist.add("ChangePW");
        gob_lvOptions.setItems(gob_oblist);
        gob_lvOptions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void loadChangePW(ActionEvent event) throws IOException {
        if(gob_lvOptions.getSelectionModel().getSelectedItem() == "ChangePW")
        {
            FXMLLoader lob_loader = new FXMLLoader(getClass().getClassLoader().getResource("ChangePW.fxml"));
            AnchorPane lob_pane = lob_loader.load();
            gob_rootPane.getChildren().setAll(lob_pane);
            /*Scene lob_scene = new Scene(lob_pane);
            Stage lob_stage = new Stage();
            lob_stage.setTitle("Settings");
            lob_stage.setResizable(false);
            lob_stage.setScene(lob_scene);
            lob_stage.show();*/
        }
    }
}
