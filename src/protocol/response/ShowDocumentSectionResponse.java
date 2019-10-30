package protocol.response;

import client.Client;
import server.DocumentSection;

import java.io.IOException;

/*
 * Response generata a seguito di una ShowDocumentSectionRequest.
 * Contiene solo la sezione richiesta.
 */
public class ShowDocumentSectionResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final DocumentSection section;

    public ShowDocumentSectionResponse(DocumentSection section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return section.toString();
    }

    @Override
    public void process(Client client) {
        try {
            this.section.save();
            System.out.println("Sezione " + section.getUri() + " salvata con successo in " + section.getUri().getPath());
        } catch(IOException e) {
            System.err.println("Si è verificato un errore durante il salvataggio della sezione:");
            e.printStackTrace();
        }
    }
}
