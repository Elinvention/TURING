package protocol.response;

public class InviteNotification extends Response {
    private final String owner;
    private final String docName;

    public InviteNotification(String owner, String docName) {
        this.owner = owner;
        this.docName = docName;
    }

    @Override
    public String toString() {
        return "Invite to collaborate on " + owner + "/" + docName;
    }
}
