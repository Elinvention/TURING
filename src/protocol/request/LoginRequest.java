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


public class LoginRequest extends Request {
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
            State.getInstance().login(client, user, password);
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRequestException e) {
            return new ExceptionResponse(e);
        }
        return new LoginResponse(username);
    }
}
