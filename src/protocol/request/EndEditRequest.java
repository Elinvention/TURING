package protocol.request;

import exceptions.*;
import protocol.DocumentUri;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.Document;
import server.State;
import server.User;

import java.net.Socket;

public class EndEditRequest extends Request {
    private final long sessionID;
    private final DocumentUri uri;
    private final String editedText;

    public EndEditRequest(long sessionID, DocumentUri uri, String editedText) {
        this.sessionID = sessionID;
        this.uri = uri;
        this.editedText = editedText;
    }

    @Override
    public Response process(Socket client) {
        try {
            User editor = State.getInstance().getUserFromSession(this.sessionID);
            Document doc = State.getInstance().getDocument(editor, this.uri);
            doc.unlockSection(editor, editedText, this.uri.section);
            return new AckResponse(this);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
    }

    @Override
    public String toString() {
        return "End edit on " + this.uri.toString();
    }
}
