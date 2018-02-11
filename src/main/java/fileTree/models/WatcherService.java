package fileTree.models;

import java.io.File;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatcherService extends Thread{

    private Collection<File> gco_files;

    public WatcherService(Collection<File> iob_file) {
        this.gco_files = iob_file;
    }

    public void run() {
        try (WatchService service = FileSystems.getDefault().newWatchService()){
            Map<WatchKey, Path> keyMap = new HashMap<>();

            for (File lob_file : gco_files) {
                Path path = lob_file.toPath();
                keyMap.put(path.register(service,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY),
                        path
                );
            }

            WatchKey watchKey;

            do {
                watchKey = service.take();
                Path eventDir = keyMap.get(watchKey);

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path eventPath = (Path)event.context();

                    if (kind != ENTRY_MODIFY) {
                        System.out.println(eventDir + ":" + kind + ":" + eventPath);
                    }
                }

            } while(watchKey.reset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
