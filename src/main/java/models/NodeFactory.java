package models;

import interfaces.NodeInterface;

public class NodeFactory {
    public static NodeInterface createFileNode(String iva_name, String iva_path, long lva_size) {
        return new Node(iva_name, iva_path, false, lva_size);
    }

    public static NodeInterface createDirectoryNode(String iva_name, String iva_path) {
        return new Node(iva_name, iva_path, true, 0);
    }
}
