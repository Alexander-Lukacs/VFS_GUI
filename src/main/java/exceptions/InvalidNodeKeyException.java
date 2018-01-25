package exceptions;

public class InvalidNodeKeyException extends RuntimeException{

    public InvalidNodeKeyException(String iva_message) {
        super(iva_message);
    }
}
