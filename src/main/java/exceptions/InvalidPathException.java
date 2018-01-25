package exceptions;

public class InvalidPathException extends RuntimeException{

    public InvalidPathException(String iva_message) {
        super(iva_message);
    }
}
