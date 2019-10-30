package protocol.request;

import exceptions.ProtocolException;
import protocol.DocumentUri;
import protocol.response.Response;
import protocol.response.ShowDocumentResponse;
import server.Document;
import server.State;
import server.User;

import java.net.Socket;

/*
 * Richiesta di download di un intero documento
 */
public class ShowDocumentRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final long sessionID;
    private final DocumentUri uri;

    public ShowDocumentRequest(long sessionID, DocumentUri uri) {
        this.sessionID = sessionID;
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) throws ProtocolException {
        User requester = State.getInstance().getUserFromSession(this.sessionID);
        Document document = State.getInstance().getDocument(requester, this.uri);
        requester.processInbox(client);
        return new ShowDocumentResponse(document);
    }

    @Override
    public String toString() {
        return "Show document " + this.uri;
    }
}
