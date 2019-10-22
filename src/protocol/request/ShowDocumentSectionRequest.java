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
    private final DocumentUri uri;

    public ShowDocumentSectionRequest(DocumentUri uri) {
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getLoggedInUser(client);
            DocumentSection docSection = State.getInstance().getDocumentSection(requester, this.uri);
            return new ShowDocumentSectionResponse(docSection);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
    }
}
