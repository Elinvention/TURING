package server;

import protocol.DocumentUri;

import java.io.Serializable;
import java.util.Set;

/*
 * Contiene le informazioni necessarie a mostrare la lista dei documenti all'utente
 */
public class DocumentInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DocumentUri uri;
    private final Set<String> collaborators;

    public DocumentInfo(DocumentUri uri, Set<String> collaborators) {
        this.uri = uri;
        this.collaborators = collaborators;
    }

    @Override
    public String toString() {
        return this.uri.docName + "\n\tCreatore: " + this.uri.owner + "\n\tCollaboratori: " + String.join(", ", this.collaborators);
    }
}
