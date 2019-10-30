package server;

import exceptions.*;
import protocol.DocumentUri;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;


/*
 * Classe che mantiene lo stato globale del server in un singleton.
 */
public class State {
    private static State singleton;
    private static SecureRandom csrng = new SecureRandom();

    // mappa nome utente in User
    private final Map<String, User> users = new HashMap<>();
    // mappa sessionID (long) in User ('utente che ha aperto quella sessione effettuando il login)
    private final Map<Long, User> activeLoginSessions = new HashMap<>();

    private State() {

    }

    // implementazione singleton
    public static State getInstance() {
        if (singleton == null)
            singleton = load();
        return singleton;
    }

    // carica lo stato persistente salvato sul disco e inizializza il singleton
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

    // restituisce un User a partire dal suo username, lanciando una InvalidUsernameException in caso l'utente non esista
    public synchronized User getUser(String username) throws InvalidUsernameException {
        if (!users.containsKey(username))
            throw new InvalidUsernameException();
        return users.get(username);
    }

    // restituisce un User a partire dal suo username se esiste, altrimenti null
    public synchronized User getUserOrNull(String username) {
        return users.get(username);
    }

    // restituisce un Document a partire da un DocumentUri.
    // vengono effettuati controlli di:
    // - esistenza del proprietario del documento (viene lanciato InvalidUsernameException in caso contrario)
    // - esistenza del documento (viene lanciato DocumentNotFoundException in caso contrario)
    // - permessi dell'utente (viene lanciato NotAllowedException in caso contrario)
    public synchronized Document getDocument(User requester, DocumentUri uri) throws InvalidUsernameException, DocumentNotFoundException, NotAllowedException {
        User owner = getUser(uri.owner);
        return owner.getDocument(requester, uri.docName);
    }

    // restituisce un Document a partire da un DocumentUri.
    // vengono effettuati controlli di:
    // - esistenza del proprietario del documento (viene lanciato InvalidUsernameException in caso contrario)
    // - esistenza del documento (viene lanciato DocumentNotFoundException in caso contrario)
    // - esistenza della sezione (viene lanciato DocumentSectionNotFoundException in caso contrario)
    // - permessi dell'utente (viene lanciato NotAllowedException in caso contrario)
    public synchronized DocumentSection getDocumentSection(User requester, DocumentUri uri) throws DocumentSectionNotFoundException, InvalidUsernameException, DocumentNotFoundException, NotAllowedException {
        Document doc = getDocument(requester, uri);
        return doc.getSection(uri.section);
    }

    // restituisce l'utente che ha avviato la sessione data come parametro
    // In caso la sessione non sia valida viene lanciato un InvalidSessionException
    public synchronized User getUserFromSession(Long sessionID) throws InvalidSessionException {
        if (!activeLoginSessions.containsKey(sessionID))
            throw new InvalidSessionException("Session " + sessionID + " is not a valid session. Please login again.");
        return activeLoginSessions.get(sessionID);
    }

    // registra un utente al servizio
    // DuplicateUsernameException -> nome utente già utilizzato
    // InvalidPasswordException   -> password non valida (troppo corta)
    // InvalidUsernameException   -> username non valido (troppo corto)
    // InvalidKeySpecException    -> eccezione lanciata dalla implementazione della JVM in uso
    // NoSuchAlgorithmException   -> eccezione lanciata dalla implementazione della JVM in uso
    public synchronized void registerUser(String username, String password) throws DuplicateUsernameException,
            InvalidPasswordException, InvalidUsernameException, InvalidKeySpecException, NoSuchAlgorithmException {
        User new_user = User.registerUser(username, password);
        if (users.containsKey(username)) {
            System.err.println("User " + new_user.toString() + " already exists.");
            throw new DuplicateUsernameException();
        }
        this.users.put(username, new_user);
        System.out.println("New " + new_user.toString() + " registered.");
    }

    // genera un ID sessione a caso. Probabilità di una collisione: #Sessioni / 2 ^ 64 (3 E38), cioè quasi impossibile
    private static Long generateSessionID() {
        return csrng.nextLong();
    }

    // esegue il login dell'utente, controllando nome utente e password e generando una nuova sessione
    // Fallisce in caso la password non sia valida lanciando un InvalidPasswordException
    // InvalidKeySpecException e NoSuchAlgorithmException vengono lanciate dal codice che controlla la password e
    // dipendono dalla corrente implementazione della JVM
    public synchronized Long login(User user, String password) throws InvalidPasswordException,
            InvalidKeySpecException, NoSuchAlgorithmException {
        user.login(password);
        Long sessionID = generateSessionID();
        synchronized (this.activeLoginSessions) {
            this.activeLoginSessions.put(sessionID, user);
        }
        return sessionID;
    }

    // essegue il logout invalidando il sessionID.
    public synchronized void logout(Long sessionID) throws InvalidRequestException {
        synchronized (this.activeLoginSessions) {
            User loggedIn = this.activeLoginSessions.get(sessionID);
            if (loggedIn == null)
                throw new InvalidRequestException();
            activeLoginSessions.remove(sessionID);
        }
    }
}
