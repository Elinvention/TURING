package protocol.response;

import server.DocumentSection;

public class ShowDocumentSectionResponse extends Response {
    private final DocumentSection section;

    public ShowDocumentSectionResponse(DocumentSection section) {
        this.section = section;
    }

    @Override
    public String toString() {
        return section.toString();
    }
}
