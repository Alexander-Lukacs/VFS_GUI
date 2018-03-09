package threads.models;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.File;

/**
 * Created by Mesut on 05.03.2018.
 */
public class DirectoryCounterThread extends Thread {
    private File gob_file;
    private Label gob_content_label;
    private int countDir = 0;
    private int countFiles = 0;

    public DirectoryCounterThread(File iob_file, Label iob_content_label) {
        gob_file = iob_file;
        gob_content_label = iob_content_label;
    }

    @Override
    public void run() {
        getCount(gob_file);
        Platform.runLater(() -> gob_content_label.setText("Files: " + countFiles + " Directories: " + countDir));
    }

    private void getCount(File iob_file) {
        File[] lob_files = iob_file.listFiles();

        if (lob_files != null) {
            for (File lob_file : lob_files) {

                if (lob_file.isDirectory()) {
                    countDir++;
                    getCount(lob_file);
                } else {
                    countFiles++;
                }
            }
        }
    }
}
