package protocol.request;

import exceptions.NotAllowedException;
import protocol.response.ExceptionResponse;
import protocol.response.ListDocumentsResponse;
import protocol.response.Response;
import server.State;
import server.User;

import java.net.Socket;

public class ListDocumentsRequest extends Request {
    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getLoggedInUser(client);
            return new ListDocumentsResponse(requester.listDocuments());
        } catch (NotAllowedException e) {
            return new ExceptionResponse(e);
        }
    }
}
