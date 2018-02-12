package tools;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static models.constants.TreeControlConstants.*;
import static models.constants.TreeControlConstants.GC_WORD_FILE;
import static models.constants.TreeControlConstants.GC_XML_FILE;

public class TreeTool {
    private static TreeTool ourInstance = new TreeTool();

    public static TreeTool getInstance() {
        return ourInstance;
    }

    private TreeTool() {
    }

    public TreeItem<String> addTreeItem(TreeItem<String> iob_parent, File iob_file) {
        String lva_fileType;
        TreeItem<String> rob_child = new TreeItem<>(iob_file.getName());
        if (iob_file.isDirectory()) {
            rob_child.setGraphic(getTreeIcon(iob_file.getAbsolutePath()));
        } else {
            lva_fileType = iob_file.getName().replaceFirst(".*\\.","");
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
}
