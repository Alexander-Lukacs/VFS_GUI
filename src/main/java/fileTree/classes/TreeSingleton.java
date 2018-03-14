package fileTree.classes;

import javafx.scene.control.TreeView;

public class TreeSingleton {
    private static TreeSingleton gob_instance;
    private TreeView<String> gob_treeView;

    public static TreeSingleton getInstance() {
        if (gob_instance == null) {
                gob_instance = new TreeSingleton();
        }
        return gob_instance;
    }

    public TreeView<String> getTreeView() {
        return this.gob_treeView;
    }

    public void reset() {
        gob_treeView = null;
        gob_instance = null;
    }

    private TreeSingleton(){
        gob_treeView = new TreeView<>();
    }
}
