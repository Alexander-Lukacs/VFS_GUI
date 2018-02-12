package fileTree.models;

import fileTree.interfaces.FileChangeListener;
import org.apache.commons.io.comparator.PathFileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class NewWatchService implements Runnable{
    private HashMap<Path, FileTime> gob_registerdPaths;
    private Path gob_root;
    private FileChangeListener gob_listender;

    NewWatchService(Path iob_root, FileChangeListener iob_listener) throws IOException{
        gob_root = iob_root;
        gob_listender = iob_listener;
        File lob_file = gob_root.toFile();
        if (!lob_file.exists() || !lob_file.isDirectory()) {
            throw new IOException("");
        }
        gob_registerdPaths = new HashMap<>();
        Collection<Path> lob_c = scan(gob_root.toFile(), new HashMap<>()).keySet();

        for (Path path : lob_c) {
            register(path);
        }
    }

    /**
     * register a new path to the WatcherService
     * @param iob_path path to watch
     */
    private void register(Path iob_path) throws IOException{
        BasicFileAttributes lob_attr = Files.readAttributes(iob_path, BasicFileAttributes.class);
        gob_registerdPaths.put(iob_path, lob_attr.creationTime());
    }

    public void start() {
        Thread runnerThread = new Thread(this, NewWatchService.class.getSimpleName());
        runnerThread.start();
    }

    private void scanRootAndCompare() throws IOException {
        File lob_file = gob_root.toFile();
        HashMap<Path, FileTime> lco_tmp = new HashMap<>(gob_registerdPaths);
        HashMap<Path, FileTime> lco_scanned = scan(gob_root.toFile(), new HashMap<>());
        ArrayList<File> lli_delete = new ArrayList<>();

        if (!lob_file.exists() || !lob_file.isDirectory()) {
            throw new IOException("Root is not a Directory");
        }

        lco_tmp.keySet().removeAll(lco_scanned.keySet());
        lco_scanned.keySet().removeAll(gob_registerdPaths.keySet());

        boolean g = false;

        for (Map.Entry<Path, FileTime> lob_entry : lco_tmp.entrySet()) {
            for (Iterator<Map.Entry<Path, FileTime>> lob_scannedIterator = lco_scanned.entrySet().iterator(); lob_scannedIterator.hasNext();) {
                Map.Entry<Path, FileTime> t = lob_scannedIterator.next();
                if (t.getValue().toMillis() == lob_entry.getValue().toMillis()) {
                    g = true;
                    System.out.println("RENAMED: " + lob_entry.getKey() + " TO " + t.getKey().getFileName());
                    gob_registerdPaths.remove(lob_entry.getKey());
                    register(t.getKey());
                    lob_scannedIterator.remove();
                }
            }

            if (!g) {
                System.out.println("Gelöscht: " + lob_entry.getKey());
                gob_registerdPaths.remove(lob_entry.getKey());
                if (TreeSingleton.getInstance().getDuplicateFilePrevention().isFileDeted(lob_entry.getKey())) {
                    TreeSingleton.getInstance().getDuplicateFilePrevention().removeDeleted(lob_entry.getKey());
                } else {
                    lli_delete.add(lob_entry.getKey().toFile());
                }
            }
        }

        ArrayList<File> test = new ArrayList<>();
        lco_scanned.keySet().forEach(lob_key -> {
            if (TreeSingleton.getInstance().getDuplicateFilePrevention().isFileCreated(lob_key)) {
                TreeSingleton.getInstance().getDuplicateFilePrevention().removeCreated(lob_key);
            } else {
                test.add(lob_key.toFile());
            }
        });


        lli_delete.sort(PathFileComparator.PATH_REVERSE);
        for (File file : lli_delete) {
            gob_listender.fileDeleted(file.toPath());
        }

        test.sort(PathFileComparator.PATH_COMPARATOR);
        for (File file : test) {
            register(file.toPath());
            gob_listender.fileAdded(file.toPath());
        }

        System.out.println("----------------------------------------------------------");
        for (Map.Entry<Path, FileTime> lob_entry : gob_registerdPaths.entrySet()) {
            System.out.println(lob_entry.getKey());
        }
        System.out.println("----------------------------------------------------------");
    }

    /**
     * scan all files including the children of the file
     * @param iob_file pointer to the current path
     * @param ico_files all found paths
     * @return collection of all found paths
     */
    private HashMap<Path, FileTime> scan(File iob_file, HashMap<Path, FileTime> ico_files) throws IOException{
        BasicFileAttributes attr = Files.readAttributes(iob_file.toPath(), BasicFileAttributes.class);
        ico_files.put(iob_file.toPath(), attr.creationTime());
        File[] lar_children = iob_file.listFiles();
        if (lar_children != null) {
            for (File lob_file : lar_children) {
                scan(lob_file, ico_files);
            }
        }
        return ico_files;
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
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(5000);
                scanRootAndCompare();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
