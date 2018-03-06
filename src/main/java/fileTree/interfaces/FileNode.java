package fileTree.interfaces;

import java.io.File;
import java.util.Collection;

public interface FileNode {

    /**
     * @param iob_child add this file as child to this node
     */
    void addChild(FileNode iob_child);

    /**
     * @return all children that this file has
     */
    Collection<FileNode> getChildren();

    /**
     * return a specific child
     * @param iob_file the file to search for
     * @return the found child, otherwise null
     */
    FileNode getChild(File iob_file);

    /**
     * @param iob_child remove the node with this file
     */
    void removeChild(File iob_child);

    /**
     * remove all children of this node
     */
    void removeAllChildren();

    /**
     * @return this nodes file
     */
    File getFile();

    /**
     * Set the file of this node
     * @param iob_file set this as file
     */
    void setFile(File iob_file);

    /**
     * @param iob_parent set this node as parent
     */
    void setParent(FileNode iob_parent);

    /**
     * @return this nodes parent
     */
    FileNode getParent();
}

