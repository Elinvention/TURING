package exceptions;

public class DuplicateUsernameException extends ProtocolException {
    public DuplicateUsernameException() {

    }
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
