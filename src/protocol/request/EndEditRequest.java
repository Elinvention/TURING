package protocol.request;

import exceptions.*;
import protocol.DocumentUri;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.DocumentSection;
import server.State;
import server.User;

import java.net.Socket;

public class EndEditRequest extends Request {
    private final DocumentUri uri;
    private final String editedText;

    public EndEditRequest(DocumentUri uri, String editedText) {
        this.uri = uri;
        this.editedText = editedText;
    }

    @Override
    public Response process(Socket client) {
        try {
            User editor = State.getInstance().getLoggedInUser(client);
            DocumentSection ds = State.getInstance().getDocumentSection(editor, this.uri);
            ds.setText(editor, editedText);
            ds.setCurrentEditor(null);
        } catch (ProtocolException e) {
            return new ExceptionResponse(e);
        }
        return new AckResponse(this);
    }

    @Override
    public String toString() {
        return "End edit on " + this.uri.toString();
    }
}
