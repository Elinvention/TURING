package protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;


public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public void send(Socket sock) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        oos.writeObject(this);
        oos.flush();
    }

    public static Message receive(Socket sock) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
        Message msg = (Message) ois.readObject();
        return msg;
    }
}
