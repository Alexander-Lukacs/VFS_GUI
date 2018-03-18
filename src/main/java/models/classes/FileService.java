package models.classes;

import cache.DirectoryCache;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

public class FileService {


    public static Collection<File> readAllFilesFromDirectory(File iob_file) {
        return getAllFiles(new ArrayList<>(), iob_file);
    }

    private static Collection<File> getAllFiles(Collection<File> ico_files, File iob_pointer) {
        try {
            Files.walk(iob_pointer.toPath()).filter( lob_path ->
                    !TreeTool.filterRootFiles(lob_path) || lob_path.toFile().equals(DirectoryCache.getDirectoryCache().getUserDirectory())
            ).forEach( lob_path ->
                    ico_files.add(lob_path.toFile())
            );
        } catch (IOException ignore) {

        }

        return ico_files;
    }
}
