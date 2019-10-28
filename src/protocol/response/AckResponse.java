package protocol.response;

import client.Client;
import protocol.request.Request;

public class AckResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final Request reqAck;

    public AckResponse(Request reqAck) {
        this.reqAck = reqAck;
    }

    public boolean valid(Request req) {
        return req == reqAck;
    }

    @Override
    public String toString() {
        return "Operazione \"" + this.reqAck.toString() + "\" eseguita con successo.";
    }

    @Override
    public void process(Client client) {
        System.out.println(this.toString());
    }
}
