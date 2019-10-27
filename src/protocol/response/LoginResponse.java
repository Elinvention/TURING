package protocol.response;


public class LoginResponse extends Response {
    public final String username;
    public final Long sessionID;

    public LoginResponse(String username, Long sessionID) {
        this.username = username;
        this.sessionID = sessionID;
    }

    public String toString() {
        return "User " + username + " successfully logged in. New session: " + sessionID;
    }
}
