package threads.classes;

import javafx.application.Platform;
import javafx.scene.control.Label;
import threads.interfaces.ThreadControl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Mesut on 05.03.2018.
 */
public class DirectoryCounterThread implements Runnable, ThreadControl {
    private final File gob_file;
    private final Label gob_content_label;
    private int countDir = 0;
    private int countFiles = 0;
    private Thread gob_thread;

    public DirectoryCounterThread(File iob_file, Label iob_content_label) {
        gob_file = iob_file;
        gob_content_label = iob_content_label;
    }

    private void getCount(File iob_file){
        try {
            Files.walk(iob_file.toPath()).forEach( lob_path -> {
                    if (lob_path.toFile().isDirectory()) {
                        countDir++;
                    } else {
                        countFiles++;
                    }
                }
            );
        } catch (IOException ignore) {

        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        getCount(gob_file);
        Platform.runLater(() -> gob_content_label.setText("Files: " + countFiles + " Directories: " + countDir));
    }

    @Override
    public void start() {
        gob_thread = new Thread(this, DirectoryCounterThread.class.getSimpleName());
        gob_thread.setDaemon(true);
        gob_thread.start();
    }

    @Override
    public void stop() {
        gob_thread.interrupt();
    }

    @Override
    public void clear() {

    }
}
