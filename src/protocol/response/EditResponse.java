package protocol.response;

import client.Client;
import server.DocumentSection;

import java.io.IOException;
import java.net.InetAddress;

/*
 * Risposta ad una richiesta EditRequest. Contiene la sezione da modificare e l'indirizzo di chat
 * assegnato al documento.
 */
public class EditResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final DocumentSection section;
    public final InetAddress chatAddress;

    public EditResponse(DocumentSection section, InetAddress chatAddress) {
        this.section = section;
        this.chatAddress = chatAddress;
    }

    @Override
    public String toString() {
        return section.getUri() + " bloccato con successo. Indirizzo chat: " + chatAddress + ".";
    }

    @Override
    public void process(Client client) {
        client.setMulticastGroup(this.chatAddress);
        System.out.println(this.toString());
        try {
            this.section.save();
            System.out.println("Sezione " + section.getUri() + " salvata con successo in " + section.getUri().getPath());
        } catch (IOException e) {
            System.err.println("Si è verificato un errore durante il salvataggio della sezione:");
            e.printStackTrace();
        }
    }
}
