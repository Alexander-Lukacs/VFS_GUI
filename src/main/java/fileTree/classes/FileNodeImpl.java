package fileTree.classes;

import fileTree.interfaces.FileNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class FileNodeImpl implements FileNode {
    private File gob_file;
    private FileNode gob_parentNode = null;
    private Collection<FileNode> gco_children;

    public FileNodeImpl(File iob_file) {
        this.gco_children = new ArrayList<>();
        setFile(iob_file);
    }

    /**
     * @param iob_child add this file as child to this node
     */
    @Override
    public void addChild(FileNode iob_child) {
        this.gco_children.add(iob_child);
    }

    /**
     * @return all children that this file has
     */
    @Override
    public Collection<FileNode> getChildren() {
        return this.gco_children;
    }

    /**
     * return a specific child
     *
     * @param iob_file the file to search for
     * @return the found child, otherwise null
     */
    @Override
    public FileNode getChild(File iob_file) {
        for (FileNode lob_node : this.gco_children) {
            if (lob_node.getFile().equals(iob_file)) {
                return lob_node;
            }
        }
        return null;
    }

    /**
     * @param iob_child remove the node with this file
     */
    @Override
    public void removeChild(File iob_child) {
        this.gco_children.removeIf(lob_child -> (iob_child.equals(lob_child.getFile())));
    }

    /**
     * remove all children of this node
     */
    @Override
    public void removeAllChildren() {
        this.gco_children.clear();
    }

    /**
     * @return this nodes file
     */
    @Override
    public File getFile() {
        return this.gob_file;
    }

    /**
     * Set the file of this node
     *
     * @param iob_file set this as file
     */
    @Override
    public void setFile(File iob_file) {
        this.gob_file = iob_file;
    }

    /**
     * @param iob_parent set this node as parent
     */
    @Override
    public void setParent(FileNode iob_parent) {
        this.gob_parentNode = iob_parent;
    }

    /**
     * @return this nodes parent
     */
    @Override
    public FileNode getParent() {
        return this.gob_parentNode;
    }
}

