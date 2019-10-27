package exceptions;

public class NotAllowedException extends ProtocolException {
    public NotAllowedException() {}
    public NotAllowedException(String message) {
        super(message);
    }
}
