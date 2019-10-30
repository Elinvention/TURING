package protocol.response;

import client.Client;
import server.DocumentInfo;

import java.util.List;

/*
 * Una Response generata dal server a seguito di una ListDocumentRequest.
 * Produce la lista di documenti posseduti e modificabili dall'utente.
 */
public class ListDocumentsResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final List<DocumentInfo> infos;

    public ListDocumentsResponse(List<DocumentInfo> infos) {
        this.infos = infos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.infos.size() == 0)
            sb.append("Non hai documenti. Perché non ne crei uno?");
        else {
            sb.append("Documenti a cui si ha accesso:");
            for (DocumentInfo docInfo : this.infos) {
                sb.append("\n" + docInfo.toString());
            }
        }
        return sb.toString();
    }

    @Override
    public void process(Client client) {
        System.out.println(this.toString());
    }
}
