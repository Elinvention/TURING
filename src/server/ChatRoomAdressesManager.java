package server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/*
 * Un singleton che mantiene traccia degli indirizzi multicast in utilizzo e ne genera nuovi
 */
public class ChatRoomAdressesManager {
    public static final int PORT = 2000;  //porta della chat UDP
    public static final int MAX = 268_435_456;  // 240 * 2 ^ 28 - 224 * 2 ^ 28

    // Insieme di indirizzi in utilizzo
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

    // Genera una stringa che contiene un IPv4 in dotted notation e lo converte in InetAddress
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

    // Apre una Chatroom asssegnando un nuovo indirizzo multicast non utilizzato
    public synchronized InetAddress openChatRoom() {
        if (usedAddresses.size() >= MAX)
            return null;
        InetAddress address = this.randomMulticastAddress();
        while (usedAddresses.contains(address))
            address = this.randomMulticastAddress();
        usedAddresses.add(address);
        return address;
    }

    // Chiude una chatroom rendendo nuovamente disponibile l'indirizzo multicast
    public synchronized void closeChatRoom(InetAddress address) {
        usedAddresses.remove(address);
    }
}
