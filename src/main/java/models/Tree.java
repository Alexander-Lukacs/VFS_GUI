package models;

import exceptions.NodeNotFoundException;
import interfaces.NodeInterface;
import interfaces.TreeInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Tree implements TreeInterface {

    private static final String BASE_DIRECTORY = "C:\\\\Users\\\\Mesut\\\\Documents\\\\Fileserver\\\\";
    private static final String GC_ROOT_PATH = "C:\\Users\\Mesut\\Documents\\Fileserver\\root";
    private static final String GC_ROOT_NAME = "<ROOT>";
    private static final String GC_NODE_NOT_FOUND = "Node not found: ";

    private NodeInterface gob_root;
    private boolean gva_nodeNotFoundExceptionStatus;

    public Tree() {
        this.gob_root = NodeFactory.createDirectoryNode(GC_ROOT_NAME, GC_ROOT_PATH);
        this.gva_nodeNotFoundExceptionStatus = true;
    }

    @Override
    public void clear() {
        this.gob_root.removeAllChildren();
    }

    @Override
    public boolean addNode(NodeInterface iob_node) {
        //---------------Variables---------------
        NodeInterface lob_parent;
        //---------------------------------------

        NodeInterface node = searchNode(gob_root, iob_node.getPath(), 0);

        if (node != null) {
            return false;
        }

        lob_parent = addNode(gob_root, iob_node, 0);

        //set the parent of the new node
        iob_node.setParent(lob_parent);

        //add the new node as child to his parent
        lob_parent.addChild(iob_node);

        return true;
    }

    @Override
    public boolean addNodes(Collection<NodeInterface> ico_nodeCollection) {
        //------------------------Variables--------------------------------
        boolean lr_isNodeInsertionSuccssful = true;
        //-----------------------------------------------------------------

        for (NodeInterface lob_node : ico_nodeCollection) {
            if(!addNode(lob_node)){
                lr_isNodeInsertionSuccssful = false;
            }
        }
        return lr_isNodeInsertionSuccssful;
    }

    @Override
    public NodeInterface getNode(String iva_path) throws NodeNotFoundException {
        //---------------------------Variables-----------------------------------
        NodeInterface lob_getNode = searchNode(gob_root,iva_path,0);
        //-----------------------------------------------------------------------

        if (lob_getNode == null && gva_nodeNotFoundExceptionStatus){
            throw new NodeNotFoundException(GC_NODE_NOT_FOUND + iva_path);
        }

        return lob_getNode;
    }

    @Override
    public NodeInterface getRoot() {
        return this.gob_root;
    }

    @Override
    public Collection<NodeInterface> getRootSubNodes() {
        return this.gob_root.getChildren();
    }

    @Override
    public Collection<NodeInterface> getNodesByPath(Collection<String> ico_nodePaths) throws NodeNotFoundException {
        //---------------------------------------Variables----------------------------------------------------------
        Collection<NodeInterface> rco_foundNodesByPath = new ArrayList<>();
        NodeInterface lob_searchedNode;
        //----------------------------------------------------------------------------------------------------------

        for (String iva_path : ico_nodePaths){
            lob_searchedNode = searchNode(gob_root,iva_path,0);
            if (lob_searchedNode != null) {
                rco_foundNodesByPath.add(lob_searchedNode);
            }
        }
        return rco_foundNodesByPath;
    }

    @Override
    public Collection<NodeInterface> getAllFiles() {
        return getAllFiles(this.gob_root, new ArrayList<>());
    }

    @Override
    public Collection<NodeInterface> getAllDirectories() {
        return getAllDirectories(this.gob_root, new ArrayList<>());
    }

    @Override
    public Collection<NodeInterface> getAll() {
        return getAll(this.gob_root, new ArrayList<>());
    }

    @Override
    public boolean removeNode(String iva_path) throws NodeNotFoundException {
        //---------------------------Variables-----------------------------------
        NodeInterface lob_nodeToRemove = searchNode(gob_root,iva_path,0);
        NodeInterface lob_parentNode;
        //-----------------------------------------------------------------------

        if(lob_nodeToRemove == null && gva_nodeNotFoundExceptionStatus){
            throw new NodeNotFoundException(GC_NODE_NOT_FOUND + iva_path);
        }

        if (lob_nodeToRemove == null) {
            return false;
        }

        lob_parentNode = lob_nodeToRemove.getParent();
        lob_nodeToRemove.setParent(null);
        lob_nodeToRemove.removeAllChildren();

        if (lob_parentNode != null) {
            lob_parentNode.removeChild(lob_nodeToRemove);
        }

        return true;
    }

    @Override
    public boolean removeNode(NodeInterface iob_node) throws NodeNotFoundException {
        return removeNode(iob_node.getPath());
    }

    @Override
    public boolean removeNodes(Collection<NodeInterface> ico_nodeCollection) throws NodeNotFoundException {
        //--------------------------------------Variables--------------------------------------------------
        boolean lr_isNodeInsertionSuccssful = true;
        //-------------------------------------------------------------------------------------------------
        for (NodeInterface lob_collectionNode : ico_nodeCollection) {
            if (!removeNode(lob_collectionNode.getPath())) {
                lr_isNodeInsertionSuccssful = false;
            }
        }

        return lr_isNodeInsertionSuccssful;
    }

    @Override
    public boolean removeNodesByPath(Collection<String> ico_nodePaths) throws NodeNotFoundException {
        //------------------------------------Variables----------------------------------------------
        boolean lr_isNodeInsertionSuccssful = true;
        //-------------------------------------------------------------------------------------------
        for (String lva_nodePath : ico_nodePaths) {
            if(!removeNode(lva_nodePath)){
                lr_isNodeInsertionSuccssful = false;
            }
        }

        return lr_isNodeInsertionSuccssful;
    }

    @Override
    public boolean removeDirectoryOnly(NodeInterface iob_node) {
        //----------------------Variables-----------------------
        NodeInterface lob_parent = iob_node.getParent();
        NodeInterface lob_child;
        //------------------------------------------------------

        if (!iob_node.isDirectory()) {
            return false;
        }

        if (getNode(iob_node.getPath()) == null){
            return false;
        }

        if (lob_parent != null) {
            for (Iterator<NodeInterface> iterator = iob_node.getChildren().iterator(); iterator.hasNext();) {
                lob_child = iterator.next();
                lob_child.setParent(lob_parent);
                lob_parent.addChild(lob_child);
                iterator.remove();
            }
            lob_parent.removeChild(iob_node);
        }
        return true;
    }

    @Override
    public boolean removeDirectoryOnly(String iva_path) {
        //------------------Variables--------------------
        NodeInterface lob_node = getNode(iva_path);
        //-----------------------------------------------

        return lob_node != null && removeDirectoryOnly(lob_node);

    }

    @Override
    public List<NodeInterface> toList() {
        return (List<NodeInterface>) getAll();
    }

    @Override
    public NodeInterface[] toArray() {
        return (NodeInterface[]) getAll().toArray();
    }

    @Override
    public void setNodeNotFoundExceptionStatus(boolean iva_exceptionStatus) {
        this.gva_nodeNotFoundExceptionStatus = iva_exceptionStatus;
    }

    @Override
    public boolean isExceptionActive() {
        return gva_nodeNotFoundExceptionStatus;
    }

    @Override
    public boolean moveNode(NodeInterface iob_node, NodeInterface iob_destinationNode) {
        return moveNode(iob_node.getPath(), iob_destinationNode.getPath());
    }

    @Override
    public boolean moveNode(String iva_path, String iva_destinationPath) {
        //---------------------------Variables----------------------------
        NodeInterface lob_destinationNode = getNode(iva_destinationPath);
        NodeInterface lob_node = getNode(iva_path);
        NodeInterface lob_parent;
        //----------------------------------------------------------------

        if (lob_destinationNode == null || lob_node == null) {
            return false;
        }

        lob_parent = lob_node.getParent();

        if (lob_parent != null) {
            lob_parent.removeChild(lob_node);
        }

        lob_node.setParent(lob_destinationNode);
        lob_destinationNode.addChild(lob_node);

        return true;
    }

    @Override
    public TreeDifference compareTrees(TreeInterface iob_tree) {
        //---------------------------Variables-----------------------------
        boolean lva_treeExceptionStatus = iob_tree.isExceptionActive();
        boolean lva_tmpExceptionStatus = this.gva_nodeNotFoundExceptionStatus;
        Collection<NodeInterface> lco_thisTreeCollection = this.getAll();
        Collection<NodeInterface> lco_treeCollection = iob_tree.getAll();
        Collection<NodeInterface> lco_nodesToUpdate = new ArrayList<>();
        Collection<NodeInterface> lco_nodesToDelete = new ArrayList<>();
        Collection<NodeInterface> lco_nodesToInsert = new ArrayList<>();
        NodeInterface lob_treeNode;
        TreeDifference rob_treeDifference = new TreeDifference();
        //-----------------------------------------------------------------

        iob_tree.setNodeNotFoundExceptionStatus(false);
        this.gva_nodeNotFoundExceptionStatus = false;

        for (NodeInterface lob_collectionNode : lco_thisTreeCollection) {
            lob_treeNode = iob_tree.getNode(lob_collectionNode.getPath());

            if (lob_treeNode == null) {
                lco_nodesToDelete.add(lob_collectionNode);
            } else if (lob_treeNode.getSize() != lob_collectionNode.getSize()) {
                //the nodesize is different so it must be updated
                lco_nodesToUpdate.add(lob_treeNode);
            }
            lco_treeCollection.remove(lob_treeNode);
        }



        rob_treeDifference.setNodesToUpdate(lco_nodesToUpdate);
        rob_treeDifference.setNodesToDelete(lco_nodesToDelete);
        rob_treeDifference.setNodesToInsert(lco_treeCollection);

        iob_tree.setNodeNotFoundExceptionStatus(lva_treeExceptionStatus);
        this.gva_nodeNotFoundExceptionStatus = lva_tmpExceptionStatus;

        return rob_treeDifference;
    }

    private NodeInterface addNode(NodeInterface iob_parent, NodeInterface iob_nodeToInsert, int depth) {
        //------------------------------------Variables---------------------------------------------------------
        String[] lar_childNodePath;
        String[] lar_parentNodePath;
        String[] lar_nodeToInsertPath = convertPathToArray(iob_nodeToInsert.getPath());
        NodeInterface lob_newDirectory;
        StringBuilder lob_newDirectoryPath = new StringBuilder(GC_ROOT_PATH + "\\");
        int lva_loopCounter;
        //------------------------------------------------------------------------------------------------------

        lar_parentNodePath = convertPathToArray(iob_parent.getPath());

        //loop over all children of the current parent node
        for (NodeInterface lob_childNode : iob_parent.getChildren()) {
            if (lob_childNode.isDirectory()) {
                lar_childNodePath = convertPathToArray(lob_childNode.getPath());
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
            lob_newDirectory = createNewDirectory(lob_newDirectoryPath.toString());
            //set the current node as parent for the new directory
            lob_newDirectory.setParent(iob_parent);
            //add the new directory as child to the current node
            iob_parent.addChild(lob_newDirectory);
            return addNode(lob_newDirectory, iob_nodeToInsert, ++depth);
        }

        if (!lar_parentNodePath[depth].equals(lar_nodeToInsertPath[depth])) {
            return iob_parent.getParent();
        }

        return iob_parent;
    }

    private String removeBasePath(String iva_path) {
        return iva_path.replaceFirst(BASE_DIRECTORY, "");
    }

    private String[] convertPathToArray(String iva_path) {
        iva_path = removeBasePath(iva_path);
        return iva_path.split("\\\\");
    }

    private NodeInterface createNewDirectory(String iva_path) {
        //-------------------Variables-------------------------
        String[] lva_directoryPath = convertPathToArray(iva_path);
        String lva_directoryName = lva_directoryPath[lva_directoryPath.length - 1];
        //-----------------------------------------------------

        return NodeFactory.createDirectoryNode(lva_directoryName, iva_path);
    }

    private NodeInterface searchNode(NodeInterface iob_parent, String iva_path, int depth) {
        //-------------------------------Variables------------------------------------------
        String[] lar_childPath;
        String[] lar_searchPath = convertPathToArray(iva_path);
        //----------------------------------------------------------------------------------

        if (iob_parent.getPath().equals(iva_path)) {
            return iob_parent;
        }

        for (NodeInterface lob_childNode : iob_parent.getChildren()) {
            lar_childPath = convertPathToArray(lob_childNode.getPath());
//            if (lar_childPath[depth].equals(lar_searchPath[depth]) &&
//                lar_searchPath.length > (depth + 1) &&
//                lar_childPath[depth + 1].equals(lar_searchPath[depth + 1])) {
//                return searchNode(lob_childNode, iva_path, ++depth);
//            }
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

    private Collection<NodeInterface> getAllFiles(NodeInterface iob_node, Collection<NodeInterface> ico_files) {
        if (!iob_node.isDirectory()) {
            ico_files.add(iob_node);
            return ico_files;
        }

        for (NodeInterface lob_child : iob_node.getChildren()) {
            ico_files = getAllFiles(lob_child, ico_files);
        }

        return ico_files;
    }

    private Collection<NodeInterface> getAllDirectories(NodeInterface iob_node, Collection<NodeInterface> ico_directories) {
        if (iob_node.isDirectory() && iob_node != this.gob_root) {
            ico_directories.add(iob_node);
        }

        for (NodeInterface lob_child : iob_node.getChildren()) {
            ico_directories = getAllDirectories(lob_child, ico_directories);
        }

        return ico_directories;
    }

    private Collection<NodeInterface> getAll(NodeInterface iob_node, Collection<NodeInterface> ico_nodes) {
        if (iob_node != this.gob_root) {
            ico_nodes.add(iob_node);
        }

        for (NodeInterface lob_child : iob_node.getChildren()) {
            ico_nodes = getAll(lob_child, ico_nodes);
        }

        return ico_nodes;
    }
}
