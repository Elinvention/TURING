package protocol.request;

import exceptions.DuplicateDocumentException;
import exceptions.InvalidSessionException;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.State;
import server.User;

import java.net.Socket;

/*
 * Richiesta di creazione nuovo documento.
 */
public class CreateDocumentRequest extends Request {
    private static final long serialVersionUID = 1L;

    public final long sessionID;
    private final String document_name;
    private final int sections;

    public CreateDocumentRequest(long sessionID, String document_name, int sections) {
        this.sessionID = sessionID;
        this.document_name = document_name;
        this.sections = sections;
    }

    @Override
    public Response process(Socket client) {
        try {
            User owner = State.getInstance().getUserFromSession(this.sessionID);
            synchronized (owner) {
                owner.createDocument(this.document_name, this.sections);
                owner.processInbox(client);
            }
            return new AckResponse(this);
        } catch (DuplicateDocumentException | InvalidSessionException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    public String toString() {
        return "Create document \"" + document_name + "\" with " + sections + " sections.";
    }
}
