package models.classes;

import fileTree.classes.TreeSingleton;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import threads.constants.FileManagerConstants;
import threads.classes.ThreadManager;
import tools.AlertWindows;
import tools.TreeTool;
import tools.xmlTools.DirectoryNameMapper;

import java.io.File;

import static tools.TreeTool.buildFileFromItem;

@SuppressWarnings("WeakerAccess")
public class TreeCellImpl extends TreeCell<String> {
    private TextField gob_textField;

    public TreeCellImpl() {
        setEvents();
    }

    @Override
    public void startEdit() {
        TreeItem<String> lob_item = getTreeItem();
        File lob_file = TreeTool.buildFileFromItem(lob_item);

        if (!lob_item.getValue().equals("Shared")) {
            if (lob_file.exists()) {
                super.startEdit();

                if (gob_textField == null) {
                    createTextField();
                }
                setText(null);
                setGraphic(gob_textField);
                gob_textField.selectAll();
            }
        }

    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setGraphic(getTreeItem().getGraphic());
    }

    @Override
    protected void updateItem(String iva_item, boolean iva_empty) {
        super.updateItem(iva_item, iva_empty);

        if (!iva_empty) {
            setGraphic(getTreeItem().getGraphic());
            setText(getItem() == null ? "" : getItem());
        } else {
            setText(null);
            setGraphic(null);
        }
    }

    private void createTextField() {
        gob_textField = new TextField(getString());
        gob_textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                int lva_counter = 0;
                String lva_newValue = gob_textField.getText();
                for (TreeItem<String> lob_sibling : getTreeItem().getParent().getChildren()) {
                    if (lob_sibling.getValue().equals(lva_newValue)) {
                        lva_counter++;
                    }
                }

                if (lva_counter >= 1) {
                    new AlertWindows().createWarningAlert("File with the same name already exists");
                    cancelEdit();
                    return;
                }

                commitEdit(gob_textField.getText());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }

    private void setEvents() {
        this.setOnDragDetected(event -> {
            TreeItem<String> lob_selectedItem = this.getTreeItem();
            Dragboard lob_dragBoard;
            ClipboardContent lob_content;

            if (lob_selectedItem != null && !isRootChildElement(lob_selectedItem) &&
                    !lob_selectedItem.getParent().getValue().equals("Shared")) {

                lob_dragBoard = this.startDragAndDrop(TransferMode.MOVE);
                lob_content = new ClipboardContent();
                lob_content.putString(lob_selectedItem.getValue());
                lob_dragBoard.setContent(lob_content);
            }
            event.consume();
        });

        this.setOnDragDropped(event -> {
            TreeSingleton lob_treeSingleton = TreeSingleton.getInstance();

            TreeItem<String> lob_treeItemHovered;
            TreeCell lob_cellDragged;
            TreeItem lob_treeItemDragged;
            File lob_fileHovered;
            File lob_fileDragged;

            lob_treeItemHovered = this.getTreeItem();
            lob_cellDragged = (TreeCell) event.getGestureSource();
            lob_treeItemDragged = lob_cellDragged.getTreeItem();

            lob_fileHovered = TreeTool.buildFileFromItem(lob_treeItemHovered);
            lob_fileDragged = TreeTool.buildFileFromItem(lob_treeItemDragged);

            ThreadManager.addCommandToFileManager(lob_fileDragged, FileManagerConstants.GC_MOVE, true, lob_fileHovered, false);
            PreventDuplicateOperation.getDuplicateOperationPrevention().putMoved(lob_fileDragged.toPath());

            lob_treeSingleton.getTreeView().getSelectionModel().select(lob_treeItemHovered);

            event.setDropCompleted(true);
            event.consume();
        });


        this.setOnDragEntered(event -> {
            TreeItem<String> lob_selectedItem = this.getTreeItem();
            Object lva_cellDraggedValue;
            Object lva_cellHoveredValue;
            TreeCell lob_cellDragged;

            if (lob_selectedItem != null && !buildFileFromItem(this.getTreeItem()).isFile() &&
                    event.getGestureSource() != this) {

                lob_cellDragged = (TreeCell) event.getGestureSource();
                lva_cellDraggedValue = lob_cellDragged.getTreeItem().getParent().getValue();
                lva_cellHoveredValue = this.getTreeItem().getValue();

                if (lva_cellDraggedValue != lva_cellHoveredValue) {
                    this.setStyle("-fx-background-color: powderblue;");
                }
            }

            event.consume();
        });

        this.setOnDragOver(event -> {
            TreeItem<String> lob_selectedItem = this.getTreeItem();
            Object lva_cellDraggedValue;
            Object lva_cellHoveredValue;
            TreeCell lob_cellDragged;

            if (lob_selectedItem != null && event.getGestureSource() != this &&
                    !buildFileFromItem(this.getTreeItem()).isFile() && !lob_selectedItem.getValue().equals("Shared")) {

                lob_cellDragged = (TreeCell) event.getGestureSource();
                lva_cellDraggedValue = lob_cellDragged.getTreeItem().getParent().getValue();
                lva_cellHoveredValue = this.getTreeItem().getValue();

                if (lva_cellDraggedValue != lva_cellHoveredValue) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }

            event.consume();
        });

        this.setOnDragExited(event -> {
            this.setStyle("-fx-background-color: white");
            this.setStyle("-fx-focus-color: black");

            event.consume();
        });
    }

    private boolean isRootChildElement(TreeItem iob_selectedItem) {
        return iob_selectedItem.getValue().equals(DirectoryNameMapper.getPrivateDirectoryName()) ||
                iob_selectedItem.getValue().equals(DirectoryNameMapper.getPublicDirectoryName()) ||
                iob_selectedItem.getValue().equals("Shared");
    }
}
