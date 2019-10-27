package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ChatRoomAdressesManager {
    public static final int PORT = 2000;
    public static final int MAX = 268_435_456;
    private final Set<InetAddress> usedAddresses;
    private Random rng = new Random();

    private static ChatRoomAdressesManager singleton;

    private ChatRoomAdressesManager() {
        this.usedAddresses = new HashSet<>();
    }

    public static final ChatRoomAdressesManager getInstance() {
        if (singleton == null)
            singleton = new ChatRoomAdressesManager();
        return singleton;
    }

    private InetAddress randomMulticastAddress() {
        String ip4 = 224 + rng.nextInt(16) + "."
                + rng.nextInt(255) + "."
                + rng.nextInt(255) + "."
                + rng.nextInt(255);
        try {
            return InetAddress.getByName(ip4);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized InetAddress openChatRoom() {
        if (usedAddresses.size() >= MAX)
            return null;
        InetAddress address = this.randomMulticastAddress();
        while (usedAddresses.contains(address))
            address = this.randomMulticastAddress();
        usedAddresses.add(address);
        return address;
    }

    public synchronized void closeChatRoom(InetAddress address) {
        usedAddresses.remove(address);
    }

    public static void main(String args[]) {
        for (int i = 0; i < 100; i++)
            System.out.println(getInstance().openChatRoom());
    }
}
