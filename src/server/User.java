package server;

import exceptions.*;
import protocol.DocumentUri;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Rappresenta un utente registrato al servizio Turing
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // nome dell'utente
    public final String name;
    // hash della password dell'utente
    public final String hashedPassword;
    // identifiacatore della sezione bloccata dall'utente
    public DocumentUri editing;
    // Mappa il nome del documento con il Document corrispondente
    private transient Map<String, Document> documents;
    // Insieme di documenti su cui l'utente sta collaborando. Va mantenuta la consistenza con Document.collaborators
    private transient Set<Document> collaboratingOn;
    // Coda di inviti pendenti non ancora notificati all'utente perché non è online
    private transient Queue<Invite> inviteInbox;

    // Costruttore che pone dei controlli di validità della password e del nome utente
    public User(String name, String hashedPassword) throws InvalidUsernameException, InvalidPasswordException {
        if (name == null || hashedPassword == null) {
            throw new NullPointerException();
        }
        if (name.length() < 5) {
            throw new InvalidUsernameException();
        }
        if (hashedPassword.length() < 8) {
            throw new InvalidPasswordException();
        }
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.documents = new HashMap<>();
        this.collaboratingOn = new HashSet<>();
        this.inviteInbox = new ArrayDeque<>();
    }

    // Registra un nuovo utente sul server creando la struttura di directory corrispondente
    public static User registerUser(String name, String password) throws InvalidPasswordException, InvalidUsernameException, InvalidKeySpecException, NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password);
        User newUser = new User(name, hashedPassword);
        try {
            Path path = Paths.get(PermanentStorage.BASE_FOLDER, name);
            Files.createDirectories(path);
            Files.writeString(path.resolve("password.txt"), hashedPassword, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.out.println("Could not create user data: " + e.getMessage());
        }
        return newUser;
    }

    // Genera il sale, un numero casuale crittograficamente sicuro da utilizzare per creare l'hash della password
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        return salt;
    }

    // genera l'hash della password, una stringa separata da ":", che è composta da:
    // - numero di iterazioni dell'algoritmo PBKDF2
    // - sale codificato in Base64
    // - hash della password e del sale calcolato dal PBKDF2 e codificato in Base64
    private static String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = generateSalt();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();

        String encodedSalt = new String(Base64.getEncoder().encode(salt));
        String encodedHash = new String(Base64.getEncoder().encode(hash));
        return iterations + ":" + encodedSalt + ":" + encodedHash;
    }

    // Controllo corrispondenza password, mediante confronto dell'hash
    private boolean checkPassword(String originalPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] splitted = this.hashedPassword.split(":");
        int iterations = Integer.parseInt(splitted[0]);
        byte[] storedSalt = Base64.getDecoder().decode(splitted[1].getBytes());
        byte[] storedHash = Base64.getDecoder().decode(splitted[2].getBytes());

        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), storedSalt, iterations, storedHash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        // slowEquals, impiega sempre lo stesso tempo, sia quando i 2 hash sono uguali, sia quando sono diversi
        // evita timing attack
        int diff = storedHash.length ^ testHash.length;
        for (int i = 0; i < storedHash.length && i < testHash.length; i++) {
            diff |= storedHash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    // carica la password e tutti i documenti dell'utente da disco
    public static User load(Path userFolder) throws IOException, InvalidPasswordException, InvalidUsernameException {
        String username = userFolder.getFileName().toString();
        System.out.println("Loading user " + username);
        String hashedPassword = Files.readString(userFolder.resolve("password.txt"));
        User user = new User(username, hashedPassword);
        user.loadDocuments(userFolder);
        return user;
    }

    // Carica i documenti dell'utente da disco
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
        return String.format("User \"%s\": %d documents owned, %d collaborations.", this.name, this.documents.size(), this.collaboratingOn.size());
    }

    public String getName() {
        return this.name;
    }

    // restituisce la lista dei documenti posseduti dall'utente
    public synchronized List<Document> getOwnedDocuments() {
        return new ArrayList<>(this.documents.values());
    }

    // restituisce la lista di DocumentInfo relativi ai documenti posseduti dall'utente e quelli su cui sta collaborando
    public synchronized List<DocumentInfo> listDocumentInfos() {
        Stream<Document> allDocuments = Stream.concat(this.documents.values().stream(), this.collaboratingOn.stream());
        return allDocuments.map(doc -> doc.getInfo()).collect(Collectors.toList());
    }

    // Controllo password al momento del login
    public void login(String password) throws InvalidPasswordException, InvalidKeySpecException, NoSuchAlgorithmException {
        if (!this.checkPassword(password))
            throw new InvalidPasswordException();
    }

    // Svuota la coda di notifiche pendenti inviando ogni invito
    public synchronized void processInbox(Socket client) {
        while (!inviteInbox.isEmpty()) {
            Invite invite = inviteInbox.remove();
            try {
                invite.send(client);
            } catch (IOException | NullPointerException e) {
                System.err.println("Failed to send " + invite.toString());
            }
        }
    }

    // Accoda un invito e aggiunge il documento all'insieme dei documenti su cui si sta collaborando
    public void queueInvite(Invite invite) {
        this.inviteInbox.add(invite);
        this.collaboratingOn.add(invite.document);
    }

    // Crea un documento controllando che non sia già presente
    public synchronized Document createDocument(String docName, int sections) throws DuplicateDocumentException {
        if (documents.containsKey(docName)) {
            throw new DuplicateDocumentException();
        }
        Document doc = Document.create(new DocumentUri(this.name, docName), this, sections);
        this.documents.put(docName, doc);
        return doc;
    }

    // Restituisce un Document a partire dal suo nome e dal richiedente. Il richiedente serve per verificare che abbia i
    // permessi necessari
    public synchronized Document getDocument(User requester, String name) throws DocumentNotFoundException, NotAllowedException {
        if (!documents.containsKey(name))
            throw new DocumentNotFoundException();
        Document doc = documents.get(name);
        if (!doc.isAllowed(requester))
            throw new NotAllowedException();
        return doc;
    }

    // restituisce true se l'utente sta modificando una sessione
    public synchronized boolean isEditing() {
        return this.editing != null;
    }
}
