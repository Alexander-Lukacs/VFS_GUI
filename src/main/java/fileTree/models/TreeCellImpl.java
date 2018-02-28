package fileTree.models;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;

public class TreeCellImpl extends TreeCell<String> {
    private TextField textField;

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
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
        textField = new TextField(getString());
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
