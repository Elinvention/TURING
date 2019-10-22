package protocol.request;

import protocol.Message;
import protocol.response.Response;

import java.net.Socket;


public abstract class Request extends Message {
    public abstract Response process(Socket client);
}
