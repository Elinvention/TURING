package protocol;

import server.PermanentStorage;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * Identificatore univoco di un Document o di una DocumentSection
 */
public class DocumentUri implements Serializable {
    private static final long serialVersionUID = 1L;

    // Nome utente del possessore del documento
    public final String owner;
    // Nome del documento
    public final String docName;
    // numero di sezione. Null se questo DocumentUri non si riferisce ad una DocumentSection
    public final Integer section;

    public DocumentUri(String owner, String docName, Integer section) {
        this.owner = owner;
        this.docName = docName;
        this.section = section;
    }

    public DocumentUri(String owner, String docName) {
            this(owner, docName, null);
    }

    // Da stringa a DocumentUri
    public static DocumentUri parse(String uri) {
        String[] split = uri.split("/");
        if (split.length == 2)
            return new DocumentUri(split[0], split[1]);
        else if (split.length == 3)
            return new DocumentUri(split[0], split[1], Integer.valueOf(split[2]));
        else
            throw new IllegalArgumentException();
    }

    // Restituisce una nuova DocumentUri che ha una sezione diversa da quella di partenza
    public DocumentUri withSection(Integer section) {
        return new DocumentUri(this.owner, this.docName, section);
    }

    // Restituisce il Path in cui verrà salvata o caricata la sezione o il documento
    public Path getPath() {
        if (section != null)
            return Paths.get(PermanentStorage.BASE_FOLDER, owner, docName, section.toString() + ".txt");
        else
            return Paths.get(PermanentStorage.BASE_FOLDER, owner, docName);
    }

    // Da DocumentUri a String
    @Override
    public String toString() {
        if (section == null)
            return this.owner + "/" + this.docName;
        else
            return this.owner + "/" + this.docName + "/" + section;
    }
}
