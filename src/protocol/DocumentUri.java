package protocol;

import server.PermanentStorage;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentUri implements Serializable {
    public final String owner;
    public final String docName;
    public final Integer section;

    private static final long serialVersionUID = 1L;

    public DocumentUri(String owner, String docName, Integer section) {
        this.owner = owner;
        this.docName = docName;
        this.section = section;
    }

    public DocumentUri(String owner, String docName) {
            this(owner, docName, null);
    }

    public static DocumentUri parse(String uri) {
        String[] split = uri.split("/");
        if (split.length == 2)
            return new DocumentUri(split[0], split[1]);
        else if (split.length == 3)
            return new DocumentUri(split[0], split[1], Integer.valueOf(split[2]));
        else
            throw new IllegalArgumentException();
    }

    public DocumentUri withSection(Integer section) {
        return new DocumentUri(this.owner, this.docName, section);
    }

    public Path getPath() {
        if (section != null)
            return Paths.get(PermanentStorage.BASE_FOLDER, owner, docName, section.toString() + ".txt");
        else
            return Paths.get(PermanentStorage.BASE_FOLDER, owner, docName);
    }

    @Override
    public String toString() {
        if (section == null)
            return this.owner + "/" + this.docName;
        else
            return this.owner + "/" + this.docName + "/" + section;
    }
}
