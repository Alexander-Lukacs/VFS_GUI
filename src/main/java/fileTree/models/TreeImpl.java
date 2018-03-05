package fileTree.models;

import fileTree.interfaces.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class TreeImpl implements Tree {

    private String gva_basePath;
    private String gva_rootDirectory;
    private FileNode gob_rootNode;

    public TreeImpl(String iva_rootDirectory) throws IOException{
        this.gob_rootNode = new FileNodeImpl(new File(iva_rootDirectory));
        this.gva_rootDirectory = gob_rootNode.getFile().getCanonicalPath();
        if (gob_rootNode.getFile().createNewFile()) {
            throw new IOException();
        }
        this.gva_basePath = this.gva_rootDirectory.replaceFirst("[^\\\\]*$", "");
    }

    public FileNode getRootNode() {
        return this.gob_rootNode;
    }

    /**
     * remove all files from the tree and delete them
     */
    @Override
    public void clear(boolean iva_deleteAllFiles) {
        if (iva_deleteAllFiles) {
            for (File lob_file : getAll()) {
                deleteFile(lob_file);
            }
        }

        this.gob_rootNode.removeAllChildren();
    }

    /**
     * Add a new file to the tree and create the file if it does not exist
     *
     * @param iob_file new file
     * @return true if the file was added and created, otherwise false
     */
    @Override
    public boolean addFile(File iob_file, boolean iva_isDirectory) {
        //---------------Variables-------------------------
        FileNode lob_parent;
        FileNode lob_newNode = new FileNodeImpl(iob_file);
        //-------------------------------------------------
        try {
            if (searchNode(this.gob_rootNode, iob_file.getCanonicalPath(), 0) != null) {
                return false;
            }

            lob_parent = addNode(this.gob_rootNode, lob_newNode, 0);

            //set the parent of the new node
            lob_newNode.setParent(lob_parent);
            if (iva_isDirectory && !iob_file.exists()) {
                if (!iob_file.mkdir()){
                    return false;
                }
            } else if (!iob_file.exists()){
                if (!iob_file.createNewFile()){
                    return false;
                }
            }

            //add the new node as child to his parent
            lob_parent.addChild(lob_newNode);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Add all files from a collection to the tree
     *
     * @param iob_fileCollection contains all files to add
     * @return true if all files were added, otherwise false
     */
    @Override
    public boolean addFiles(Map<File, Boolean> iob_fileCollection) {
        //------------------------Variables--------------------------------
        boolean lr_isNodeInsertionSuccssful = true;
        //-----------------------------------------------------------------

        for (File lob_file : iob_fileCollection.keySet()) {
            if(!addFile(lob_file, iob_fileCollection.get(lob_file))){
                lr_isNodeInsertionSuccssful = false;
            }
        }
        return lr_isNodeInsertionSuccssful;
    }

    /**
     * Search a specific file in the tree and return it
     *
     * @param iva_path the path of the file to search for
     * @return the found file
     */
    @Override
    public File getFile(String iva_path) {
        //---------------------------Variables-----------------------------------
        FileNode lob_getNode;
        //-----------------------------------------------------------------------
        try {
            lob_getNode = searchNode(this.gob_rootNode ,iva_path,0);

            if (lob_getNode == null) {
                return null;
            }

            return lob_getNode.getFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the tree root
     *
     * @return root of the tree
     */
    @Override
    public File getRoot() {
        return this.gob_rootNode.getFile();
    }


    /**
     * Get children from the root
     *
     * @return all children from the root
     */
    @Override
    public Collection<File> getRootSubFiles() {
        //--------------------Variables-----------------------
        Collection<File> rco_rootChildren = new ArrayList<>();
        //----------------------------------------------------

        for (FileNode lob_node : this.getRootNode().getChildren()) {
            rco_rootChildren.add(lob_node.getFile());
        }
        return rco_rootChildren;
    }

    /**
     * Get all nodes by the path
     *
     * @param ico_filePaths contains all file paths
     * @return the found file or null if the file is not in the tree
     */
    @Override
    public Collection<File> getFiles(Collection<String> ico_filePaths) {
        //---------------------------------------Variables----------------------------------------------------------
        Collection<File> rco_foundFiles = new ArrayList<>();
        File lob_searchedFile;
        //----------------------------------------------------------------------------------------------------------

        for (String iva_path : ico_filePaths){
            lob_searchedFile = getFile(iva_path);
            if (lob_searchedFile != null) {
                rco_foundFiles.add(lob_searchedFile);
            }
        }
        return rco_foundFiles;
    }

    /**
     * @return all files that the tree contains
     */
    @Override
    public Collection<File> getAllFiles() {
        return getAllFiles(this.gob_rootNode, new ArrayList<>());
    }

    /**
     * @return all directories that the tree contains
     */
    @Override
    public Collection<File> getAllDirectories() {
        return getAllDirectories(this.gob_rootNode, new ArrayList<>());
    }

    /**
     * @return all files and directories that the tree contains
     */
    @Override
    public Collection<File> getAll() {
        return getAll(this.gob_rootNode, new ArrayList<>());
    }

    /**
     * delete a file in the tree and on the disk
     *
     * @param iva_path path of the file to delete
     * @return true of the file was successfully deleted, otherwise false
     */
    @Override
    public boolean deleteFile(String iva_path) {
        //---------------------------Variables-----------------------------------
        FileNode lob_parent;
        FileNode lob_nodeToDelete;
        boolean lva_deleteResult;
        //-----------------------------------------------------------------------
        try {
            lob_nodeToDelete = searchNode(gob_rootNode, iva_path, 0);

            if (lob_nodeToDelete == null) {
                return false;
            }

            lob_parent = lob_nodeToDelete.getParent();

            lva_deleteResult = delete(iva_path);

            if (lob_parent != null) {
                lob_parent.removeChild(lob_nodeToDelete.getFile());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return lva_deleteResult;
    }

    /**
     * delete a file in the tree and on the disk
     *
     * @param iob_file file to delete
     * @return true of the file was successfully deleted, otherwise false
     */
    @Override
    public boolean deleteFile(File iob_file) {
        try {
            return deleteFile(iob_file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * delete all files from a collection
     *
     * @param ico_fileCollection contains all files to delete
     * @return true if all files were deleted, otherwise false
     */
    @Override
    public boolean deleteFiles(Collection<File> ico_fileCollection) {
        //--------------------------------------Variables--------------------------------------------------
        boolean lr_isNodeInsertionSuccessful = true;
        //-------------------------------------------------------------------------------------------------
        for (File lob_collectionNode : ico_fileCollection) {
            if (!deleteFile(lob_collectionNode)) {
                lr_isNodeInsertionSuccessful = false;
            }
        }

        return lr_isNodeInsertionSuccessful;
    }

    /**
     * delete all files from a collection
     *
     * @param ico_filePaths contains all files to delete
     * @return true if all files were deleted, otherwise false
     */
    @Override
    public boolean deleteFilesByPath(Collection<String> ico_filePaths) {
        //------------------------------------Variables----------------------------------------------
        boolean lr_isNodeInsertionSuccessful = true;
        //-------------------------------------------------------------------------------------------
        for (String lva_nodePath : ico_filePaths) {
            if(!deleteFile(lva_nodePath)){
                lr_isNodeInsertionSuccessful = false;
            }
        }

        return lr_isNodeInsertionSuccessful;
    }

    /**
     * move all files that the directory contains to the parent directory and delete
     * the directory itself
     *
     * @param iob_directory directory to delete
     * @return ture if the directory was removed, otherwise false
     */
    @Override
    public boolean deleteDirectoryOnly(File iob_directory) {
        try {
            if (!iob_directory.isDirectory()) {
                return false;
            }

            return deleteDirectoryOnly(iob_directory.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * move all files that the directory contains to the parent directory and delete
     * the directory itself
     *
     * @param iva_path directory path to delete
     * @return ture if the directory was removed, otherwise false
     */
    @Override
    public boolean deleteDirectoryOnly(String iva_path) {
        //----------------------Variables-----------------------
        FileNode lob_parent;
        FileNode lob_node;
        FileNode lob_child;
        //------------------------------------------------------

        try {
            lob_node  = searchNode(this.gob_rootNode, iva_path, 0);

            if (lob_node == null) {
                return false;
            }

            lob_parent = lob_node.getParent();

            if (!lob_node.getFile().isDirectory()) {
                return false;
            }

            if (lob_parent != null) {
                for (Object lob_object : lob_node.getChildren().toArray()) {
                    lob_child = (FileNode) lob_object;
                    if (!moveFile(lob_child.getFile(), lob_parent.getFile().getCanonicalPath(), false)) {
                        return false;
                    }
                }
                lob_parent.removeChild(lob_node.getFile());
                lob_node.getFile().delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * move a file
     * @param iob_file file to move
     * @param iva_destinationNode new file path
     * @param iva_moveJustInTree its possible that the file was already moved by the os or the user, to prevent errors
     *                           this parameter is used to move the file just in the tree object and not on the
     *                           file system
     * @return true if the file was moved, otherwise false
     */
    @Override
    public boolean moveFile(File iob_file, String iva_destinationNode, boolean iva_moveJustInTree) {

        try {
            FileNode lob_fileNode = searchNode(gob_rootNode, iob_file.getCanonicalPath(), 0);
            FileNode lob_destination = searchNode(gob_rootNode, iva_destinationNode, 0);
            File lob_destinationFile;
            if (lob_fileNode == null || lob_destination == null) {
                return false;
            }

            lob_destinationFile = lob_destination.getFile();

            if (!iva_moveJustInTree) {
                if (lob_fileNode.getFile().isDirectory()) {
                    FileUtils.moveDirectoryToDirectory(iob_file, lob_destinationFile, false);
                } else {
                    FileUtils.moveFileToDirectory(iob_file, lob_destinationFile, false);
                }
            }
            lob_fileNode.getParent().getChildren().remove(lob_fileNode);
            lob_destination.getChildren().add(lob_fileNode);
            lob_fileNode.setParent(lob_destination);
            updateFilePath(lob_fileNode.getParent().getFile().toPath(), lob_fileNode);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * move a file
     * @param iva_path file path to move
     * @param iva_destinationPath new file path
     * @param iva_moveJustInTree its possible that the file was already moved by the os or the user, to prevent errors
     *                           this parameter is used to move the file just in the tree object and not on the
     *                           file system
     * @return true if the file was moved, otherwise false
     */
    public boolean moveFile(String iva_path, String iva_destinationPath, boolean iva_moveJustInTree) {
        //---------------Variables-------------
        FileNode lob_node;
        //-------------------------------------
        try {
            lob_node = searchNode(this.gob_rootNode, iva_path, 0);
            return lob_node != null && moveFile(lob_node.getFile(), iva_destinationPath, iva_moveJustInTree);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * rename a file
     *
     * @param iva_path    path of the file to renam
     * @param iva_newName new file name
     * @return true if the file was renamed, otherwise false
     */
    @Override
    public boolean renameFile(String iva_path, String iva_newName) {
        //--------------------------Variables-----------------------
        FileNode lob_node;
        //----------------------------------------------------------

        try {
            lob_node = searchNode(gob_rootNode, iva_path, 0);
            if (lob_node == null) {
                return false;
            }

            iva_newName = iva_path.replaceFirst("[^\\\\]*$", iva_newName);
            File lob_renamedFile = new File(iva_newName);
            lob_node.getFile().renameTo(lob_renamedFile);
            lob_node.setFile(lob_renamedFile);
            if (lob_node.getFile().isDirectory()) {
                for (FileNode lob_child : lob_node.getChildren()) {
                    updateFilePath(lob_node.getFile().toPath(), lob_child);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * rename a file
     *
     * @param iob_file    file to rename
     * @param iva_newName new file name
     * @return true if the file was renamed, otherwise false
     */
    @Override
    public boolean renameFile(File iob_file, String iva_newName) {
        try {
            return renameFile(iob_file.getCanonicalPath(), iva_newName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * compare two trees
     *
     * @param iob_tree tree to compare to
     * @return the difference between the two trees
     */
    @Override
    public TreeDifference compareTrees(Tree iob_tree) {
        //------------------Variables----------------------------
        TreeDifference lob_difference = new TreeDifferenceImpl();
        Collection<File> lco_thisFiles = getAll();
        Collection<File> lco_compareFiles = iob_tree.getAll();
        String lva_thisRootPath;
        String lva_treeRootPath;
        String lva_thisFilePath;
        String lva_treeFilePath;
        Iterator<File> lob_treeFileIterator;
        Iterator<File> lob_thisFileIterator;
        File lob_treeFile;
        File lob_thisFile;
        //-------------------------------------------------------

        try {
            lva_thisRootPath = this.gva_rootDirectory;
            lva_treeRootPath = iob_tree.getRoot().getCanonicalPath();

            for (lob_thisFileIterator = lco_thisFiles.iterator(); lob_thisFileIterator.hasNext();) {
                lob_thisFile = lob_thisFileIterator.next();
                //remove everything including from the path including root to get a relative path
                lva_thisFilePath = lob_thisFile.getCanonicalPath();
                lva_thisFilePath = lva_thisFilePath.replace(lva_thisRootPath, "");
                for(lob_treeFileIterator = lco_compareFiles.iterator(); lob_treeFileIterator.hasNext();) {
                    lob_treeFile = lob_treeFileIterator.next();
                    lva_treeFilePath = lob_treeFile.getCanonicalPath();
                    lva_treeFilePath = lva_treeFilePath.replace(lva_treeRootPath, "");
                    //check if the relative paths are the same
                    if (lva_thisFilePath.equals(lva_treeFilePath)) {
                        //check if the file on the server is newer
                        if (lob_thisFile.lastModified() > lob_treeFile.lastModified()) {
                            //add the file to the update list
                            lob_difference.addFileToUpdate(lva_treeFilePath);
                            lob_treeFileIterator.remove();
                            lob_thisFileIterator.remove();
                            break;
                        }
                    }
                }
            }

            for (File lob_file : lco_thisFiles) {
                lva_thisFilePath = lob_file.getCanonicalPath();
                lva_thisFilePath = lva_thisFilePath.replace(lva_thisRootPath, "");
                lob_difference.addFileToInsert(lva_thisFilePath);
            }

            for (File lob_file : lco_compareFiles) {
                lva_treeFilePath = lob_file.getCanonicalPath();
                lva_treeFilePath = lva_treeFilePath.replace(lva_treeRootPath, "");
                lob_difference.addFileToInsert(lva_treeFilePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return lob_difference;
    }

//    /**
//     * its possible that some nodes point to files, do not exist anymore. This can happen after a directory or a file
//     * has been moved
//     *
//     * WARNIG: This method must be called with a node that points to a correct file, otherwise the
//     * behaviour is unexpected
//     *
//     * @param iob_nodeToUpdate node that could contain a non existing file
//     * @param iva_parentPath path of the parent file (must have a correct file path)
//     */
//    private void refreshTreeParentNodes(FileNode iob_nodeToUpdate, String iva_parentPath) {
//        //---------------------------------Variables----------------------------
//        String lva_newFilePath;
//        //----------------------------------------------------------------------
//
//        try {
//            if (!iob_nodeToUpdate.getFile().exists()) {
//                lva_newFilePath = iva_parentPath + "\\" + iob_nodeToUpdate.getFile().getName();
//
//                iob_nodeToUpdate.setFile(new File(lva_newFilePath));
//            }
//
//            for (FileNode lob_child : iob_nodeToUpdate.getChildren()) {
//                refreshTreeParentNodes(lob_child, iob_nodeToUpdate.getFile().getCanonicalPath());
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

    private FileNode addNode(FileNode iob_parent, FileNode iob_nodeToInsert, int depth) throws IOException{
        //------------------------------------Variables---------------------------------------------------------
        String[] lar_childNodePath;
        String[] lar_parentNodePath;
        String[] lar_nodeToInsertPath = convertPathToArray(iob_nodeToInsert.getFile().getCanonicalPath());
        FileNode lob_newDirectory;
        StringBuilder lob_newDirectoryPath = new StringBuilder(this.gva_rootDirectory + "\\");
        int lva_loopCounter;
        //------------------------------------------------------------------------------------------------------

        lar_parentNodePath = convertPathToArray(iob_parent.getFile().getCanonicalPath());

        //loop over all children of the current parent node
        for (FileNode lob_childNode : iob_parent.getChildren()) {
            if (lob_childNode.getFile().isDirectory()) {
                lar_childNodePath = convertPathToArray(lob_childNode.getFile().getCanonicalPath());
                if (lar_nodeToInsertPath[depth].equals(lar_childNodePath[depth])) {
                    if (lar_nodeToInsertPath.length >= (depth + 1)) {
                        if (lar_nodeToInsertPath[depth + 1].equals(lar_childNodePath[depth + 1])) {
                            return addNode(lob_childNode, iob_nodeToInsert, ++depth);
                        }
                    }
                }
            }
        }

        //the parent directory of the file that we want to insert does not exist yet
        if (lar_nodeToInsertPath.length > (lar_parentNodePath.length + 1)) {
            lva_loopCounter = 1;
            //build the path for the parent directory
            do {
                lob_newDirectoryPath.append(lar_nodeToInsertPath[lva_loopCounter]).append("\\");
                lva_loopCounter++;
            } while (lva_loopCounter < depth + 2);
            //remove the last '\' from the directory path
            lob_newDirectoryPath.delete(lob_newDirectoryPath.lastIndexOf("\\"), lob_newDirectoryPath.length());
            //create the new directory
            //lob_newDirectory = createNewDirectory(lob_newDirectoryPath.toString());
            lob_newDirectory = new FileNodeImpl(new File(lob_newDirectoryPath.toString()));
            //set the current node as parent for the new directory
            lob_newDirectory.setParent(iob_parent);
            //add the new directory as child to the current node
            iob_parent.addChild(lob_newDirectory);
            createDirectory(lob_newDirectory);
            return addNode(lob_newDirectory, iob_nodeToInsert, ++depth);
        }

        if (!lar_parentNodePath[depth].equals(lar_nodeToInsertPath[depth])) {
            return iob_parent.getParent();
        }

        return iob_parent;
    }

    private String[] convertPathToArray(String iva_path) {
        iva_path = removeBasePath(iva_path);
        return iva_path.split("\\\\");
    }

    private String removeBasePath(String iva_path) {
        String lva_replacePattern = this.gva_basePath.replaceAll("\\\\", "\\\\\\\\");
        return iva_path.replaceFirst(lva_replacePattern, "");
    }

    /**
     * search for a specific node and return it
     * @param iob_parent current node pointer
     * @param iva_path path of the file to find
     * @param depth of the tree
     * @return the found file node or null
     */
    private FileNode searchNode(FileNode iob_parent, String iva_path, int depth) throws IOException{
        //-------------------------------Variables------------------------------------------
        String[] lar_childPath;
        String[] lar_searchPath = convertPathToArray(iva_path);
        //----------------------------------------------------------------------------------

        if (iob_parent.getFile().getCanonicalPath().equals(iva_path)) {
            return iob_parent;
        }

        for (FileNode lob_childNode : iob_parent.getChildren()) {
            lar_childPath = convertPathToArray(lob_childNode.getFile().getCanonicalPath());
            if (lar_searchPath[depth].equals(lar_childPath[depth])) {
                if (lar_searchPath.length >= (depth + 1)) {
                    if (lar_searchPath[depth + 1].equals(lar_childPath[depth + 1])) {
                        return searchNode(lob_childNode, iva_path, ++depth);
                    }
                }
            }
        }

        return null;
    }

    /**
     * get all nodes in the tree
     * @param iob_node pointer to the current node in the tree
     * @param ico_files contains all found nodes
     * @return all found nodes
     */
    private Collection<File> getAllFiles(FileNode iob_node, Collection<File> ico_files) {
        if (iob_node.getFile().isFile()) {
            ico_files.add(iob_node.getFile());
            return ico_files;
        }

        for (FileNode lob_child : iob_node.getChildren()) {
            ico_files = getAllFiles(lob_child, ico_files);
        }

        return ico_files;
    }

    /**
     * get all nodes that contain a directory in this tree as collection
     * @param iob_node pointer to the current node in the tree
     * @param ico_directories contains all found directories
     * @return all found directories
     */
    private Collection<File> getAllDirectories(FileNode iob_node, Collection<File> ico_directories) {
        if (iob_node.getFile().isDirectory() && iob_node != this.gob_rootNode) {
            ico_directories.add(iob_node.getFile());
        }

        for (FileNode lob_child : iob_node.getChildren()) {
            ico_directories = getAllDirectories(lob_child, ico_directories);
        }

        return ico_directories;
    }

    /**
     * get all Nodes that contain a file in this tree as collection
     * @param iob_node pointer to the current node in the tree
     * @param ico_nodes contains all found nodes so far
     * @return all found nodes
     */
    private Collection<File> getAll(FileNode iob_node, Collection<File> ico_nodes) {
        if (iob_node != this.gob_rootNode) {
            ico_nodes.add(iob_node.getFile());
        }

        for (FileNode lob_child : iob_node.getChildren()) {
            ico_nodes = getAll(lob_child, ico_nodes);
        }

        return ico_nodes;
    }

    /**
     * create a new directory
     * @param iob_node the file that points to the directory
     * @return true if the directory was created, otherwise false
     */
    private boolean createDirectory(FileNode iob_node) {
        return iob_node.getFile().mkdir();
    }

    private boolean delete(String iva_path) {
        //---------------------------Variables-----------------------------------
        FileNode lob_nodeToRemove;
        //-----------------------------------------------------------------------

        try {
            lob_nodeToRemove = searchNode(this.gob_rootNode,iva_path,0);
            if (lob_nodeToRemove == null) {
                return false;
            }

            lob_nodeToRemove.setParent(null);

            //delete all children as well
            Iterator<FileNode> lob_iterator;
            for (lob_iterator = lob_nodeToRemove.getChildren().iterator(); lob_iterator.hasNext();) {
                FileNode lob_file = lob_iterator.next();
                delete(lob_file.getFile().getCanonicalPath());
                lob_iterator.remove();
            }
            return lob_nodeToRemove.getFile().delete();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * this method is used to update all file paths after the file was moved
     * @param iob_basePath basePath
     * @param iob_node node that contains the file that needs a update
     */
    private void updateFilePath(Path iob_basePath, FileNode iob_node) {
        String lva_newFilePath = iob_basePath.toString() + "\\" + iob_node.getFile().getName();
        iob_node.setFile(new File(lva_newFilePath));

        if (iob_node.getFile().isDirectory()) {
            for (FileNode lob_child : iob_node.getChildren()) {
                updateFilePath(iob_node.getFile().toPath(), lob_child);
            }
        }
    }
}

