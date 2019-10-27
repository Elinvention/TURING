package exceptions;

public class InvalidUsernameException extends ProtocolException {
    public InvalidUsernameException() {

    }
    public InvalidUsernameException(String message) {
        super(message);
    }

}
