package exceptions;

public class InvalidPasswordException extends ProtocolException {
    public InvalidPasswordException() {

    }
    public InvalidPasswordException(String message) {
        super(message);
    }
}
