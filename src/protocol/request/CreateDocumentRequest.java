package protocol.request;

import exceptions.DuplicateDocumentException;
import exceptions.NotAllowedException;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.State;
import server.User;

import java.net.Socket;


public class CreateDocumentRequest extends Request {
    private final String document_name;
    private final int sections;

    public CreateDocumentRequest(String document_name, int sections) {
        this.document_name = document_name;
        this.sections = sections;
    }

    @Override
    public Response process(Socket client) {
        try {
            User owner = State.getInstance().getLoggedInUser(client);
            owner.createDocument(this.document_name, this.sections);
        } catch (NotAllowedException | DuplicateDocumentException e) {
            return new ExceptionResponse(e);
        }
        return new AckResponse(this);
    }

    @Override
    public String toString() {
        return "Create document \"" + document_name + "\" with " + sections + " sections.";
    }
}
