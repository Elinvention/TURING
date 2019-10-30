package protocol.response;


import client.Client;

/*
 * Una Response generata a seguito di una LoginRequest.
 */
public class LoginResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final Long sessionID;

    public LoginResponse(Long sessionID) {
        this.sessionID = sessionID;
    }

    public String toString() {
        return "Login eseguito con successo! Nuovo ID sessione: " + sessionID;
    }

    @Override
    public void process(Client client) {
        client.setSessionID(sessionID);
        System.out.println(this.toString());
    }
}
