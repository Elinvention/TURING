package protocol.response;


import client.Client;

public class LoginResponse extends Response {
    private static final long serialVersionUID = 1L;

    public final String username;
    public final Long sessionID;

    public LoginResponse(String username, Long sessionID) {
        this.username = username;
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
