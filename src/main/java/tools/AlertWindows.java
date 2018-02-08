package tools;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

import static controller.constants.AlertConstants.*;

/**
 * Created by Mesut on 08.02.2018.
 */

public class AlertWindows {


    public static void ExceptionAlert (String content, Exception exception){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(GC_EXCEPTION_TITLE);
        alert.setHeaderText(GC_EXCEPTION_HEADER);
        alert.setContentText(content);

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }
    public static void WarningAlert ( String content) {

        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(GC_WARNING_TITLE);
        alert.setHeaderText(GC_WARNING_HEADER);
        alert.setContentText(content);

        alert.showAndWait();
    }

    //TODO falls kein content dazu kommt, kann dies auch statisch gemacht werden
    public static void ErrorAlert (String content){
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(GC_ERROR_TITLE);
        alert.setHeaderText(GC_ERROR_HEADER);
        alert.setContentText(content);

        alert.showAndWait();
    }

}
