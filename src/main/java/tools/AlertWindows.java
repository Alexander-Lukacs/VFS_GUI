package tools;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;

import static tools.constants.AlertConstants.*;

/**
 * Created by Mesut on 08.02.2018.
 */

public class AlertWindows {


    public static void createExceptionAlert(String iva_content, Exception iob_exception) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(GC_EXCEPTION_TITLE);
        alert.setHeaderText(GC_EXCEPTION_HEADER);
        alert.setContentText(iva_content);

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        iob_exception.printStackTrace(pw);
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

    public static void createWarningAlert(String iva_content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(GC_WARNING_TITLE);
        alert.setHeaderText(GC_WARNING_HEADER);
        alert.setContentText(iva_content);

        alert.showAndWait();
    }

    public static void createErrorAlert(String iva_content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(GC_ERROR_TITLE);
        alert.setHeaderText(GC_ERROR_HEADER);
        alert.setContentText(iva_content);

        alert.showAndWait();
    }

    public static void createInformationAlert(String iva_content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(GC_INFORMATION_TITLE);
        alert.setHeaderText(GC_INFORMATION_HEADER);
        alert.setContentText(iva_content);

        alert.showAndWait();

    }

}
