package fileTree.models;

import java.io.File;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class WatcherService extends Thread{

    private File gob_file;

    public WatcherService(File iob_file) {
        this.gob_file = iob_file;
    }

    public void run() {
        try (WatchService service = FileSystems.getDefault().newWatchService()){
            Map<WatchKey, Path> keyMap = new HashMap<>();
            Path path = gob_file.toPath();
            keyMap.put(path.register(service,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY),
                    path
            );

            WatchKey watchKey;
            do {
                watchKey = service.take();
                Path eventDir = keyMap.get(watchKey);

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path eventPath = (Path)event.context();
                    System.out.println(eventDir + ":" + kind + ":" + eventPath);
                }

            } while(watchKey.reset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
