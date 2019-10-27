package exceptions;

public class DocumentSectionLockedException extends ProtocolException {
    public DocumentSectionLockedException() {

    }
    public DocumentSectionLockedException(String message) {
        super(message);
    }
}
