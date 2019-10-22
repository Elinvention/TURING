package server;

import exceptions.*;
import protocol.DocumentUri;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


public class User {
    private String name;
    private String password;
    private Map<String, Document> documents;
    private Set<Document> collaboratingOn;
    private Queue<Invite> inviteInbox;
    private Socket client;

    public User(String name, String password) throws InvalidUsernameException, InvalidPasswordException {
        if (name == null || password == null) {
            throw new NullPointerException();
        }
        if (name.length() < 5) {
            throw new InvalidUsernameException();
        }
        if (password.length() < 8) {
            throw new InvalidPasswordException();
        }
        this.name = name;
        this.password = password;
        this.documents = new HashMap<>();
        this.collaboratingOn = new HashSet<>();
        this.inviteInbox = new ArrayDeque<>();
    }

    public static User registerUser(String name, String password) throws InvalidPasswordException, InvalidUsernameException {
        User newUser = new User(name, password);
        try {
            Path path = Paths.get(PermanentStorage.BASE_FOLDER, name);
            Files.createDirectories(path);
            Files.writeString(path.resolve("password.txt"), password, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("Could not create user data: " + e.getMessage());
        }
        return newUser;
    }

    public static User load(Path userFolder) throws IOException, InvalidPasswordException, InvalidUsernameException {
        String username = userFolder.getFileName().toString();
        System.out.println("Loading user " + username);
        String password = Files.readString(userFolder.resolve("password.txt"));
        User user = new User(username, password);
        user.loadDocuments(userFolder);
        return user;
    }

    private void loadDocuments(Path userFolder) {
        try {
            Files.list(userFolder).forEach(documentFolder -> {
               try {
                   String docName = documentFolder.getFileName().toString();
                   int sections = Files.list(documentFolder).toArray().length;
                   System.out.println("Loading document " + docName);
                   Document doc = Document.load(new DocumentUri(this.name, docName), this, sections);
                   this.documents.put(docName, doc);
               } catch (NotDirectoryException ignored) {

               } catch (IOException e) {
                   e.printStackTrace();
               }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User '").append(this.name).append("' with password '").append(this.password).append("'");
        if (client != null)
            sb.append(" logged in from ").append(client.getRemoteSocketAddress().toString());
        return sb.toString();
    }

    public String getName() {
        return this.name;
    }

    public List<Document> getOwnedDocuments() {
        List<Document> docs = new ArrayList<>();
        docs.addAll(this.documents.values());
        return docs;
    }

    public List<DocumentUri> listDocuments() {
        List<DocumentUri> docs = new ArrayList<>();
        docs.addAll(this.documents.values().stream().map(d -> d.uri).collect(Collectors.toList()));
        docs.addAll(this.collaboratingOn.stream().map(d -> d.uri).collect(Collectors.toList()));
        return docs;
    }

    public void login(String password, Socket client) throws InvalidPasswordException, InvalidRequestException {
        if (this.client != null)
            throw new InvalidRequestException();
        if (!this.password.equals(password)) {
            System.out.println("User " + this.name + " entered wrong password!");
            this.client = null;
            throw new InvalidPasswordException();
        }
        this.client = client;
        System.out.println("User " + this.name + " logged in from " + this.client.getRemoteSocketAddress() + ".");
        this.processInbox();
    }

    public void processInbox() {
        if (this.client == null)
            return;
        while (!inviteInbox.isEmpty()) {
            Invite invite = inviteInbox.remove();
            try {
                invite.send(this.client);
            } catch (IOException | NullPointerException e) {
                System.err.println("Failed to send " + invite.toString());
            }
        }
    }

    public void queueInvite(Invite invite) {
        this.inviteInbox.add(invite);
        this.collaboratingOn.add(invite.document);
    }

    public void logout() throws InvalidRequestException {
        if (this.client == null) {
            System.out.println("User " + this.name + " was not logged in!");
            throw new InvalidRequestException();
        }
        System.out.println("User " + this.name + " logged out.");
        this.client = null;
    }

    void clientDisconnected() {
        this.client = null;
    }

    public Document createDocument(String docName, int sections) throws NotAllowedException, DuplicateDocumentException {
        if (client == null) {
            throw new NotAllowedException();
        }
        if (documents.containsKey(docName)) {
            throw new DuplicateDocumentException();
        }
        Document doc = new Document(new DocumentUri(this.name, docName), this, sections);
        this.documents.put(docName, doc);
        return doc;
    }

    public Document getDocument(User requester, String name) throws DocumentNotFoundException, NotAllowedException {
        if (!documents.containsKey(name))
            throw new DocumentNotFoundException();
        Document doc = documents.get(name);
        if (!doc.isAllowed(requester))
            throw new NotAllowedException();
        return doc;
    }
}
