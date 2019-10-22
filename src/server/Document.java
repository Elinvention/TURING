package server;

import exceptions.DocumentSectionNotFoundException;
import protocol.DocumentUri;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Document  {
    public final DocumentUri uri;
    private final User owner;
    private Set<User> collaborators;
    private DocumentSection sections[];

    public Document(DocumentUri uri, User owner, int sections) {
        this.uri = uri;
        this.owner = owner;
        if (!uri.owner.equals(owner.getName()))
            throw new IllegalArgumentException();
        this.collaborators = new HashSet<>();
        this.sections = new DocumentSection[sections];
    }

    public static Document load(DocumentUri uri, User owner, int sections) {
        Document newDoc = new Document(uri, owner, sections);
        for(int i = 0; i < newDoc.sections.length; i++) {
            newDoc.sections[i] = DocumentSection.loadOrCreate(uri.withSection(i));
        }
        Path collaboratorsPath = uri.getPath().resolve("collaborators.txt");
        try {
            newDoc.collaborators = Files.readAllLines(collaboratorsPath, StandardCharsets.UTF_8).stream().map(c -> State.getInstance().getUserOrNull(c)).collect(Collectors.toSet());
            System.out.println(newDoc.collaborators);
        } catch (NoSuchFileException e) {
            try {
                Files.createFile(collaboratorsPath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newDoc;
    }

    public DocumentSection getSection(int section) throws DocumentSectionNotFoundException {
        try {
            return sections[section];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new DocumentSectionNotFoundException();
        }
    }

    public String getFullText() {
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

    public String getOwnerName() {
        return this.uri.owner;
    }

    public boolean isAllowed(User requester) {
        if (requester == this.owner)
            return true;
        return collaborators.contains(requester);
    }

    public void inviteCollaborator(User collaborator) {
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
}
