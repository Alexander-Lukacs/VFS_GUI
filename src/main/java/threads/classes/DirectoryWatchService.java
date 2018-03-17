package threads.classes;

import models.classes.PreventDuplicateOperation;
import models.interfaces.FileChangeListener;
import org.apache.commons.io.comparator.PathFileComparator;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class DirectoryWatchService implements Runnable {
    private HashMap<File, BasicFileAttributes> gob_registeredPaths;
    private File gob_root;
    private FileChangeListener gob_listener;
    private static Thread gob_thread;
    private static HashSet<Long> gob_creationTimes;

    /**
     * Register the WatchService on the root directory. Scan at the same time all children of the root directory and
     * register them too
     * @param iob_root the root to watch
     * @param iob_listener call the fitting method if something happens in the registered directories
     * @throws IOException if the root is no directory or does not exist.
     */
    DirectoryWatchService(File iob_root, FileChangeListener iob_listener) throws IOException {
        gob_root = iob_root;
        gob_listener = iob_listener;
        if (!iob_root.exists() || !iob_root.isDirectory()) {
            throw new IOException("The Path to Watch is no Directory");
        }

        gob_registeredPaths = new HashMap<>();
        gob_creationTimes = new HashSet<>();
    }

    private void init() throws IOException{
        System.out.println("Start Registration");
        for (File lob_path : scan(gob_root, new HashMap<>()).keySet()) {
            register(lob_path);
        }
        System.out.println("Finished Registration");
    }

    /**
     * We chick if the creation time of the file was already scanned, if that is true we increase the creation time by
     * one. This process is repeated until we have a creation time that was not scanned yet. This ensures that the file
     * has a unique creation time. Later the files can be identified by the creation time. After the unique creation
     * time is saved we register the file to the service
     * @param iob_file path to watch
     */
    private void register(File iob_file) throws IOException{
        BasicFileAttributeView lob_attrView = Files.getFileAttributeView(iob_file.toPath(), BasicFileAttributeView.class);
        BasicFileAttributes lob_attr = lob_attrView.readAttributes();
        FileTime lob_creationTime;
        FileTime lob_lastAccessTime = lob_attr.lastAccessTime();
        FileTime lob_lastModifiedTime = lob_attr.lastModifiedTime();
        long lva_creationTime = lob_attr.creationTime().toMillis();
        long lva_tmp = lva_creationTime;

        while(gob_creationTimes.contains(lva_creationTime)) {
            lva_creationTime++;
        }
        gob_creationTimes.add(lva_creationTime);

        if (lva_tmp != lva_creationTime) {
            lob_creationTime = FileTime.fromMillis(lva_creationTime);
            lob_attrView.setTimes(lob_lastModifiedTime, lob_lastAccessTime, lob_creationTime);
        }
        gob_registeredPaths.put(iob_file, lob_attr);
    }

    private void scanAndCompare() throws IOException{
        HashMap<File, BasicFileAttributes> lco_scannedFiles;
        TreeMap<File, BasicFileAttributes> lco_tmp;
        Map.Entry<File, BasicFileAttributes> lob_scannedEntry;
        Map.Entry<File, BasicFileAttributes> lob_registeredEntry;
        List<File> lli_added;
        List<File> lli_deleted;
        List<File> lli_updated = new ArrayList<>();
        Map<File, File> lob_movedMap = new TreeMap<>();
        Map<File, String> lob_renamedMap = new TreeMap<>();

        File lob_registeredFile;
        File lob_scannedFile;
        boolean lva_removeEntry;

        gob_listener.startScan();
        lco_scannedFiles = scan(gob_root, new HashMap<>());

        //create a copy of the paths that are registered
        lco_tmp = new TreeMap<>(gob_registeredPaths);


        for (Iterator<Map.Entry<File, BasicFileAttributes>> lob_registeredIterator = lco_tmp.entrySet().iterator(); lob_registeredIterator.hasNext();) {
            lva_removeEntry = false;
            lob_registeredEntry = lob_registeredIterator.next();

            for (Iterator<Map.Entry<File, BasicFileAttributes>> lob_scannedIterator = lco_scannedFiles.entrySet().iterator(); lob_scannedIterator.hasNext();) {

                lob_scannedEntry = lob_scannedIterator.next();

                //the creation time is the same
                if (lob_registeredEntry.getValue().creationTime().equals(lob_scannedEntry.getValue().creationTime())) {
                    lva_removeEntry = true;

                    //Check if the file was updated--------------------------------------
                    if (lob_registeredEntry.getValue().lastModifiedTime().compareTo(lob_scannedEntry.getValue().lastModifiedTime()) < 0) {
                        lli_updated.add(lob_scannedEntry.getKey());
                        //update the registered value
                        gob_registeredPaths.replace(lob_registeredEntry.getKey(), lob_scannedEntry.getValue());
                        lob_scannedEntry.getValue().fileKey();
//                        gob_registeredPaths.put(lob_registeredEntry.getKey(), lob_scannedEntry.getValue());
                    }

                    //Check if the file was renamed--------------------------------------
                    lob_scannedFile = lob_scannedEntry.getKey();
                    lob_registeredFile = lob_registeredEntry.getKey();
                    if (lob_scannedFile.getParentFile().equals(lob_registeredFile.getParentFile())) {
                        //check if the name has changed
                        if (!lob_scannedFile.getName().equals(lob_registeredFile.getName())) {
                            lob_renamedMap.put(lob_registeredFile, lob_scannedFile.getName());
                            //the file was renamed -> remove the old path
                            gob_registeredPaths.remove(lob_registeredEntry.getKey());
                            gob_registeredPaths.put(lob_scannedEntry.getKey(), lob_scannedEntry.getValue());
                        }
                    }

                    //check if the file was moved
                    if (!lob_registeredEntry.getKey().equals(lob_scannedEntry.getKey()) &&
                            !lob_registeredEntry.getKey().getParentFile().equals(lob_scannedEntry.getKey().getParentFile())) {

                        if (!lob_renamedMap.containsKey(lob_registeredEntry.getKey().getParentFile())) {
                            lob_movedMap.put(lob_registeredEntry.getKey(), lob_scannedEntry.getKey());
                        }
                        gob_registeredPaths.remove(lob_registeredEntry.getKey());
                        gob_registeredPaths.put(lob_scannedEntry.getKey(), lob_scannedEntry.getValue());
                    }

                    lob_scannedIterator.remove();
                    break;
                }
            }
            if (lva_removeEntry) {
                lob_registeredIterator.remove();
            }
        }

        lli_added = new ArrayList<>(lco_scannedFiles.keySet());
        lli_added.sort(PathFileComparator.PATH_COMPARATOR);

        lli_deleted = new ArrayList<>(lco_tmp.keySet());
        lli_deleted.sort(PathFileComparator.PATH_COMPARATOR);

        Path lob_deletePointer;
        Path lob_deleteParent = null;
        for (Iterator<File> lob_iterator = lli_deleted.iterator(); lob_iterator.hasNext();) {
            lob_deletePointer = lob_iterator.next().toPath();

            if (lob_deleteParent == null) {
                lob_deleteParent = lob_deletePointer;
            }

            if (!lob_deleteParent.equals(lob_deletePointer)) {
                if (lob_deletePointer.startsWith(lob_deleteParent)) {
                    gob_registeredPaths.remove(lob_deletePointer.toFile());
                    lob_iterator.remove();
                } else {
                    lob_deleteParent = lob_deletePointer;
                }
            }

        }

        lli_updated.sort(PathFileComparator.PATH_COMPARATOR);

        for (File lob_file : lli_added) {
            gob_listener.fileAdded(lob_file);
            register(lob_file);
        }

        for (File lob_file : lli_updated) {
            if (lob_file.isFile()) {
                gob_listener.fileUpdate(lob_file);
            }
        }

        for (File lob_file : lli_deleted) {
            gob_registeredPaths.remove(lob_file);
            gob_listener.fileDeleted(lob_file);
        }

        Map.Entry<File, String> lob_currentRenamedParent = null;
        Map.Entry<File, String> lob_renamedPointer;
        for (Iterator<Map.Entry<File, String>> lob_iterator = lob_renamedMap.entrySet().iterator(); lob_iterator.hasNext();) {
            lob_renamedPointer = lob_iterator.next();

            if (lob_currentRenamedParent == null) {
                lob_currentRenamedParent = lob_renamedPointer;
            }

            if (lob_renamedPointer != lob_currentRenamedParent) {
                if (lob_renamedPointer.getKey().toPath().startsWith(lob_currentRenamedParent.getKey().toPath())) {
                    lob_iterator.remove();
                } else {
                    lob_currentRenamedParent = lob_renamedPointer;
                }
            }
        }

        for (Map.Entry<File, String> lob_renamedEntry : lob_renamedMap.entrySet()) {
            gob_listener.fileRenamed(lob_renamedEntry.getKey(), lob_renamedEntry.getValue());
        }

        Map.Entry<File, File> lob_currentMoveParent = null;
        Map.Entry<File, File> lob_movedPointer;
        for (Iterator<Map.Entry<File, File>> lob_iterator = lob_movedMap.entrySet().iterator(); lob_iterator.hasNext();) {
            lob_movedPointer = lob_iterator.next();

            if (lob_currentMoveParent == null) {
                lob_currentMoveParent = lob_movedPointer;
            }

            if (lob_movedPointer != lob_currentMoveParent) {
                if (lob_movedPointer.getKey().toPath().startsWith(lob_currentMoveParent.getKey().toPath())) {
                    lob_iterator.remove();
                } else {
                    lob_currentMoveParent = lob_movedPointer;
                }
            }
        }

        for (Map.Entry<File, File> lob_movedEntry : lob_movedMap.entrySet()) {
            gob_listener.fileMoved(lob_movedEntry.getKey(), lob_movedEntry.getValue());
        }
        PreventDuplicateOperation.getDuplicateOperationPrevention().clear();

        System.out.println("----------------------------------------------------------");
        for (Map.Entry<File, BasicFileAttributes> lob_entry : gob_registeredPaths.entrySet()) {
            System.out.println(lob_entry.getKey());
        }
        System.out.println("----------------------------------------------------------");

        gob_listener.finishedScan();
    }

    /**
     * scan all files including the children of the file
     * @param iob_file pointer to the current path
     * @param ico_files all found paths
     * @return collection of all found paths
     */
    private HashMap<File, BasicFileAttributes> scan(File iob_file, HashMap<File, BasicFileAttributes> ico_files) throws IOException {
        Files.walk(iob_file.toPath())
                .filter(lob_file -> !TreeTool.filterRootFiles(lob_file) && iob_file.equals(gob_root))
                .forEach( lob_path -> {
                    BasicFileAttributes lob_attr;
                    try {
                        lob_attr = Files.readAttributes(lob_path, BasicFileAttributes.class);
                        ico_files.put(lob_path.toFile(), lob_attr);
                    } catch (IOException ignore) {

                    }
                });
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
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        try {
            init();
            while (true) {
                Thread.sleep(10000);
                scanAndCompare();
            }
        } catch (InterruptedException ignore) {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * start the scan routine in a new ThreadControl
     */
    public void start() {
        gob_thread = new Thread(this, DirectoryWatchService.class.getSimpleName());
        gob_thread.setDaemon(true);
        gob_thread.start();
    }

    /**
     * stop the WatchService
     */
    public void stop() {
        gob_thread.interrupt();
    }

    public void clear() {
        this.gob_registeredPaths.clear();
    }
}
