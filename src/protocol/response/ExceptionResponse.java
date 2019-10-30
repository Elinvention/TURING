package protocol.response;

import client.Client;

/*
 * Risposta generica di errore. Il server ha generato una eccezione durante l'esecuzione di una richiesta.
 */
public class ExceptionResponse extends Response {
    private static final long serialVersionUID = 1L;

    // Eccezione generata dal server
    public Exception ex;

    public ExceptionResponse(Exception ex) {
        this.ex = ex;
    }

    @Override
    public String toString() {
        return "Il server ha riportato un errore: " + ex.toString();
    }

    @Override
    public void process(Client client) {
        System.err.println(this.toString());
    }
}
