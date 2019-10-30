package server;

import exceptions.*;
import protocol.DocumentUri;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Documento di Turing
 */
public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    // Uri Identificatore
    public final DocumentUri uri;
    // Proprietario del documento
    public final User owner;
    // insieme dei collaboratori del documento. Va mantenuta la coerenza con User.collaboratingOn
    private Set<User> collaborators;
    // Array di sezioni
    private DocumentSection[] sections;
    // Indirizzo multicast della chat assegnato a questo documento. Può essere null se la chat non è ancora stata aperta
    private transient InetAddress chatAddress;
    // Contatore delle sezioni bloccate
    private transient int lockedSectionsCounter = 0;

    private Document(DocumentUri uri, User owner, int sections) {
        this.uri = uri;
        this.owner = owner;
        if (!uri.owner.equals(owner.getName()))
            throw new IllegalArgumentException();
        this.collaborators = new HashSet<>();
        this.sections = new DocumentSection[sections];
    }

    // Crea un nuovo documento creando l'apposita struttura di directory.
    public static Document create(DocumentUri uri, User owner, int sections) {
        Document newDoc = new Document(uri, owner, sections);
        for (int i = 0; i < newDoc.sections.length; i++) {
            newDoc.sections[i] = new DocumentSection(uri.withSection(i));
            try {
                newDoc.sections[i].save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newDoc;
    }

    // Carica tutte le sezioni da disco
    public static Document load(DocumentUri uri, User owner, int sections) {
        Document newDoc = new Document(uri, owner, sections);
        for (int i = 0; i < newDoc.sections.length; i++) {
            newDoc.sections[i] = DocumentSection.loadOrCreate(uri.withSection(i));
        }
        return newDoc;
    }

    // Salva tutte le sezioni su discp
    public synchronized void save() throws IOException {
        for (DocumentSection section : this.sections) {
            section.save();
        }
    }

    // Carica l'insieme di collaboratori salvati su disco
    public synchronized void loadCollaborators() {
        Path collaboratorsPath = this.uri.getPath().resolve("collaborators.txt");
        try {
            this.collaborators = Files.readAllLines(collaboratorsPath, StandardCharsets.UTF_8).stream()
                    .map(c -> State.getInstance().getUserOrNull(c))
                    .collect(Collectors.toSet());
            System.out.println(this.uri + " can be accessed by " + this.collaborators);
            for (User collaborator : collaborators) {
                collaborator.queueInvite(new Invite(this, collaborator));
            }
        } catch (NoSuchFileException e) {
            try {
                Files.createFile(collaboratorsPath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized DocumentSection getSection(int section) throws DocumentSectionNotFoundException {
        try {
            return sections[section];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DocumentSectionNotFoundException();
        }
    }

    // Restituisce l'intero testo del documento concatenando il testo di tutte le sezioni.
    public synchronized String getFullText() {
        StringBuilder sb = new StringBuilder();
        for (DocumentSection s : sections) {
            sb.append(s.getText());
        }
        return sb.toString();
    }

    public String toString() {
        return "Document \"" + uri.toString() + "\"\n" + getFullText();
    }

    public String getName() {
        return this.uri.docName;
    }

    public User getOwner() {
        return this.owner;
    }

    // Restituisce true se il richiedente è abilitato
    public synchronized boolean isAllowed(User requester) {
        if (requester == this.owner)
            return true;
        return collaborators.contains(requester);
    }

    // Invita un collaboratore e lo aggiunge a collaborators.txt
    public synchronized void inviteCollaborator(User collaborator) {
        this.collaborators.add(collaborator);
        Path collaboratorsPath = uri.getPath().resolve("collaborators.txt");
        Iterable<String> collaborators = this.collaborators.stream().map(c -> c.getName()).collect(Collectors.toList());
        try {
            Files.writeString(collaboratorsPath, String.join("\r\n", collaborators), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        collaborator.queueInvite(new Invite(this, collaborator));
    }

    // Blocca la sezione specificata controllando se l'editor sta già modificando un altra sezione
    public synchronized DocumentSection lockSection(User editor, int section) throws DocumentSectionNotFoundException, DocumentSectionLockedException, UserAlreadyEditingException {
        if (editor.isEditing())
            throw new UserAlreadyEditingException("User " + editor.getName() + " is already editing " + editor.editing);
        DocumentSection documentSection = this.getSection(section);
        documentSection.setCurrentEditor(editor);
        this.owner.editing = documentSection.getUri();
        this.lockedSectionsCounter++;
        return documentSection;
    }

    // Restituisce l'IP assegnato alla chat di questo documento
    public synchronized InetAddress getChatAddress() {
        if (this.chatAddress != null)
            return this.chatAddress;
        this.chatAddress = ChatRoomAdressesManager.getInstance().openChatRoom();
        return this.chatAddress;
    }

    // Sblocca la sezione specificata dopo averne modificato il testo. Se tutte le sezioni di questo documento vengono
    // sbloccate, rilascia l'idirizzo assegnato
    public synchronized void unlockSection(User editor, String editedText, int section) throws DocumentSectionNotFoundException,
            DocumentSectionLockedException, DocumentSectionNotLockedException, IOException {
        DocumentSection documentSection = this.getSection(section);
        documentSection.setText(editor, editedText);
        documentSection.setCurrentEditor(null);
        this.owner.editing = null;
        if (this.lockedSectionsCounter > 0)
            this.lockedSectionsCounter--;
        if (this.lockedSectionsCounter == 0) {
            ChatRoomAdressesManager.getInstance().closeChatRoom(this.chatAddress);
            this.chatAddress = null;
        }
    }

    // restituisce un DocumentInfo relativo a qusto documento
    public DocumentInfo getInfo() {
        return new DocumentInfo(this.uri, this.collaborators.stream().map(User::getName).collect(Collectors.toSet()));
    }
}
