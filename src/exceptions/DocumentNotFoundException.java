package exceptions;

public class DocumentNotFoundException extends ProtocolException {
    public DocumentNotFoundException() {

    }

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
