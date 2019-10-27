package exceptions;

public class DuplicateDocumentException extends ProtocolException {
    public DuplicateDocumentException() {

    }
    public DuplicateDocumentException(String message) {
        super(message);
    }
}
