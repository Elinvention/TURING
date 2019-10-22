package protocol.response;

import protocol.DocumentUri;

import java.util.List;

public class ListDocumentsResponse extends Response {
    public final List<DocumentUri> list;

    public ListDocumentsResponse(List<DocumentUri> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DocumentUri docUri : list) {
            sb.append(docUri.toString() + "\n");
        }
        return sb.toString();
    }
}
