package protocol.request;

import exceptions.ProtocolException;
import protocol.Message;
import protocol.response.Response;

import java.net.Socket;


/*
 * Message che vengono spediti dal client e ricevuti dal server. Vengono interpretati dal sever come richieste.
 * il metodo astratto Response process(Socket client) è in grado di modificare lo stato del server.
 */
public abstract class Request extends Message {
    private static final long serialVersionUID = 1L;

    public abstract Response process(Socket client) throws ProtocolException;
}
