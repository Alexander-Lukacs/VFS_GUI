package fileTree.models;

import fileTree.interfaces.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class TreeImpl implements Tree {

    private String gva_basePath;
    private String gva_rootDirectory;
    private FileNodeImpl gob_rootNode;

    public TreeImpl(String iva_rootDirectory) throws IOException{
        this.gob_rootNode = new FileNodeImpl(new File(iva_rootDirectory));
        this.gva_rootDirectory = gob_rootNode.getFile().getCanonicalPath();
        if (gob_rootNode.getFile().createNewFile()) {
            throw new IOException();
        }
        this.gva_basePath = this.gva_rootDirectory.replaceFirst("[^\\\\]*$", "");
    }

    public String getBasePath (){
        return gva_basePath;
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
        FileNode lob_getNode = null;
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
        FileNode lob_nodeToRemove;
        FileNode lob_parentNode;
        //-----------------------------------------------------------------------

        try {
            lob_nodeToRemove = searchNode(this.gob_rootNode,iva_path,0);
            if (lob_nodeToRemove == null) {
                return false;
            }

            lob_parentNode = lob_nodeToRemove.getParent();
            lob_nodeToRemove.setParent(null);

            //delete all children as well
            for (FileNode lob_file : lob_nodeToRemove.getChildren()) {
                deleteFile(lob_file.getFile());
            }

            lob_nodeToRemove.removeAllChildren();

            lob_nodeToRemove.getFile().delete();

            if (lob_parentNode != null) {
                lob_parentNode.removeChild(lob_nodeToRemove.getFile());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
        boolean lva_moveFailed = true;
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
                for (FileNode lob_child : lob_node.getChildren()) {
                    if(!moveFile(lob_child.getFile(), lob_parent.getFile().getCanonicalPath())){
                        lva_moveFailed = false;
                    }
                    lob_node.removeChild(lob_node.getFile());
                    lob_child.setParent(lob_parent);
                    lob_parent.addChild(lob_child);
                }
                lob_parent.removeChild(lob_node.getFile());
                if (lva_moveFailed) {
                    lob_node.getFile().delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * move a file
     *
     * @param iob_node file to move
     * @param iva_destinationNode new file path
     * @return true if the file was moved, otherwise false
     */
    @Override
    public boolean moveFile(File iob_node, String iva_destinationNode) {
        try {
            if (iob_node.isDirectory()) {
                FileUtils.moveDirectoryToDirectory(iob_node, iob_node.getParentFile().getParentFile(), false);
            } else {
                FileUtils.moveFileToDirectory(iob_node, iob_node.getParentFile().getParentFile(), false);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * move a file
     *
     * @param iva_path            file path to move
     * @param iva_destinationPath new file path
     * @return true if the file was moved, otherwise false
     */
    @Override
    public boolean moveFile(String iva_path, String iva_destinationPath) {
        //---------------Variables-------------
        FileNode lob_node;
        //-------------------------------------
        try {
            lob_node = searchNode(this.gob_rootNode, iva_path, 0);
            return lob_node != null && moveFile(lob_node.getFile(), iva_destinationPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * compare this tree
     *
     * @param iob_tree tree to compare to
     * @return the difference between the two trees
     */
    @Override
    public TreeDifference compareTrees(Tree iob_tree) {
        return null;
    }

//    @Override
//    public TreeDifference compareTrees(TreeInterface iob_tree) {
//        //---------------------------Variables-----------------------------
//        boolean lva_treeExceptionStatus = iob_tree.isExceptionActive();
//        boolean lva_tmpExceptionStatus = this.gva_nodeNotFoundExceptionStatus;
//        Collection<NodeInterface> lco_thisTreeCollection = this.getAll();
//        Collection<NodeInterface> lco_treeCollection = iob_tree.getAll();
//        Collection<NodeInterface> lco_nodesToUpdate = new ArrayList<>();
//        Collection<NodeInterface> lco_nodesToDelete = new ArrayList<>();
//        Collection<NodeInterface> lco_nodesToInsert = new ArrayList<>();
//        NodeInterface lob_treeNode;
//        TreeDifference rob_treeDifference = new TreeDifference();
//        //-----------------------------------------------------------------
//
//        iob_tree.setNodeNotFoundExceptionStatus(false);
//        this.gva_nodeNotFoundExceptionStatus = false;
//
//        for (NodeInterface lob_collectionNode : lco_thisTreeCollection) {
//            lob_treeNode = iob_tree.getNode(lob_collectionNode.getPath());
//
//            if (lob_treeNode == null) {
//                lco_nodesToDelete.add(lob_collectionNode);
//            } else if (lob_treeNode.getSize() != lob_collectionNode.getSize()) {
//                //the nodesize is different so it must be updated
//                lco_nodesToUpdate.add(lob_treeNode);
//            }
//            lco_treeCollection.remove(lob_treeNode);
//        }
//
//
//
//        rob_treeDifference.setNodesToUpdate(lco_nodesToUpdate);
//        rob_treeDifference.setNodesToDelete(lco_nodesToDelete);
//        rob_treeDifference.setNodesToInsert(lco_treeCollection);
//
//        iob_tree.setNodeNotFoundExceptionStatus(lva_treeExceptionStatus);
//        this.gva_nodeNotFoundExceptionStatus = lva_tmpExceptionStatus;
//
//        return rob_treeDifference;
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

    public String removeBasePath(String iva_path) {
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
}

