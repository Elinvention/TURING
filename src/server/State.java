package server;

import exceptions.*;
import protocol.DocumentUri;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class State implements Serializable {
    private static State singleton;

    private Map<String, User> users = new HashMap<>();
    private transient Map<Socket, User> logins = new HashMap<>();

    private State() {

    }

    public static State getInstance() {
        if (singleton == null)
            singleton = load();
        return singleton;
    }

    private static State load() {
        State state = new State();
        Path path = Paths.get(PermanentStorage.BASE_FOLDER);
        try {
            Files.list(path).forEach(userFolder -> {
                try {
                    User user = User.load(userFolder);
                    state.users.put(user.getName(), user);
                } catch (IOException | InvalidUsernameException | InvalidPasswordException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return state;
    }

    public User getUser(String username) throws InvalidUsernameException {
        if (!users.containsKey(username))
            throw new InvalidUsernameException();
        return users.get(username);
    }

    public User getUserOrNull(String username) {
        return users.get(username);
    }

    public Document getDocument(User requester, DocumentUri uri) throws InvalidUsernameException, DocumentNotFoundException, NotAllowedException {
        User owner = getUser(uri.owner);
        return owner.getDocument(requester, uri.docName);
    }

    public DocumentSection getDocumentSection(User requester, DocumentUri uri) throws DocumentSectionNotFoundException, InvalidUsernameException, DocumentNotFoundException, NotAllowedException {
        Document doc = getDocument(requester, uri);
        return doc.getSection(uri.section);
    }

    public User getLoggedInUser(Socket client) throws NotAllowedException {
        if (!logins.containsKey(client))
            throw new NotAllowedException();
        return logins.get(client);
    }

    public void registerUser(String username, String password) throws DuplicateUsernameException, InvalidPasswordException, InvalidUsernameException {
        User new_user = User.registerUser(username, password);
        if (users.containsKey(username)) {
            System.err.println("User " + new_user.toString() + " already exists.");
            throw new DuplicateUsernameException();
        }
        this.users.put(username, new_user);
        System.out.println("New " + new_user.toString() + " registered.");
    }

    public void login(Socket client, User user, String password) throws InvalidRequestException, InvalidPasswordException {
        user.login(password, client);
        logins.put(client, user);
    }

    public void logout(Socket client) throws InvalidRequestException {
        User loggedIn = logins.get(client);
        if (loggedIn == null)
            throw new InvalidRequestException();
        loggedIn.logout();
        logins.remove(client);
    }

    void clientDisconnected(Socket client) {
        User loggedIn = logins.get(client);
        if (loggedIn != null) {
            loggedIn.clientDisconnected();
            logins.remove(client);
        }
    }

    public void processInvites() {
        for (User user : logins.values()) {
            user.processInbox();
        }
    }

}
