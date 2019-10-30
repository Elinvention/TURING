package protocol.request;

import exceptions.InvalidSessionException;
import protocol.response.ExceptionResponse;
import protocol.response.ListDocumentsResponse;
import protocol.response.Response;
import server.DocumentInfo;
import server.State;
import server.User;

import java.net.Socket;
import java.util.List;

/*
 * Richiesta di elencazione dei documenti posseduti e modificabili dall'utente.
 */
public class ListDocumentsRequest extends Request {
    private static final long serialVersionUID = 1L;

    public final long sessionID;

    public ListDocumentsRequest(long sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getUserFromSession(this.sessionID);
            List<DocumentInfo> infos = requester.listDocumentInfos();
            requester.processInbox(client);
            return new ListDocumentsResponse(infos);
        } catch (InvalidSessionException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    public String toString() {
        return "Richiesta lista documenti accessibili. (ID sessione: " + sessionID + ").";
    }
}
