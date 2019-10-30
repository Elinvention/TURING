package protocol.request;

import exceptions.InvalidPasswordException;
import exceptions.InvalidRequestException;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import exceptions.InvalidUsernameException;
import protocol.response.LoginResponse;
import server.State;
import server.User;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


public class LoginRequest extends Request {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String toString() {
        return String.format("Login %s with password %s", this.username, this.password);
    }

    @Override
    public Response process(Socket client) {
        try {
            User user = State.getInstance().getUser(this.username);
            Long sessionID = State.getInstance().login(user, password);
            user.processInbox(client);
            return new LoginResponse(sessionID);
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            return new ExceptionResponse(e);
        }
    }
}
