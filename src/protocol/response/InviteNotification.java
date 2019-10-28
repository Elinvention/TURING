package protocol.response;

import client.Client;
import protocol.DocumentUri;

public class InviteNotification extends Response {
    private static final long serialVersionUID = 1L;

    private final String owner;
    private final String docName;

    public InviteNotification(String owner, String docName) {
        this.owner = owner;
        this.docName = docName;
    }

    @Override
    public String toString() {
        return "Hai ricevuto un invito di collaborazione sul documento " + new DocumentUri(owner, docName);
    }

    @Override
    public void process(Client client) {
        System.out.println(this.toString());
    }
}
