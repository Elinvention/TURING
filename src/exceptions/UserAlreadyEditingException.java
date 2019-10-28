package exceptions;

public class UserAlreadyEditingException extends ProtocolException {
    public UserAlreadyEditingException() {

    }

    public UserAlreadyEditingException(String message) {
        super(message);
    }
}
