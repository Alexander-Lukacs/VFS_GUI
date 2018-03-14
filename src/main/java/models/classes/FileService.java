package models.classes;

import builder.RestClientBuilder;
import cache.DirectoryCache;
import cache.FileMapperCache;
import restful.clients.FileRestClient;
import threads.classes.ThreadManager;
import threads.constants.FileManagerConstants;
import tools.TreeTool;
import tools.Utils;
import tools.xmlTools.FileMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class FileService {

//    public static boolean compareFilesToServer() {
//        FileMapperCache lob_fileMapperCache = FileMapperCache.getFileMapperCache();
//        Collection<File> lco_files = readAllFilesFromDirectory(DirectoryCache.getDirectoryCache().getUserDirectory());
//        MappedFile lob_mappedFile;
//        long lva_lastModified;
//        FileRestClient lob_restClient = RestClientBuilder.buildFileRestClientWithAuth();
//        TreeDifference lob_treeDifference;
//
//        for (File lob_file : lco_files) {
//            lob_mappedFile = FileMapper.getFile(lob_file.toPath().toString());
//            try {
//                lva_lastModified = Files.readAttributes(lob_file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis();
//                if (lob_mappedFile.getFilePath() == null) {
//                    lob_mappedFile = new MappedFile(lob_file.toPath(), 1, lva_lastModified);
//                    lob_fileMapperCache.put(lob_mappedFile);
//                } else {
//                    if (lob_mappedFile.getLastModified() < lva_lastModified) {
//                        lob_mappedFile.setLastModified(lva_lastModified);
//                        lob_mappedFile.setVersion(lob_mappedFile.getVersion() + 1);
//                    }
//                    lob_fileMapperCache.put(lob_mappedFile);
//                }
//            } catch (IOException ignore) {
//
//            }
//        }
//
//        for (MappedFile t : FileMapperCache.getFileMapperCache().getAll()) {
//            System.out.println(t.toString());
//        }
//
//        lob_treeDifference = lob_restClient.compareClientAndServerTree();
//
//        if (lob_treeDifference == null) {
//            return false;
//        }
//
//        for (String lva_relativeFilePath : lob_treeDifference.getFilesToDelete()) {
//            File lob_deleteFile = new File(Utils.convertRelativeToAbsolutePath(lva_relativeFilePath, true));
//            ThreadManager.addCommandToFileManager(lob_deleteFile, FileManagerConstants.GC_DELETE, false, true);
//        }
//
//
//        return true;
//    }

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
