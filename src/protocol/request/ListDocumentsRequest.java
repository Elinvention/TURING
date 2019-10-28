package protocol.request;

import exceptions.InvalidSessionException;
import protocol.response.ExceptionResponse;
import protocol.response.ListDocumentsResponse;
import protocol.response.Response;
import server.State;
import server.User;

import java.net.Socket;

public class ListDocumentsRequest extends Request {
    public final long sessionID;

    public ListDocumentsRequest(long sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getUserFromSession(this.sessionID);
            requester.processInbox(client);
            return new ListDocumentsResponse(requester.listDocumentUris());
        } catch (InvalidSessionException e) {
            return new ExceptionResponse(e);
        }
    }
}
