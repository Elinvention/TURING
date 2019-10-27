package exceptions;

public class InvalidSessionException extends ProtocolException {
    public InvalidSessionException() {

    }

    public InvalidSessionException(String message) {
        super(message);
    }
}
