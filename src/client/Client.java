package client;

import protocol.DocumentUri;
import protocol.request.*;
import protocol.Message;
import protocol.response.*;
import server.ChatRoomAdressesManager;
import server.Server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


public class Client {
    private Socket socket = new Socket();
    private InetAddress multicastGroup;
    private Long sessionID;

    public Client() {
        InetAddress remoteAddress = null;
        try {
            remoteAddress = InetAddress.getLocalHost();
            String address = Files.readString(Paths.get("address.txt")).strip();
            remoteAddress = InetAddress.getByName(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            SocketAddress addr = new InetSocketAddress(remoteAddress, Server.PORT);
            socket.connect(addr);
            socket.setSoTimeout(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSessionID() {
        if (this.sessionID == null) {
            try {
                Files.deleteIfExists(Paths.get(".turing.sessionID"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(".turing.sessionID"));) {
                dos.writeLong(this.sessionID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSessionID() {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(".turing.sessionID"))) {
            this.sessionID = dis.readLong();
        } catch (FileNotFoundException | EOFException e) {
            this.sessionID = null;
        } catch (IOException e) {
            this.sessionID = null;
            e.printStackTrace();
        }
    }

    private void saveMulticastGroup() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(".turing.multicastGroup"))) {
            oos.writeObject(this.multicastGroup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMulticastGroup() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(".turing.multicastGroup"))) {
            this.multicastGroup = (InetAddress) ois.readObject();
        } catch (FileNotFoundException e) {
            this.multicastGroup = null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            this.multicastGroup = null;
        }
    }

    public void login(String username, String password) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        if (this.sessionID != null)
            this.logout();
        LoginRequest req = new LoginRequest(username, password);
        System.out.println(req.toString());
        req.send(socket);
        Response response = receiveResponse();
        if (response instanceof LoginResponse) {
            this.sessionID = ((LoginResponse) response).sessionID;
            this.saveSessionID();
        }
    }

    public void logout() throws IOException, ClassNotFoundException {
        this.loadSessionID();
        LogoutRequest req = new LogoutRequest(this.sessionID);
        System.out.println(req.toString());
        req.send(socket);
        Response response = receiveResponse();
        if (response instanceof AckResponse) {
            this.sessionID = null;
            this.saveSessionID();
        }
    }

    public void createDocument(String docName, int sections) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        CreateDocumentRequest req = new CreateDocumentRequest(sessionID, docName, sections);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void showDocument(DocumentUri uri) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        ShowDocumentRequest req = new ShowDocumentRequest(sessionID, uri);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void showDocumentSection(DocumentUri uri) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        ShowDocumentSectionRequest req = new ShowDocumentSectionRequest(sessionID, uri);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void editDocument(DocumentUri uri) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        EditRequest req = new EditRequest(sessionID, uri);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void endEditDocument(DocumentUri uri, String editedText) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        EndEditRequest req = new EndEditRequest(sessionID, uri, editedText);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void inviteCollaborator(String docName, String username) throws IOException, ClassNotFoundException {
        this.loadSessionID();
        InviteCollaboratorRequest req = new InviteCollaboratorRequest(sessionID, docName, username);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void listDocuments() throws IOException, ClassNotFoundException {
        this.loadSessionID();
        ListDocumentsRequest req = new ListDocumentsRequest(sessionID);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void sendChatMessage(String message) {
        this.loadMulticastGroup();
        if (this.multicastGroup == null) {
            System.err.println("You are not editing any document.");
            return;
        }
        try (MulticastSocket socket = new MulticastSocket(ChatRoomAdressesManager.PORT)) {
            socket.setTimeToLive(1);
            socket.setLoopbackMode(false);
            socket.setReuseAddress(true);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteStream);
            out.writeUTF(message);
            byte[] data = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, this.multicastGroup, ChatRoomAdressesManager.PORT);
            socket.send(packet);
            System.out.format("Sent message: \"%s\"\n", message);
        } catch (IOException e) {
            System.err.println("Some error appeared: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void receiveChatMessages() {
        this.loadMulticastGroup();
        if (this.multicastGroup == null) {
            System.err.println("You are not editing any document.");
            return;
        }
        try (MulticastSocket client = new MulticastSocket(ChatRoomAdressesManager.PORT);) {
            client.joinGroup(this.multicastGroup);
            DatagramPacket packet = new DatagramPacket(new byte[512], 512);
            while(true) {
                client.receive(packet);
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                        packet.getData(), packet.getOffset(), packet.getLength()));
                String message = in.readUTF();
                System.out.println("Message received: " + message);
            }
        } catch (IOException e) {
            System.err.println("Some error appeared: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Response receiveResponse() throws IOException, ClassNotFoundException {
        Response response = (Response) Message.receive(socket);
        while (response instanceof InviteNotification) {
            System.out.println("Received " + response);
            response = (Response) Message.receive(socket);
        }
        if (response instanceof EditResponse) {
            this.multicastGroup = ((EditResponse) response).chatAddress;
            this.saveMulticastGroup();
        }
        printResponse(response);
        return response;
    }

    public static void printResponse(Response response) {
        if (response instanceof ExceptionResponse) {
            System.err.println(response.toString());
        } else {
            System.out.println(response.toString());
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showUsage() {
        System.out.println(
                "turing -- help\n"
                + "usage: turing COMMAND [ ARGS ...]\n"
                + "commands:\n"
                + "register < username > < password > registra l'utente\n"
                + "login < username > < password > effettua il login\n"
                + "logout effettua il logout\n"
                + "create < doc > < numsezioni > crea un documento\n"
                + "share < URI > < username > condivide il documento\n"
                + "show < URI > mostra l'intero documento o una sezione\n"
                + "list - mostra la lista dei documenti\n"
                + "edit < URI > modifica una sezione del documento\n"
                + "end-edit < URI > < testo > fine modifica della sezione del documento\n"
                + "send < msg > invia un msg sulla chat\n"
                + "receive visualizza i msg ricevuti sulla chat\n"
        );
    }

    public static void main(String args[]) {
        Client c = new Client();

        try {
            if (args.length == 1) {
                if (args[0].equals("logout")) {
                    c.logout();
                } else if (args[0].equals("receive")) {
                    c.receiveChatMessages();
                } else if (args[0].equals("list")) {
                    c.listDocuments();
                } else {
                    showUsage();
                    System.exit(-1);
                }
            } else if (args.length == 2) {
                if (args[0].equals("send")) {
                    c.sendChatMessage(args[1]);
                } else if (args[0].equals("show")) {
                    try {
                        DocumentUri parsedUri = DocumentUri.parse(args[1]);
                        if (parsedUri.section == null)
                            c.showDocument(parsedUri);
                        else
                            c.showDocumentSection(parsedUri);
                    } catch (IllegalArgumentException e) {
                        System.err.format("Failed to parse URI \"%s\". Valid URIs have the format owner/document[/section]\n", args[1]);
                    }
                } else if (args[0].equals("edit")) {
                    c.editDocument(DocumentUri.parse(args[1]));
                } else {
                    showUsage();
                    System.exit(-1);
                }
            } else if (args.length == 3) {
                if (args[0].equals("register")) {
                    RmiRegisterUserClient.registerUser(args[1], args[2]);
                } else if (args[0].equals("login")) {
                    c.login(args[1], args[2]);
                } else if (args[0].equals("create")) {
                    c.createDocument(args[1], Integer.parseInt(args[2]));
                } else if (args[0].equals("share")) {
                    c.inviteCollaborator(args[1], args[2]);
                } else if (args[0].equals("end-edit")) {
                    c.endEditDocument(DocumentUri.parse(args[1]), args[2]);
                } else {
                    showUsage();
                    System.exit(-1);
                }
            } else {
                showUsage();
                System.exit(-1);
            }
        } catch (IOException | ClassNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        Client c = new Client();
        Client d = new Client();
        try {
            // test login when user does not exist
            c.login("test1", "testpwd1");
            // test register user
            RmiRegisterUserClient.registerUser("test1", "testpwd1");
            RmiRegisterUserClient.registerUser("test2", "testpwd2");
            // test login with wrong password
            c.login("test1", "testpwd2");
            // test correct login
            c.login("test1", "testpwd1");

            // test logout when not logged in
            c.logout();
            c.logout();

            // test create document
            c.createDocument("testdoc", 10);

            c.login("test1", "testpwd1");
            c.createDocument("testdoc", 10);
            c.createDocument("testdoc", 10);
            c.logout();

            c.createDocument("testdoc", 10);

            c.login("test1", "testpwd1");
            for (int i = 0; i < 10; i++) {
                c.createDocument("doc" + i, 1);
            }
            c.listDocuments();
            c.showDocument(new DocumentUri("test1", "troll"));
            c.showDocument(new DocumentUri("troll", "troll"));
            c.showDocument(new DocumentUri("test1", "testdoc"));
            c.endEditDocument(new DocumentUri("test1", "testdoc", 3), "lol\n");
            c.showDocument(new DocumentUri("test1", "testdoc"));

            d.login("test2", "testpwd2");
            d.listDocuments();
            d.showDocument(new DocumentUri("test1", "testdoc"));
            d.logout();
            c.inviteCollaborator("wrongDoc", "test2");
            c.inviteCollaborator("testdoc", "wrongUser");
            c.inviteCollaborator("testdoc", "test2");
            for (int i = 0; i < 10; i++) {
                c.inviteCollaborator("doc" + i, "test2");
            }
            d.login("test2", "testpwd2");
            d.listDocuments();
            d.showDocument(new DocumentUri("test1", "testdoc"));

            for (int i = 0; i < 10; i++) {
                DocumentUri uri = new DocumentUri("test1", "testdoc", i);
                c.editDocument(uri);
                c.showDocumentSection(uri);
                d.editDocument(uri);
                c.endEditDocument(uri, String.valueOf(i) + "\n");
                c.showDocumentSection(uri);
            }
            c.showDocument(new DocumentUri("test1", "testdoc"));
            c.logout();

            c.login("test1", "testpwd1");
            d.login("test2", "testpwd2");
            c.editDocument(new DocumentUri("test1", "testdoc", 1));
            d.editDocument(new DocumentUri("test1", "testdoc", 2));
            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                String message = sc.nextLine();
                c.sendChatMessage(message);
            }
            c.logout();
            d.logout();
        } catch (UnknownHostException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
            System.exit(-2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-3);
        } finally {
            c.close();
            d.close();
        }
    }
}
