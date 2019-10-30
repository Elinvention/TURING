package protocol.request;

import exceptions.InvalidRequestException;
import exceptions.InvalidSessionException;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.State;

import java.net.Socket;


/*
 * Richiesta di logout
 */
public class LogoutRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final long sessionID;

    public LogoutRequest(long sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public Response process(Socket client) {
        try {
            State.getInstance().getUserFromSession(sessionID).processInbox(client);
            State.getInstance().logout(sessionID);
        } catch (InvalidRequestException | InvalidSessionException e) {
            return new ExceptionResponse(e);
        }
        return new AckResponse(this);
    }

    @Override
    public String toString() {
        return "Logout Request.";
    }
}
