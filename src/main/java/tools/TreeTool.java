package tools;

import fileTree.interfaces.Tree;
import fileTree.models.TreeSingleton;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import restful.clients.FileRestClient;

import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class TreeTool {
    private static TreeTool ourInstance = new TreeTool();

    public static TreeTool getInstance() {
        return ourInstance;
    }

    private TreeTool() {
    }

    public TreeItem<String> addTreeItem(TreeItem<String> iob_parent, File iob_file) {
        TreeItem<String> rob_child = new TreeItem<>(iob_file.getName());
        if (iob_file.isDirectory()) {
            rob_child.setGraphic(getTreeIcon(iob_file.getAbsolutePath()));
        } else {
            rob_child.setGraphic(getTreeIcon(iob_file.getAbsolutePath()));
        }

        iob_parent.getChildren().add(rob_child);
        return rob_child;
    }

    public static TreeItem<String> getTreeItem(File iob_file) {
        return searchTreeItem(iob_file);
    }

    public Node getTreeIcon(String iva_iconName) {
        javax.swing.Icon icon = FileSystemView.getFileSystemView().getSystemIcon(new File(iva_iconName));

        BufferedImage bufferedImage = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);

        try {
            Image fxImage = SwingFXUtils.toFXImage(
                    bufferedImage, null
            );
            return new ImageView(fxImage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_FILE.png")));
    }

    public static void removeFromTreeView(File iob_file) {
        TreeItem<String> lob_item = searchTreeItem(iob_file);
        if (lob_item == null) {
            return;
        }
        TreeItem<String> lob_parent = lob_item.getParent();
        lob_parent.getChildren().remove(lob_item);
    }

    public void createDirectory(File iob_directory) {
        int lva_tries;
        boolean lva_directoryCreated;
        if (!iob_directory.exists() || !iob_directory.isDirectory()) {
            lva_tries = 0;
            do {
                lva_directoryCreated = iob_directory.mkdir();
                lva_tries++;
            } while(lva_tries < 10 && !lva_directoryCreated);

            if (!lva_directoryCreated) {
                new AlertWindows().createErrorAlert("There was a problem to create the directory \"" +
                        iob_directory.getName() + "\" under:\n" + iob_directory.getAbsolutePath());
                System.exit(1);
            }
        }
    }

    private static TreeItem<String> searchTreeItem(File iob_file) {
        TreeItem<String> item = TreeSingleton.getInstance().getTreeView().getRoot();
        if (iob_file.equals(TreeSingleton.getInstance().getTree().getRoot())) {
            return item;
        }
        try {
            boolean lva_childFound;
            String[] test = getRelativePath(iob_file.getCanonicalPath()).split("\\\\");
            int counter = 0;


            while (counter < test.length) {
                lva_childFound = false;
                for (TreeItem<String> lob_child : item.getChildren()) {
                    if (lob_child.getValue().equals(test[counter])) {
                        item = lob_child;
                        lva_childFound = true;
                        break;
                    }
                }
                if (!lva_childFound) {
                    return null;
                }
                counter++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return item;
    }

    public static boolean isFileInTreeView(File iob_file) {
        return searchTreeItem(iob_file) != null;
    }

    public static String getRelativePath(String iva_filePath) throws IOException {
        String lva_regex = TreeSingleton.getInstance().getTree().getRoot().getCanonicalPath();

        lva_regex = lva_regex.replaceAll("\\\\", "\\\\\\\\");
        String rva_return = iva_filePath.replaceFirst(lva_regex, "");
        return rva_return.replaceFirst("^\\\\", "");
    }

    public void addToTreeView(File iob_file) {
        try {
            TreeItem<String> lob_pointer = TreeSingleton.getInstance().getTreeView().getRoot();
            String[] lar_path = removeBasePathAndConvertToArray(iob_file.getCanonicalPath());
            int depth = 0;

            while (lar_path.length > depth) {
                for (TreeItem<String> lob_item : lob_pointer.getChildren()) {

                    if (lob_item.getValue().equals(lar_path[depth])) {
                        lob_pointer = lob_item;
                        break;
                    }
                }
                depth++;
            }
            addTreeItem(lob_pointer, iob_file);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static File buildFileFromItem(TreeItem iob_treeItem, Tree iob_tree) {
        StringBuilder lob_path = new StringBuilder();

        while (iob_treeItem != null) {
            lob_path.insert(0, iob_treeItem.getValue());
            lob_path.insert(0, "\\");
            iob_treeItem = iob_treeItem.getParent();
        }
        lob_path.insert(0, iob_tree.getRoot().getParent());
        return iob_tree.getFile(lob_path.toString());
    }

    public static boolean createFileOrDirectory(File iob_newFile, boolean isDirectory, FileRestClient iob_restClient) {
        //-------------------------------Variables----------------------------
        String lva_relativeFilePath;
        //--------------------------------------------------------------------
        try {

            lva_relativeFilePath = getRelativePath(iob_newFile.getCanonicalPath());

            TreeSingleton.getInstance().getTree().addFile(iob_newFile, isDirectory);
            TreeTool.getInstance().addToTreeView(iob_newFile);

            if (isDirectory) {
                iob_restClient.createDirectoryOnServer(lva_relativeFilePath);
            } else {
                iob_restClient.uploadFilesToServer(iob_newFile, lva_relativeFilePath);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public void deleteItem(File iob_file) {
        TreeItem<String> lob_item = searchTreeItem(iob_file);
        if (lob_item != null) {
            lob_item.getParent().getChildren().remove(lob_item);
        }
    }

    /**
     * delete a file on the client, explorer and server
     *
     * @param iob_file         file to delete
     * @param iob_itemToDelete item to delete in the tree
     * @return true if all files were deleted, otherwise false
     */
    public static boolean deleteFile(File iob_file, TreeItem<String> iob_itemToDelete, FileRestClient iob_restClient) {
        //-------------------------------Variables----------------------------------------
        String lva_relativePath;
        //--------------------------------------------------------------------------------
        try {
            lva_relativePath = getRelativePath(iob_file.getCanonicalPath());
            iob_restClient.deleteOnServer(lva_relativePath);

            iob_itemToDelete.getParent().getChildren().remove(iob_itemToDelete);
            return TreeSingleton.getInstance().getTree().deleteFile(iob_file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    /**
     * filter all new files on the same level as public, shared and private
     * @return return true if the file is on the same level, otherwise false
     */
    public static boolean filterRootFiles(Path iob_path) {
        if (iob_path.endsWith("Private")) {
            return false;
        }

        if (iob_path.endsWith("Public")) {
            return false;
        }

        if (iob_path.endsWith("Shared")) {
            return false;
        }

        File root_file = TreeSingleton.getInstance().getTree().getRoot();
        return root_file.equals(iob_path.getParent().toFile());
    }

    public static void moveFile(Path iob_path, Path iob_destination, boolean iva_moveJustInTree, FileRestClient iob_restClient) {
        //-------------------------Variables------------------------------------
        TreeItem<String> lob_item = getTreeItem(iob_path.toFile());
        TreeItem<String> lob_parent = getTreeItem(iob_destination.toFile());
        String lva_destination = iob_destination.toString();
        String lva_oldRelativePath;
        String lva_newRelativePath;
        //----------------------------------------------------------------------
        try {
            if (!TreeSingleton.getInstance().getTree().moveFile(iob_path.toFile(), lva_destination, iva_moveJustInTree)) {
                new AlertWindows().createErrorAlert("Fiie could not be moved. There is a File with the same name");
                return;
            }

            removeFromTreeView(iob_path.toFile());
            lob_parent.getChildren().add(lob_item);

            lva_oldRelativePath = getRelativePath(iob_path.toString());
            lva_newRelativePath = getRelativePath(lva_destination);
            iob_restClient.moveFile(lva_oldRelativePath, lva_newRelativePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String[] removeBasePathAndConvertToArray(String iva_filePath) throws IOException {
        String lva_basePath = TreeSingleton.getInstance().getTree().getRoot().getCanonicalPath();
        lva_basePath = lva_basePath.replaceAll("\\\\", "\\\\\\\\");
        iva_filePath = iva_filePath.replaceFirst(lva_basePath, "");
        iva_filePath = iva_filePath.replaceFirst("\\\\", "");
        return iva_filePath.split("\\\\");
    }
}
