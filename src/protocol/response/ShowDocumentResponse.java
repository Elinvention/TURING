package protocol.response;

import server.Document;

public class ShowDocumentResponse extends Response {
    private final Document document;

    public ShowDocumentResponse(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return document.toString();
    }
}
