package tools;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

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
            rob_child.setGraphic(getTreeIcon(GC_DIRECTORY_ICON));
        } else {
            lva_fileType = iob_file.getName().replaceFirst(".*\\.","");
            rob_child.setGraphic(getTreeIcon(lva_fileType));
        }

        iob_parent.getChildren().add(rob_child);
        return rob_child;
    }

    public Node getTreeIcon(String iva_iconName) {
        ImageView rob_imageView;
        switch (iva_iconName) {
            case GC_DIRECTORY_ICON:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_DIR.png")));
                break;
            case GC_TEXT_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_TXT.png")));
                break;
            case GC_EXCEL_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_EXCEL.png")));
                break;
            case GC_PDF_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_PDF.png")));
                break;
            case GC_WORD_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_WORD.png")));
                break;
            case GC_XML_FILE:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_XML.png")));
                break;
            default:
                rob_imageView = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("images/fileIcons/ICON_FILE.png")));
        }

        return rob_imageView;
    }
}
