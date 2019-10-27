package protocol.response;

public class ExceptionResponse extends Response {
    public Exception ex;

    public ExceptionResponse(Exception ex) {
        this.ex = ex;
    }

    @Override
    public String toString() {
        return "Server error: " + ex.toString();
    }
}
