package protocol.response;

import client.Client;

public class EndEditResponse extends Response {
    private static final long serialVersionUID = 1L;

    @Override
    public void process(Client client) {
        client.setMulticastGroup(null);
        System.out.println("Operazione di modifica andata a buon fine.");
    }
}
