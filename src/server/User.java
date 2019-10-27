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


public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String hashedPassword;
    private transient Map<String, Document> documents;
    private transient Set<Document> collaboratingOn;
    private transient Queue<Invite> inviteInbox;

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

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[64];
        random.nextBytes(salt);
        return salt;
    }

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

    public static User load(Path userFolder) throws IOException, InvalidPasswordException, InvalidUsernameException {
        String username = userFolder.getFileName().toString();
        System.out.println("Loading user " + username);
        String hashedPassword = Files.readString(userFolder.resolve("password.txt"));
        User user = new User(username, hashedPassword);
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
        return String.format("User \"%s\" with password \"%s\"", this.name, this.hashedPassword);
    }

    public String getName() {
        return this.name;
    }

    public List<Document> getOwnedDocuments() {
        return new ArrayList<>(this.documents.values());
    }

    public List<DocumentUri> listDocumentUris() {
        Stream<Document> allDocuments = Stream.concat(this.documents.values().stream(), this.collaboratingOn.stream());
        return allDocuments.map(d -> d.uri).collect(Collectors.toList());
    }

    public void login(String password, Socket client) throws InvalidPasswordException, InvalidKeySpecException, NoSuchAlgorithmException {
        if (!this.checkPassword(password))
            throw new InvalidPasswordException();
        this.processInbox(client);
    }

    public void processInbox(Socket client) {
        while (!inviteInbox.isEmpty()) {
            Invite invite = inviteInbox.remove();
            try {
                invite.send(client);
            } catch (IOException | NullPointerException e) {
                System.err.println("Failed to send " + invite.toString());
            }
        }
    }

    public void queueInvite(Invite invite) {
        this.inviteInbox.add(invite);
        this.collaboratingOn.add(invite.document);
    }

    public Document createDocument(String docName, int sections) throws DuplicateDocumentException {
        if (documents.containsKey(docName)) {
            throw new DuplicateDocumentException();
        }
        Document doc = Document.create(new DocumentUri(this.name, docName), this, sections);
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
