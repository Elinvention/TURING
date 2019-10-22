package protocol.response;

import protocol.request.Request;

public class AckResponse extends Response {

    private final Request reqAck;

    public AckResponse(Request reqAck) {
        this.reqAck = reqAck;
    }

    public boolean valid(Request req) {
        return req == reqAck;
    }

    @Override
    public String toString() {
        return "Acknowledgement for request " + reqAck.toString();
    }
}
