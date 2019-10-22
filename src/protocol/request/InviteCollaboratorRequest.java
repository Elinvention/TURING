package protocol.request;

import exceptions.ProtocolException;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.*;

import java.net.Socket;

public class InviteCollaboratorRequest extends Request {
    private final String docName;
    private final String collaborator;

    public InviteCollaboratorRequest(String docName, String collaborator) {
        this.docName = docName;
        this.collaborator = collaborator;
    }

    @Override
    public Response process(Socket client) {
        try {
            User requester = State.getInstance().getLoggedInUser(client);
            User collaborator = State.getInstance().getUser(this.collaborator);
            Document toShare = requester.getDocument(requester, docName);
            toShare.inviteCollaborator(collaborator);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
        return new AckResponse(this);
    }

    @Override
    public String toString() {
        return "Invite " + this.collaborator + " to collaborate on " + this.docName;
    }
}
