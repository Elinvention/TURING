package exceptions;

public class DocumentSectionNotFoundException extends ProtocolException {
    public DocumentSectionNotFoundException() {

    }
    public DocumentSectionNotFoundException(String message) {
        super(message);
    }
}
