package protocol.request;

import exceptions.ProtocolException;
import protocol.DocumentUri;
import protocol.response.Response;
import protocol.response.ShowDocumentSectionResponse;
import server.DocumentSection;
import server.State;
import server.User;

import java.net.Socket;


/*
 * Richiesta di download di una sezione.
 */
public class ShowDocumentSectionRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final long sessionID;
    private final DocumentUri uri;

    public ShowDocumentSectionRequest(long sessionID, DocumentUri uri) {
        this.sessionID = sessionID;
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) throws ProtocolException {
        User requester = State.getInstance().getUserFromSession(this.sessionID);
        DocumentSection docSection = State.getInstance().getDocumentSection(requester, this.uri);
        requester.processInbox(client);
        return new ShowDocumentSectionResponse(docSection);
    }

    @Override
    public String toString() {
        return "Show section " + this.uri;
    }
}
