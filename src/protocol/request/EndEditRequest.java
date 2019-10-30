package protocol.request;

import exceptions.ProtocolException;
import protocol.DocumentUri;
import protocol.response.EndEditResponse;
import protocol.response.Response;
import server.Document;
import server.State;
import server.User;

import java.net.Socket;

/*
 * Richiesta di terminazione editing.
 */
public class EndEditRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final long sessionID;
    private final DocumentUri uri;
    private final String editedText;

    public EndEditRequest(long sessionID, DocumentUri uri, String editedText) {
        this.sessionID = sessionID;
        this.uri = uri;
        this.editedText = editedText;
    }

    @Override
    public Response process(Socket client) throws ProtocolException {
        User editor = State.getInstance().getUserFromSession(this.sessionID);
        Document doc = State.getInstance().getDocument(editor, this.uri);
        doc.unlockSection(editor, editedText, this.uri.section);
        editor.processInbox(client);
        return new EndEditResponse();
    }

    @Override
    public String toString() {
        return "End edit on " + this.uri.toString();
    }
}
