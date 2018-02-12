package fileTree.models;

import javafx.scene.control.TreeItem;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;
public class WatcherService extends Thread{

    private Collection<File> gco_files;
    private Map<WatchKey, Path> gob_keyMap;
    private WatchService gob_service;

    public WatcherService(Collection<File> iob_file) {
        this.gco_files = iob_file;
        this.gob_keyMap = new HashMap<>();
    }

    public void run() {
        //-----------------------------------Variables---------------------------------
        WatchKey lob_watchKey;
        Path lob_eventDir;
        WatchEvent.Kind<?> lob_kind;
        Path lob_eventPath;
        String lva_newFilePath;
        File lob_file;
        //-----------------------------------------------------------------------------
        try (WatchService service = FileSystems.getDefault().newWatchService()){
            gob_service = service;
            for (File lob_directoryToRegister : gco_files) {
                register(lob_directoryToRegister);
            }

            do {
                lob_watchKey = gob_service.take();
                lob_eventDir = gob_keyMap.get(lob_watchKey);

                for (WatchEvent<?> event : lob_watchKey.pollEvents()) {
                    lob_kind = event.kind();
                    lob_eventPath = (Path)event.context();

                    lva_newFilePath = lob_eventPath.getFileName().toString();
                    lva_newFilePath = lob_eventDir.toFile().getCanonicalPath() + "\\" + lva_newFilePath;
                    lob_file = new File(lva_newFilePath);

                    if (lob_kind == ENTRY_CREATE) {
                        registerDirectory(lob_file);
                    }

                    if (lob_kind == ENTRY_DELETE) {
                        System.out.println(lob_file.getCanonicalPath());
                        gob_keyMap.remove(lob_watchKey);
                    }
                }
//                boolean valid = lob_watchKey.reset();
//                if (!valid) {
//                    gob_keyMap.remove(lob_watchKey);
//                }
            } while(lob_watchKey.reset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerDirectory(File iob_file) {
        try {

            if(TreeSingleton.getInstance().getDuplicateFilePrevention().isFileCreated(iob_file.toPath())) {
                TreeSingleton.getInstance().getDuplicateFilePrevention().removeCreated(iob_file.toPath());
            } else {
                addToTree(iob_file);
            }

            if (iob_file.isDirectory()) {
                register(iob_file);
                for (File lob_file : iob_file.listFiles()) {
                    registerDirectory(lob_file);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void register(File iob_file) throws IOException{
        Path lob_path = iob_file.toPath();

        gob_keyMap.put(lob_path.register(gob_service,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE),
                lob_path
        );
        System.out.println("register: " + iob_file.getCanonicalPath());
    }

    private void addToTree(File iob_file) {
        try {
            TreeItem<String> lob_pointer = TreeSingleton.getInstance().getTreeView().getRoot();
            String[] lar_path = removeBasePathAndConvertToArray(iob_file.getCanonicalPath());
            int depth = 0;

            while (lar_path.length > depth) {
                for (TreeItem<String> lob_item : lob_pointer.getChildren()) {

                    if (lob_item.getValue().equals(lar_path[depth])) {
                        lob_pointer = lob_item;
                        break;
                    }
                }
                depth++;
            }
            TreeTool.getInstance().addTreeItem(lob_pointer, iob_file);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[] removeBasePathAndConvertToArray(String iva_filePath) throws IOException{
        String lva_basePath = TreeSingleton.getInstance().getTree().getRoot().getCanonicalPath();
        lva_basePath = lva_basePath.replaceAll("\\\\", "\\\\\\\\");
        iva_filePath = iva_filePath.replaceFirst(lva_basePath, "");
        iva_filePath = iva_filePath.replaceFirst("\\\\", "");
        return iva_filePath.split("\\\\");
    }
}
