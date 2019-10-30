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

/*
 * Sezione di un documento Turing
 */
public class DocumentSection implements Serializable {
    private static final long serialVersionUID = 1L;

    // testo della sezione
    private String text = "";
    // utente che sta bloccando la sezione
    private User currentEditor;
    // identificatore della sezione
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

    // imposta il testo della sezione e controlla che il richiedente abbia i permessi necessari
    // Lancia DocumentSectionNotLockedException se la sezione non è stata bloccata prima di essere modificata
    // Lancia DocumentSectionLockedException se la sezione è già stata bloccata da un'altro utente
    // Lancia IOException se fallisce il salvataggio su disco della sezione
    public synchronized void setText(User editor, String text) throws DocumentSectionLockedException, DocumentSectionNotLockedException, IOException {
        if (currentEditor == null)
            throw new DocumentSectionNotLockedException();
        if (editor != currentEditor)
            throw new DocumentSectionLockedException();
        if (text == null)
            throw new NullPointerException();
        this.text = text;
        this.save();
    }

    public synchronized User getCurrentEditor() {
        return currentEditor;
    }

    // Imposta l'editor corrente che blocca quasta sezione
    public synchronized void setCurrentEditor(User currentEditor) throws DocumentSectionLockedException {
        if (this.currentEditor == null)
            this.currentEditor = currentEditor;
        else
            if (currentEditor == null)
                this.currentEditor = null;
            else
                throw new DocumentSectionLockedException();
    }

    // restituisce true se la sezione è bloccata
    public synchronized boolean isLocked() {
        return currentEditor != null;
    }

    @Override
    public String toString() {
        if (currentEditor != null)
            return "Section " + uri.section + " locked by " + currentEditor.getName() + "\n" + text;
        else
            return "Section " + uri.section + " is not locked\n" + text;
    }

    // Salva su disco la sezione
    public synchronized void save() throws IOException {
        Path path = this.uri.getPath();
        Files.createDirectories(path.getParent());
        Files.writeString(path, text, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Carica da disco la sezione
    public static DocumentSection load(DocumentUri uri) throws IOException {
        Path path = uri.getPath();
        String text = Files.readString(path, StandardCharsets.UTF_8);
        DocumentSection newSection = new DocumentSection(uri);
        newSection.text = text;
        return newSection;
    }

    // Tenta di caricare la sezione da disco, se fallisce ne crea una nuova.
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
