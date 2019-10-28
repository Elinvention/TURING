package protocol.request;

import exceptions.DocumentNotFoundException;
import exceptions.InvalidUsernameException;
import exceptions.NotAllowedException;
import exceptions.ProtocolException;
import protocol.DocumentUri;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import protocol.response.ShowDocumentResponse;
import server.Document;
import server.State;
import server.User;

import java.net.Socket;

public class ShowDocumentRequest extends Request {
    private final long sessionID;
    private final DocumentUri uri;

    public ShowDocumentRequest(long sessionID, DocumentUri uri) {
        this.sessionID = sessionID;
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getUserFromSession(this.sessionID);
            Document document = State.getInstance().getDocument(requester, this.uri);
            requester.processInbox(client);
            return new ShowDocumentResponse(document);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    public String toString() {
        return "Show document " + this.uri;
    }
}
