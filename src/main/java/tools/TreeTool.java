package tools;

import cache.DirectoryCache;
import fileTree.interfaces.Tree;
import fileTree.classes.TreeSingleton;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tools.xmlTools.DirectoryNameMapper;

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

    @SuppressWarnings("WeakerAccess")
    public static void addTreeItem(TreeItem<String> iob_parent, File iob_file) {
        TreeItem<String> rob_child = new TreeItem<>(iob_file.getName());
        if (iob_file.exists()) {
            rob_child.setGraphic(getTreeIcon(iob_file.getAbsolutePath()));
        }

        iob_parent.getChildren().add(rob_child);
    }

    public static TreeItem<String> getTreeItem(File iob_file) {
        return searchTreeItem(iob_file);
    }

    public static Node getTreeIcon(String iva_iconName) {
        javax.swing.Icon icon = FileSystemView.getFileSystemView().getSystemIcon(new File(iva_iconName));
        try {
            BufferedImage bufferedImage = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);


            Image fxImage = SwingFXUtils.toFXImage(
                    bufferedImage, null
            );
            return new ImageView(fxImage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ImageView(new Image(TreeTool.class.getClassLoader().getResourceAsStream("images/fileIcons/ICON_FILE.png")));
    }

    public static void createDirectory(File iob_directory) {
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
        TreeItem<String> item;
        try {
            item = TreeSingleton.getInstance().getTreeView().getRoot();
            if (iob_file.equals(DirectoryCache.getDirectoryCache().getUserDirectory())) {
                return item;
            }

            boolean lva_childFound;
            String[] test = Utils.buildRelativeFilePath(iob_file).split("\\\\");

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
        } catch (NullPointerException ex) {
            System.out.println();
            return null;
        }
        return item;
    }

    public static boolean isFileInTreeView(File iob_file) {
        return searchTreeItem(iob_file) != null;
    }

    public static boolean addToTreeView(File iob_file) {
        TreeItem<String> lob_parent;
        try {
            if (!iob_file.exists()) {
                return true;
            }

            if (TreeSingleton.getInstance().getTreeView() == null) {
                return false;
            }

            lob_parent = searchTreeItem(iob_file.getParentFile());

            if (lob_parent == null) {
                return false;
            }

            addTreeItem(lob_parent, iob_file);

        } catch (Exception ex) {
            System.out.println();
        }
        return true;
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
    public void deleteItem(File iob_file) {
        TreeItem<String> lob_item = searchTreeItem(iob_file);
        if (lob_item != null) {
            lob_item.getParent().getChildren().remove(lob_item);
        }
    }
    /**
     * filter all new files on the same level as public, shared and private
     * @return return true if the file is on the same level, otherwise false
     */
    public static boolean filterRootFiles(Path iob_path) {
        Path lob_rootPath = DirectoryCache.getDirectoryCache().getUserDirectory().toPath();
        Path lob_privatePath = new File(lob_rootPath.toString() + "\\" + DirectoryNameMapper.getPrivateDirectoryName()).toPath();
        Path lob_publicPath = new File(lob_rootPath.toString() + "\\" + DirectoryNameMapper.getPublicDirectoryName()).toPath();
        Path lob_sharedPath = new File(lob_rootPath.toString() + "\\" + DirectoryNameMapper.getSharedDirectoryName()).toPath();

        return !iob_path.startsWith(lob_privatePath) && !iob_path.startsWith(lob_publicPath) && !iob_path.startsWith(lob_sharedPath);

    }

    public static boolean isSharedDirectory(Path iob_path) {
        Path lob_rootPath = TreeSingleton.getInstance().getTree().getRoot().toPath();
        Path lob_sharedPath = new File(lob_rootPath.toString() + "\\" + DirectoryNameMapper.getSharedDirectoryName()).toPath();

        return iob_path.startsWith(lob_sharedPath);
    }

    public static boolean isRootFile(File iob_file) {
        File lob_rootFile = TreeSingleton.getInstance().getTree().getRoot();
        File lob_privateFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getPrivateDirectoryName());
        File lob_publicFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getPublicDirectoryName());
        File lob_sharedFile = new File(lob_rootFile.toString() + "\\" + DirectoryNameMapper.getSharedDirectoryName());

        return lob_privateFile.equals(iob_file) || lob_publicFile.equals(iob_file) || lob_sharedFile.equals(iob_file);
    }

//    private String[] removeBasePathAndConvertToArray(String iva_filePath) throws IOException {
//        String lva_basePath = TreeSingleton.getInstance().getTree().getRoot().getCanonicalPath();
//        lva_basePath = lva_basePath.replaceAll("\\\\", "\\\\\\\\");
//        iva_filePath = iva_filePath.replaceFirst(lva_basePath, "");
//        iva_filePath = iva_filePath.replaceFirst("\\\\", "");
//        return iva_filePath.split("\\\\");
//    }
}
