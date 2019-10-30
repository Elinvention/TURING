package protocol.response;

import client.Client;
import protocol.Message;


/*
 * Message che vengono spediti dal server e ricevuti dal client a seguito di una richiesta.
 * il metodo astratto process(Client client) è in grado di modificare lo stato del client.
 */
public abstract class Response extends Message {
    private static final long serialVersionUID = 1L;

    public abstract void process(Client client);
}
