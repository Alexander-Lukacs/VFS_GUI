package exceptions;

public class NodeNotFoundException extends RuntimeException {

    public NodeNotFoundException(String iva_message) {
        super(iva_message);
    }
}
