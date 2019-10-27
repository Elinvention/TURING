package server;

import exceptions.DocumentSectionLockedException;
import exceptions.DocumentSectionNotLockedException;
import protocol.DocumentUri;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class DocumentSection implements Serializable {
    private String text = "";
    private User currentEditor;
    private DocumentUri uri;

    public DocumentSection(DocumentUri uri) {
        if (uri.section == null)
            throw new IllegalArgumentException();
        this.uri = uri;
    }

    public String getText() {
        return text;
    }

    public DocumentUri getUri() {
        return this.uri;
    }

    public synchronized void setText(User editor, String text) throws DocumentSectionLockedException, DocumentSectionNotLockedException {
        if (currentEditor == null)
            throw new DocumentSectionNotLockedException();
        if (editor != currentEditor)
            throw new DocumentSectionLockedException();
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        this.save();
    }

    public User getCurrentEditor() {
        return currentEditor;
    }

    public synchronized void setCurrentEditor(User currentEditor) throws DocumentSectionLockedException {
        if (this.currentEditor == null)
            this.currentEditor = currentEditor;
        else
            if (currentEditor == null)
                this.currentEditor = null;
            else
                throw new DocumentSectionLockedException();
    }

    public boolean isLocked() {
        return currentEditor != null;
    }

    @Override
    public String toString() {
        if (currentEditor != null)
            return "Section " + uri.section + " locked by " + currentEditor.getName() + "\n" + text;
        else
            return "Section " + uri.section + " is not locked\n" + text;
    }

    public void save() {
        Path path = this.uri.getPath();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Files.writeString(path, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    public static DocumentSection load(DocumentUri uri) throws IOException {
        Path path = uri.getPath();
        String text = Files.readString(path, StandardCharsets.UTF_8);
        DocumentSection newSection = new DocumentSection(uri);
        newSection.text = text;
        return newSection;
    }

    public static DocumentSection loadOrCreate(DocumentUri uri) {
        try {
            return load(uri);
        } catch (IOException e) {
            Path path = uri.getPath();
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            return new DocumentSection(uri);
        }
    }
}
