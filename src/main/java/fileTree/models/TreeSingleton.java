package fileTree.models;

import fileTree.interfaces.Tree;

import java.io.IOException;

public class TreeSingleton {
    private static TreeSingleton gob_instance;
    private Tree gob_tree;
    private static String gva_treeRoot;

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

    private TreeSingleton() throws IOException{
        gob_tree = new TreeImpl(gva_treeRoot);
    }
}
