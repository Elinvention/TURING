package protocol.request;

import exceptions.*;
import protocol.DocumentUri;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.DocumentSection;
import server.State;
import server.User;

import java.net.Socket;

public class EditRequest extends Request {
    private final DocumentUri uri;

    public EditRequest(DocumentUri uri) {
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getLoggedInUser(client);
            DocumentSection s = State.getInstance().getDocumentSection(requester, uri);
            s.setCurrentEditor(requester);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
        return new AckResponse(this);
    }

    @Override
    public String toString() {
        return "Edit " + uri.toString();
    }
}
