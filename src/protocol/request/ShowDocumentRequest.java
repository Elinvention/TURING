package protocol.request;

import exceptions.DocumentNotFoundException;
import exceptions.InvalidUsernameException;
import exceptions.NotAllowedException;
import protocol.DocumentUri;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import protocol.response.ShowDocumentResponse;
import server.Document;
import server.State;
import server.User;

import java.net.Socket;

public class ShowDocumentRequest extends Request {
    private final DocumentUri uri;

    public ShowDocumentRequest(DocumentUri uri) {
        this.uri = uri;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getLoggedInUser(client);
            Document document = State.getInstance().getDocument(requester, this.uri);
            return new ShowDocumentResponse(document);
        } catch (DocumentNotFoundException | InvalidUsernameException | NotAllowedException e) {
            return new ExceptionResponse(e);
        }
    }
}
