package server;

import exceptions.*;
import protocol.DocumentUri;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class State {
    private static State singleton;
    private static SecureRandom csrng = new SecureRandom();

    private final Map<String, User> users = new HashMap<>();
    private final Map<Long, User> activeLoginSessions = new HashMap<>();

    private State() {

    }

    public static State getInstance() {
        if (singleton == null)
            singleton = load();
        return singleton;
    }

    private static State load() {
        singleton = new State();
        Path path = Paths.get(PermanentStorage.BASE_FOLDER);
        try {
            if (Files.notExists(path))
                Files.createDirectories(path);
            Files.list(path).forEach(userFolder -> {
                try {
                    User user = User.load(userFolder);
                    singleton.users.put(user.getName(), user);
                } catch (IOException | InvalidUsernameException | InvalidPasswordException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Collaborators can be loaded only after all users have been loaded
        for (User u : singleton.users.values())
            for (Document d : u.getOwnedDocuments())
                d.loadCollaborators();
        return singleton;
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

    public User getUserFromSession(Long sessionID) throws InvalidSessionException {
        if (!activeLoginSessions.containsKey(sessionID))
            throw new InvalidSessionException("Session " + sessionID + " is not a valid session. Please login again.");
        return activeLoginSessions.get(sessionID);
    }

    public void registerUser(String username, String password) throws DuplicateUsernameException, InvalidPasswordException, InvalidUsernameException, InvalidKeySpecException, NoSuchAlgorithmException {
        User new_user = User.registerUser(username, password);
        if (users.containsKey(username)) {
            System.err.println("User " + new_user.toString() + " already exists.");
            throw new DuplicateUsernameException();
        }
        this.users.put(username, new_user);
        System.out.println("New " + new_user.toString() + " registered.");
    }

    private static Long generateSessionID() {
        return csrng.nextLong();
    }

    public Long login(Socket client, User user, String password) throws InvalidRequestException, InvalidPasswordException, InvalidKeySpecException, NoSuchAlgorithmException {
        user.login(password, client);
        Long sessionID = generateSessionID();
        synchronized (this.activeLoginSessions) {
            this.activeLoginSessions.put(sessionID, user);
        }
        return sessionID;
    }

    public void logout(Long sessionID) throws InvalidRequestException {
        synchronized (this.activeLoginSessions) {
            User loggedIn = this.activeLoginSessions.get(sessionID);
            if (loggedIn == null)
                throw new InvalidRequestException();
            activeLoginSessions.remove(sessionID);
        }
    }

    public void processInvites(Socket client) {
        for (User user : this.activeLoginSessions.values()) {
            user.processInbox(client);
        }
    }
}
