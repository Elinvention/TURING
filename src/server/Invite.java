package server;

import protocol.response.InviteNotification;

import java.io.IOException;
import java.net.Socket;

public class Invite {
    public final Document document;
    public final User invitedUser;

    public Invite(Document document, User invited) {
        this.document = document;
        this.invitedUser = invited;
    }

    public void send(Socket client) throws IOException, NullPointerException {
        System.out.println("Try to send " + toString());
        InviteNotification notification = new InviteNotification(document.getOwner().getName(), document.getName());
        notification.send(client);
        System.out.println("Sent " + toString());
    }

    @Override
    public String toString() {
        return "Invite for user \"" + invitedUser.getName() + " to collaborate on document \"" + document.getName() + "\".";
    }
}
