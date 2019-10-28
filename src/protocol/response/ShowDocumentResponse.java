package protocol.response;

import client.Client;
import server.Document;

import java.io.IOException;

public class ShowDocumentResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final Document document;

    public ShowDocumentResponse(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return document.toString();
    }

    @Override
    public void process(Client client) {
        try {
            this.document.save();
            System.out.println("Documento " + document.getName() + " salvato con successo in " + document.uri.getPath());
        } catch (IOException e) {
            System.err.println("Si è verificato un errore durante il salvataggio della sezione:");
            e.printStackTrace();
        }
    }
}
