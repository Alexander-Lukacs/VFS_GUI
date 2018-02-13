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
