package protocol.response;

import client.Client;
import protocol.Message;


public abstract class Response extends Message {
    private static final long serialVersionUID = 1L;

    public abstract void process(Client client);
}
