package tools;

import fileTree.models.TreeSingleton;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    public TreeItem<String> getTreeItem(File iob_file) {
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

    public void removeFromTreeView(File iob_file) {
        TreeItem<String> lob_item = searchTreeItem(iob_file);
        if (lob_item == null) {
            return;
        }
        TreeItem<String> lob_parent = lob_item.getParent();
        lob_parent.getChildren().remove(lob_item);
    }

    private TreeItem<String> searchTreeItem(File iob_file) {
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

    public String getRelativePath(String iva_filePath) throws IOException {
        String lva_regex = TreeSingleton.getInstance().getTree().getRoot().getCanonicalPath();
        lva_regex = lva_regex.replaceAll("\\\\", "\\\\\\\\");
        return iva_filePath.replaceFirst(lva_regex + "\\\\", "");
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

    private String[] removeBasePathAndConvertToArray(String iva_filePath) throws IOException {
        String lva_basePath = TreeSingleton.getInstance().getTree().getRoot().getCanonicalPath();
        lva_basePath = lva_basePath.replaceAll("\\\\", "\\\\\\\\");
        iva_filePath = iva_filePath.replaceFirst(lva_basePath, "");
        iva_filePath = iva_filePath.replaceFirst("\\\\", "");
        return iva_filePath.split("\\\\");
    }
}
