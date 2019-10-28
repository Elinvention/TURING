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

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    public final DocumentUri uri;
    public final User owner;
    private Set<User> collaborators;
    private DocumentSection[] sections;
    private transient InetAddress chatAddress;
    private transient int lockedSectionsCounter = 0;

    private Document(DocumentUri uri, User owner, int sections) {
        this.uri = uri;
        this.owner = owner;
        if (!uri.owner.equals(owner.getName()))
            throw new IllegalArgumentException();
        this.collaborators = new HashSet<>();
        this.sections = new DocumentSection[sections];
    }

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

    public static Document load(DocumentUri uri, User owner, int sections) {
        Document newDoc = new Document(uri, owner, sections);
        for (int i = 0; i < newDoc.sections.length; i++) {
            newDoc.sections[i] = DocumentSection.loadOrCreate(uri.withSection(i));
        }
        return newDoc;
    }

    public synchronized void save() throws IOException {
        for (DocumentSection section : this.sections) {
            section.save();
        }
    }

    public synchronized void loadCollaborators() {
        Path collaboratorsPath = this.uri.getPath().resolve("collaborators.txt");
        try {
            this.collaborators = Files.readAllLines(collaboratorsPath, StandardCharsets.UTF_8).stream()
                    .map(c -> State.getInstance().getUserOrNull(c))
                    .collect(Collectors.toSet());
            System.out.println(this.uri + " can be accessed by " + this.collaborators);
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

    public synchronized boolean isAllowed(User requester) {
        if (requester == this.owner)
            return true;
        return collaborators.contains(requester);
    }

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

    public synchronized DocumentSection lockSection(User editor, int section) throws DocumentSectionNotFoundException, DocumentSectionLockedException, UserAlreadyEditingException {
        if (editor.isEditing())
            throw new UserAlreadyEditingException("User " + editor.getName() + " is already editing " + editor.editing);
        DocumentSection documentSection = this.getSection(section);
        documentSection.setCurrentEditor(editor);
        this.owner.editing = documentSection.getUri();
        this.lockedSectionsCounter++;
        return documentSection;
    }

    public synchronized InetAddress getChatAddress() {
        if (this.chatAddress != null)
            return this.chatAddress;
        this.chatAddress = ChatRoomAdressesManager.getInstance().openChatRoom();
        return this.chatAddress;
    }

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

    public DocumentInfo getInfo() {
        return new DocumentInfo(this.uri, this.collaborators.stream().map(User::getName).collect(Collectors.toSet()));
    }
}
