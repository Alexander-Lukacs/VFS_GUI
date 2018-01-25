package models;

import exceptions.*;
import interfaces.NodeInterface;

import java.util.ArrayList;
import java.util.Collection;

public class Node implements NodeInterface{

    private static final String GC_INVALID_PATH = "Path can not be empty";
    private static final String GC_EMPTY = "";
    private static final String GC_NODE_NOT_FOUND = "Node not found: ";
    private static final String GC_INVALID_FILE_SIZE = "The file size can not be negative";
    private static final String GC_CANT_ADD_CHILD_TO_FILE = "a normal file can not have children";

    private String gva_path;
    private boolean gva_isDirectory;
    private Collection<NodeInterface> gco_children;
    private boolean gva_nodeNotFoundExceptionStatus;
    private NodeInterface gob_parent;
    private long gva_size;
    private String gva_name;

    public Node(String iva_name, String iva_path, boolean iva_isDirectory, long iva_size) {

        if (iva_path.trim().equals(GC_EMPTY)) {
            throw new InvalidPathException(GC_INVALID_PATH);
        }

        if (iva_size < 0) {
            throw new InvalidSizeException(GC_INVALID_FILE_SIZE);
        }
        this.gva_path = iva_path;
        this.gva_isDirectory = iva_isDirectory;
        this.gco_children = new ArrayList<>();
        this.gva_nodeNotFoundExceptionStatus = true;
        this.gob_parent = null;
        this.gva_name = iva_name;
        this.gva_size = iva_size;
    }

    public Node(String iva_name, String iva_path, boolean iva_isDirectory) {
        this(iva_name, iva_path, iva_isDirectory, 0);
    }

    public Node(String iva_name, String iva_path) {
        this(iva_name, iva_path, false, 0);
    }

    /**
     * @return if the node is a directory it return true, otherwise false
     */
    @Override
    public boolean isDirectory() {
        return gva_isDirectory;
    }

    /**
     * Set the name of a node
     * @param iva_name set this as name of the node
     */
    @Override
    public void setName(String iva_name) {
        this.gva_name = iva_name;
    }

    /**
     * @return the name of the node
     */
    @Override
    public String getName() {
        return this.gva_name;
    }

    @Override
    public void setDirectory(boolean iva_isDirectory) {
        this.gva_isDirectory = iva_isDirectory;
    }

    @Override
    public void setPath(String iva_path) throws InvalidNodeKeyException{
        if (iva_path.trim().equals(GC_EMPTY)) {
            throw new InvalidPathException(GC_INVALID_PATH);
        }

        this.gva_path = iva_path;
    }

    @Override
    public String getPath() {
        return this.gva_path;
    }

    @Override
    public void addChild(NodeInterface iob_node) {
        if (this.gva_isDirectory) {
            this.gco_children.add(iob_node);
        } else {
            throw new NodeException(GC_CANT_ADD_CHILD_TO_FILE);
        }
    }

    @Override
    public void addChildren(Collection<NodeInterface> ico_nodeCollection) {
        if (this.gva_isDirectory) {
            this.gco_children.addAll(ico_nodeCollection);
        } else {
            throw new NodeException(GC_CANT_ADD_CHILD_TO_FILE);
        }
    }

    @Override
    public NodeInterface getChild(String iva_path) throws NodeNotFoundException {
        //---------------------------Variables-----------------------------------
        NodeInterface lob_searchedNode = searchChildNode(iva_path);
        //-----------------------------------------------------------------------
        
        if (lob_searchedNode == null && gva_nodeNotFoundExceptionStatus) {
            throw new NodeNotFoundException(GC_NODE_NOT_FOUND + iva_path);
        }

        return lob_searchedNode;
    }

    @Override
    public Collection<NodeInterface> getChildren() {
        return this.gco_children;
    }

    @Override
    public void removeChild(NodeInterface iob_node) throws NodeNotFoundException {
        //---------------------------Variables------------------------------------
        boolean lva_isNodeAvailable;
        lva_isNodeAvailable = gco_children.remove(iob_node);
        //------------------------------------------------------------------------

        if (!lva_isNodeAvailable && gva_nodeNotFoundExceptionStatus) {
            throw new NodeNotFoundException(GC_NODE_NOT_FOUND + iob_node.getPath());
        }
    }

    @Override
    public void removeChild(String iva_path) throws NodeNotFoundException {
        //---------------------------Variables-----------------------------
        boolean lva_isNodeAvailable;
        //-----------------------------------------------------------------

        lva_isNodeAvailable = gco_children.removeIf(lob_node ->
            lob_node.getPath().equals(iva_path)
        );

        if (!lva_isNodeAvailable && gva_nodeNotFoundExceptionStatus) {
            throw new NodeNotFoundException(GC_NODE_NOT_FOUND + iva_path);
        }
    }

    @Override
    public void removeChildren(Collection<String> ico_nodePaths) throws NodeNotFoundException {
        //---------------------------Variables-------------------------------------------------
        StringBuilder lob_nodesNotFound = new StringBuilder();
        boolean isNodeRemoved;
        //-------------------------------------------------------------------------------------

        for (String lva_path : ico_nodePaths) {
            isNodeRemoved = gco_children.removeIf(lob_node ->
                lob_node.getPath().equals(lva_path)
            );

            if (!isNodeRemoved) {
                lob_nodesNotFound.append(lva_path).append(", ");
            }
        }

        if (lob_nodesNotFound.length() != 0 && gva_nodeNotFoundExceptionStatus) {
            throw new NodeNotFoundException(GC_NODE_NOT_FOUND + lob_nodesNotFound.toString());
        }
    }

    @Override
    public void removeAllChildren() {
        this.gco_children.clear();
    }

    @Override
    public void setParent(NodeInterface iob_parent) {
        this.gob_parent = iob_parent;
    }

    @Override
    public NodeInterface getParent() {
        return this.gob_parent;
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
    public void setSize(long iva_size) {
        this.gva_size = iva_size;
    }

    @Override
    public long getSize() {
        return this.gva_size;
    }

    @Override
    public long calculateSize() {
        return 0;
    }
    
    private NodeInterface searchChildNode(String iva_path) {
        for (NodeInterface lob_collectionNode : this.gco_children) {
            if (lob_collectionNode.getPath().equals(iva_path)) {
                return lob_collectionNode;
            }
        }

        return null;
    }

    private NodeInterface searchChildNode(NodeInterface iob_node) {
        for (NodeInterface lob_collectionNode : this.gco_children) {
            if (lob_collectionNode == iob_node) {
                return lob_collectionNode;
            }
        }

        return null;
    }

}