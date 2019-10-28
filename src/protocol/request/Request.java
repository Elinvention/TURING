package protocol.request;

import protocol.Message;
import protocol.response.Response;

import java.net.Socket;


public abstract class Request extends Message {
    private static final long serialVersionUID = 1L;

    public abstract Response process(Socket client);
}
