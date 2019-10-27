package exceptions;

public class InvalidRequestException extends ProtocolException {
    public InvalidRequestException() {

    }
    public InvalidRequestException(String message) {
        super(message);
    }
}
