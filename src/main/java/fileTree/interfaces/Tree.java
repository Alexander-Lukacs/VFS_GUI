package fileTree.interfaces;

import java.io.File;
import java.util.Collection;
import java.util.Map;

public interface Tree {
    /**
     * remove all files from the tree and delete them
     */
    void clear(boolean iva_deleteAllFiles);

    /**
     * Add a new file to the tree and create the file if it does not exist
     * @param iob_file new file
     * @return true if the file was added and created, otherwise false
     */
    boolean addFile(File iob_file, boolean iva_isDirectory);

    /**
     * Add all files from a collection to the tree
     * @param iob_fileCollection contains all files to add
     * @return true if all files were added, otherwise false
     */
    boolean addFiles(Map<File, Boolean> iob_fileCollection);

    /**
     * Search a specific file in the tree and return it
     * @param iva_path the path of the file to search for
     * @return the found file
     */
    File getFile(String iva_path);

    /**
     * Get the tree root
     * @return root of the tree
     */
    File getRoot();

    /**
     * Get children from the root
     * @return all children from the root
     */
    Collection<File> getRootSubFiles();

    /**
     * Get all nodes by the path
     * @param ico_filePaths contains all file paths
     * @return the found file or null if the file is not in the tree
     */
    Collection<File> getFiles(Collection<String> ico_filePaths);

    /**
     * @return all files that the tree contains
     */
    Collection<File> getAllFiles();

    /**
     * @return all directories that the tree contains
     */
    Collection<File> getAllDirectories();

    /**
     * @return all files and directories that the tree contains
     */
    Collection<File> getAll();

    /**
     * delete a file in the tree and on the disk
     * @param iva_path path of the file to delete
     * @return true of the file was successfully deleted, otherwise false
     */
    boolean deleteFile(String iva_path);

    /**
     * delete a file in the tree and on the disk
     * @param iob_file file to delete
     * @return true of the file was successfully deleted, otherwise false
     */
    boolean deleteFile(File iob_file);

    /**
     * delete all files from a collection
     * @param ico_fileCollection contains all files to delete
     * @return true if all files were deleted, otherwise false
     */
    boolean deleteFiles(Collection<File> ico_fileCollection);

    /**
     * delete all files from a collection
     * @param ico_filePaths contains all files to delete
     * @return true if all files were deleted, otherwise false
     */
    boolean deleteFilesByPath(Collection<String> ico_filePaths);

    /**
     * move all files that the directory contains to the parent directory and delete
     * the directory itself
     * @param iob_directory directory to delete
     * @return ture if the directory was removed, otherwise false
     */
    boolean deleteDirectoryOnly(File iob_directory);

    /**
     * move all files that the directory contains to the parent directory and delete
     * the directory itself
     * @param iva_path directory path to delete
     * @return ture if the directory was removed, otherwise false
     */
    boolean deleteDirectoryOnly(String iva_path);

    /**
     * move a file
     * @param iob_node file to move
     * @param iva_destinationNode new file path
     * @return true if the file was moved, otherwise false
     */
    boolean moveFile(File iob_node, String iva_destinationNode);

    /**
     * move a file
     * @param iva_path file path to move
     * @param iva_destinationPath new file path
     * @return true if the file was moved, otherwise false
     */
    boolean moveFile(String iva_path, String iva_destinationPath);

    /**
     * rename a file
     * @param iva_path path of the file to renam
     * @param iva_newName new file name
     * @return true if the file was renamed, otherwise false
     */
    boolean renameFile(String iva_path, String iva_newName);

    /**
     * rename a file
     * @param iob_file file to rename
     * @param iva_newName new file name
     * @return true if the file was renamed, otherwise false
     */
    boolean renameFile(File iob_file, String iva_newName);

    /**
     * compare this tree
     * @param iob_tree tree to compare to
     * @return the difference between the two trees
     */
    TreeDifference compareTrees(Tree iob_tree);

    /**
     * replace the file of a node and move the node to the corresponding position in the tree
     * @param iob_file the old file that is not needed or does not exist anymore
     * @param iob_newFile the new file*
     * @param iob_nodePointer use this only if you are sure that the node is in the correct position and just the file has
     *                      to be replaced
     */
    boolean replaceFile(File iob_file, File iob_newFile, FileNode iob_nodePointer);
}

