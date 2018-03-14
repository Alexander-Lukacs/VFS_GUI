package models.classes;

import cache.DirectoryCache;
import tools.TreeTool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class FileService {


    public static Collection<File> readAllFilesFromDirectory(File iob_file) {
        return getAllFiles(new ArrayList<>(), iob_file);
    }

    private static Collection<File> getAllFiles(Collection<File> ico_files, File iob_pointer) {
        boolean lva_canBeAdded = TreeTool.filterRootFiles(iob_pointer.toPath());
        if (!lva_canBeAdded || iob_pointer.equals(DirectoryCache.getDirectoryCache().getUserDirectory())) {

            if (!lva_canBeAdded) {
                ico_files.add(iob_pointer);
            }

            if (iob_pointer.isDirectory()) {
                for (File lob_child : Objects.requireNonNull(iob_pointer.listFiles())) {
                    getAllFiles(ico_files, lob_child);
                }
            }
        }

        return ico_files;
    }
}
