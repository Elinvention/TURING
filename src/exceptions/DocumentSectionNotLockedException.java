package exceptions;

public class DocumentSectionNotLockedException extends ProtocolException {
    public DocumentSectionNotLockedException() {

    }
    public DocumentSectionNotLockedException(String message) {
        super(message);
    }
}
