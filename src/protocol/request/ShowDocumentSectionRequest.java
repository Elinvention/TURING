package protocol.request;

import exceptions.ProtocolException;
import protocol.DocumentUri;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import protocol.response.ShowDocumentSectionResponse;
import server.DocumentSection;
import server.State;
import server.User;

import java.net.Socket;


public class ShowDocumentSectionRequest extends Request {
    private final long sessionID;
    private final DocumentUri uri;

    public ShowDocumentSectionRequest(long sessionID, DocumentUri uri) {
        this.sessionID = sessionID;
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getUserFromSession(this.sessionID);
            DocumentSection docSection = State.getInstance().getDocumentSection(requester, this.uri);
            return new ShowDocumentSectionResponse(docSection);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    public String toString() {
        return "Show section " + this.uri;
    }
}
