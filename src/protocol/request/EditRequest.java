package protocol.request;

import exceptions.ProtocolException;
import protocol.DocumentUri;
import protocol.response.EditResponse;
import protocol.response.Response;
import server.Document;
import server.DocumentSection;
import server.State;
import server.User;

import java.net.InetAddress;
import java.net.Socket;

/*
 * Richiesta di inzio editing.
 */
public class EditRequest extends Request {
    private static final long serialVersionUID = 1L;

    public final long sessionID;
    public final DocumentUri uri;

    public EditRequest(long sessionID, DocumentUri uri) {
        this.sessionID = sessionID;
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) throws ProtocolException {
        User requester = State.getInstance().getUserFromSession(this.sessionID);
        Document doc = State.getInstance().getDocument(requester, uri);
        DocumentSection docSection = doc.lockSection(requester, uri.section);
        InetAddress addr = doc.getChatAddress();
        requester.processInbox(client);
        return new EditResponse(docSection, addr);
    }

    @Override
    public String toString() {
        return "Edit " + uri.toString();
    }
}
