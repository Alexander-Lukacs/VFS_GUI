package fileTree.classes;

import fileTree.interfaces.Tree;
import javafx.scene.control.TreeView;

import java.io.IOException;

public class TreeSingleton {
    private static TreeSingleton gob_instance;
    private Tree gob_tree;
    private static String gva_treeRoot;
    private TreeView<String> gob_treeView;
    private PreventDuplicateOperation gob_preventOperationDuplicates;

    public static TreeSingleton getInstance() {
        if (gob_instance == null) {
            try {
                gob_instance = new TreeSingleton();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return gob_instance;
    }

    public static void setTreeRootPath(String iva_rootPath) {
        gva_treeRoot = iva_rootPath;
    }

    public Tree getTree() {
        return this.gob_tree;
    }

    public TreeView<String> getTreeView() {
        return this.gob_treeView;
    }

    public PreventDuplicateOperation getDuplicateOperationsPrevention() {
        return this.gob_preventOperationDuplicates;
    }

    private TreeSingleton() throws IOException{
        gob_tree = new TreeImpl(gva_treeRoot);
        gob_treeView = new TreeView<>();
        gob_preventOperationDuplicates = new PreventDuplicateOperation();
    }
}
