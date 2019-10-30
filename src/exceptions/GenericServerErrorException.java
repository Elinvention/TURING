package exceptions;

public class GenericServerErrorException extends ProtocolException {
    public GenericServerErrorException() {
        super();
    }

    public GenericServerErrorException(String message) {
        super(message);
    }
}
