package protocol.response;


public class LoginResponse extends Response {

    public String username;

    public LoginResponse(String username) {
        this.username = username;
    }

    public String toString() {
        return "User " + username + " successfully logged in.";
    }
}
