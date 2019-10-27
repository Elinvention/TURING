package protocol.response;

import server.DocumentSection;

import java.net.InetAddress;

public class EditResponse extends Response {
    public final DocumentSection section;
    public final InetAddress chatAddress;

    public EditResponse(DocumentSection section, InetAddress chatAddress) {
        this.section = section;
        this.chatAddress = chatAddress;
    }

    @Override
    public String toString() {
        return section.getUri() + " chat address: " + chatAddress + ". Content:\n" + section.getText();
    }
}
