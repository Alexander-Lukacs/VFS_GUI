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

public class DirectoryWatchService implements Runnable{
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
    DirectoryWatchService(Path iob_root, FileChangeListener iob_listener) throws IOException {
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
        PreventDuplicateOperation lob_duplicates = TreeSingleton.getInstance().getDuplicateOperationsPrevention();
        HashMap<File, File> lco_moved = new HashMap<>();
        HashMap<File, File> lco_renamed = new HashMap<>();
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

            //iterate over the scanned files
            for (Iterator<Map.Entry<Path, FileTime>> lob_scannedIterator = lco_scanned.entrySet().iterator(); lob_scannedIterator.hasNext();) {
                Map.Entry<Path, FileTime> lob_scannedEntry = lob_scannedIterator.next();

                //the file was moved if the creation time of the file that was "added" is the same as the one that was "deleted"
                if (lob_scannedEntry.getValue().toMillis() == lob_entry.getValue().toMillis()) {

                    //the file was already moved from the UI, just ignore it
                    if (lob_duplicates.wasFilesMoved(lob_entry.getKey())) {
                        lob_duplicates.removeMoved(lob_entry.getKey());
                    } else {
                        //now we have 3 cases

                        //first case: the file was just renamed
                        if (!lob_entry.getKey().toFile().getName().equals(lob_scannedEntry.getKey().toFile().getName())) {
                            String oldFileName = lob_entry.getKey().toString().replaceFirst("[^\\\\]*$", lob_entry.getKey().toFile().getName());
                            lco_renamed.put(new File(oldFileName), lob_scannedEntry.getKey().toFile());

                            //second case: the file was moved and renamed
                            if (!lob_entry.getKey().startsWith(lob_scannedEntry.getKey().getParent())) {
                                lco_moved.put(lob_entry.getKey().toFile(), new File(oldFileName));
                            }

                        //third case: the file was just moved
                        } else {
                            lco_moved.put(lob_entry.getKey().toFile(), lob_scannedEntry.getKey().toFile());
                        }
                    }
                    wasFileRenamedOrMoved = true;
                    //remove the old file path from the registered items
                    gob_registerdPaths.remove(lob_entry.getKey());

                    //add the new path to the registered items
                    register(lob_scannedEntry.getKey());

                    //remove the file from the scanned map to speed up further iterations
                    lob_scannedIterator.remove();
                }
            }

            //the file was not moved or renamed
            if (!wasFileRenamedOrMoved) {
                //the file was deleted so delete it from the registered items
                gob_registerdPaths.remove(lob_entry.getKey());
                lli_delete.add(lob_entry.getKey().toFile());
            }
        }
        ArrayList<File> test = new ArrayList<>();
        lco_scanned.keySet().forEach(lob_key -> {
            if (TreeSingleton.getInstance().getDuplicateOperationsPrevention().wasFileCreated(lob_key)) {
                TreeSingleton.getInstance().getDuplicateOperationsPrevention().removeCreated(lob_key);
                try {
                    register(lob_key);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                test.add(lob_key.toFile());
            }
        });

        for (Map.Entry<File, File> entry : lco_renamed.entrySet()) {
            System.out.println("OLD: " + entry.getKey() + " NEW: " + entry.getValue());
            gob_listender.fileRenamed(entry.getKey().toPath(), entry.getValue().getName());
        }


        ArrayList<File> lli_renamedOrMovedKeySet = new ArrayList<>(lco_moved.keySet());
        lli_renamedOrMovedKeySet.sort(PathFileComparator.PATH_COMPARATOR);
        filesMoved(lli_renamedOrMovedKeySet, lco_moved);

        filesDeleted(lli_delete);

        test.sort(PathFileComparator.PATH_COMPARATOR);
        filesAdded(test);

        System.out.println("----------------------------------------------------------");
        for (Map.Entry<Path, FileTime> lob_entry : gob_registerdPaths.entrySet()) {
            System.out.println(lob_entry.getKey());
        }
        System.out.println("----------------------------------------------------------");
    }

    /**
     * call the fileMovedOrRenamed method of the listener for every file that was moved or renamed
     * @param ili_files contains all old file paths
     * @param ico_renamedOrMoved contains all new file paths
     */
    private void filesMoved(ArrayList<File> ili_files, HashMap<File, File> ico_renamedOrMoved) {
        filterChildren(ili_files);
        //ili_files.sort(PathFileComparator.PATH_COMPARATOR);
        for (File lob_file : ili_files) {
            System.out.println("RENAMED: " + lob_file.toPath() + " TO " + ico_renamedOrMoved.get(lob_file).toPath());
            gob_listender.fileMoved(lob_file.toPath(), ico_renamedOrMoved.get(lob_file).toPath());
        }
    }

    /**
     * call the fileDeleted method of the listener for every file that was deleted
     * @param ili_files contains all files that were deleted
     */
    private void filesDeleted(ArrayList<File> ili_files) {
        filterChildren(ili_files);

        for (File lob_file : ili_files) {
            System.out.println("DELETED: " + lob_file.toPath());
            gob_listender.fileDeleted(lob_file.toPath());
        }
    }

    /**
     * call the fileAdded method of the listener for every file that was added
     * @param ico_files contains all files that were added
     */
    private void filesAdded(Collection<File> ico_files) {
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

    private void filterChildren(ArrayList<File> ili_files) {
        ili_files.sort(PathFileComparator.PATH_COMPARATOR);
        Path lob_currentParent = null;
        File lob_currentNode;
        for (Iterator<File> lob_iterator = ili_files.iterator(); lob_iterator.hasNext();) {
            lob_currentNode = lob_iterator.next();
            if (lob_currentParent == null) {
                lob_currentParent = lob_currentNode.toPath();
            } else {
                if (lob_currentNode.toPath().startsWith(lob_currentParent)) {
                    lob_iterator.remove();
                } else {
                    lob_currentParent = lob_currentNode.toPath();
                }
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
                Thread.sleep(10000);
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
        Thread lob_runnerThread = new Thread(this, DirectoryWatchService.class.getSimpleName());
        lob_runnerThread.setDaemon(true);
        lob_runnerThread.start();
    }

    /**
     * stop the WatchService
     */
    public void stop() {
        gob_isRunning = false;
    }
}
