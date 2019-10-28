package protocol.request;

import exceptions.InvalidRequestException;
import exceptions.ProtocolException;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.*;

import java.net.Socket;

public class InviteCollaboratorRequest extends Request {
    private final long sessionID;
    private final String docName;
    private final String collaborator;

    public InviteCollaboratorRequest(long sessionID, String docName, String collaborator) {
        this.sessionID = sessionID;
        this.docName = docName;
        this.collaborator = collaborator;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getUserFromSession(sessionID);
            User collaborator = State.getInstance().getUser(this.collaborator);
            if (collaborator == requester)
                throw new InvalidRequestException("Cannot share document to owner.");
            Document toShare = requester.getDocument(requester, docName);
            toShare.inviteCollaborator(collaborator);
            requester.processInbox(client);
            return new AckResponse(this);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    public String toString() {
        return "Invite " + this.collaborator + " to collaborate on " + this.docName;
    }
}
