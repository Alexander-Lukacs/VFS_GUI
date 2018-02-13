package fileTree.models;

import fileTree.interfaces.FileChangeListener;
import fileTree.interfaces.Tree;
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
    private static boolean gob_isRunning = false;

    /**
     * Register the WatchService on the root diretory. Scan at the same time all children of the root directory and
     * register them too
     * @param iob_root the root to watch
     * @param iob_listener call the fitting method if something happens in the registered directories
     * @throws IOException if the root is no directory or does not exist.
     */
    NewWatchService(Path iob_root, FileChangeListener iob_listener) throws IOException {
        //-------------------------------Variables--------------------------------------
        Collection<Path> lco_firstScan;
        //------------------------------------------------------------------------------
        gob_root = iob_root;
        gob_listender = iob_listener;
        if (!iob_root.toFile().exists() || !iob_root.toFile().isDirectory()) {
            throw new IOException("The Path to Watch is no Directory");
        }

        gob_registerdPaths = new HashMap<>();
        lco_firstScan = scan(gob_root.toFile(), new HashMap<>()).keySet();

        for (Path lob_scannedPath : lco_firstScan) {
            register(lob_scannedPath);
        }
    }

    /**
     * register a new path to the WatcherService
     * @param iob_path path to watch
     */
    private void register(Path iob_path) throws IOException{
        gob_isRunning = true;
        BasicFileAttributes lob_attr = Files.readAttributes(iob_path, BasicFileAttributes.class);
        gob_registerdPaths.put(iob_path, lob_attr.creationTime());
    }

    private void scanRootAndCompare() throws IOException {
        //--------------------------Variables-------------------------------
        HashMap<Path, FileTime> lco_tmp;
        HashMap<Path, FileTime> lco_scanned;
        ArrayList<File> lli_delete = new ArrayList<>();
        PreventFileDuplicates lob_duplicates = TreeSingleton.getInstance().getDuplicateFilePrevention();
        HashMap<File, File> lco_renamedOrMoved = new HashMap<>();
        //------------------------------------------------------------------

        //scan the complete fileTree that is watched
        lco_scanned = scan(gob_root.toFile(), new HashMap<>());

        //create a copy of the paths that are registered
        lco_tmp = new HashMap<>(gob_registerdPaths);

        //remove all entries that have not changed
        lco_tmp.keySet().removeAll(lco_scanned.keySet());
        lco_scanned.keySet().removeAll(gob_registerdPaths.keySet());

        boolean wasFileRenamedOrMoved = false;

        for (Map.Entry<Path, FileTime> lob_entry : lco_tmp.entrySet()) {
            for (Iterator<Map.Entry<Path, FileTime>> lob_scannedIterator = lco_scanned.entrySet().iterator(); lob_scannedIterator.hasNext();) {
                Map.Entry<Path, FileTime> lob_scannedEntry = lob_scannedIterator.next();
                if (lob_scannedEntry.getValue().toMillis() == lob_entry.getValue().toMillis()) {

                    if (lob_duplicates.isFileRenamedOrRemoved(lob_entry.getKey())) {
                        lob_duplicates.removeRenamedOrDeleted(lob_entry.getKey());
                    } else {
                        lco_renamedOrMoved.put(lob_entry.getKey().toFile(), lob_scannedEntry.getKey().toFile());
                    }
                    wasFileRenamedOrMoved = true;
                    gob_registerdPaths.remove(lob_entry.getKey());
                    register(lob_scannedEntry.getKey());
                    lob_scannedIterator.remove();
                }
            }

            if (!wasFileRenamedOrMoved) {
                gob_registerdPaths.remove(lob_entry.getKey());
                if (TreeSingleton.getInstance().getDuplicateFilePrevention().isFileDeted(lob_entry.getKey())) {
                    TreeSingleton.getInstance().getDuplicateFilePrevention().removeDeleted(lob_entry.getKey());
                    gob_registerdPaths.remove(lob_entry.getKey());
                } else {
                    lli_delete.add(lob_entry.getKey().toFile());
                }
            }
        }
        ArrayList<File> test = new ArrayList<>();
        lco_scanned.keySet().forEach(lob_key -> {
            if (TreeSingleton.getInstance().getDuplicateFilePrevention().isFileCreated(lob_key)) {
                TreeSingleton.getInstance().getDuplicateFilePrevention().removeCreated(lob_key);
                try {
                    register(lob_key);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                test.add(lob_key.toFile());
            }
        });

        ArrayList<File> test2 = new ArrayList<>(lco_renamedOrMoved.keySet());
        for (File file : test2) {
            System.out.println("RENAMED: " + file.toPath() + " TO " + lco_renamedOrMoved.get(file).toPath());
            gob_listender.renamedOrMoved(file.toPath(), lco_renamedOrMoved.get(file).toPath());

        }

        lli_delete.sort(PathFileComparator.PATH_REVERSE);
        for (File file : lli_delete) {
            System.out.println("DELETED: " + file.toPath());
            gob_listender.fileDeleted(file.toPath());
        }

        test.sort(PathFileComparator.PATH_COMPARATOR);
        fileAdded(test);
//        for (File file : test) {
//            System.out.println("ADDED: " + file.toPath());
//            register(file.toPath());
//            gob_listender.fileAdded(file.toPath());
//        }

        System.out.println("----------------------------------------------------------");
        for (Map.Entry<Path, FileTime> lob_entry : gob_registerdPaths.entrySet()) {
            System.out.println(lob_entry.getKey());
        }
        System.out.println("----------------------------------------------------------");
    }

    private void fileAdded(Collection<File> ico_files) {
        for (File lob_file : ico_files) {
            try {
                System.out.println("ADDED: " + lob_file.toPath());
                register(lob_file.toPath());
                gob_listender.fileAdded(lob_file.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
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
            while(gob_isRunning) {
                Thread.sleep(5000);
                scanRootAndCompare();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * start the scan routine in a new Thread
     */
    public void start() {
        Thread lob_runnerThread = new Thread(this, NewWatchService.class.getSimpleName());
        lob_runnerThread.start();
    }

    /**
     * stop the WatchService
     */
    public void stop() {
        gob_isRunning = false;
    }
}
