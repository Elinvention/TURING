package protocol.request;

import exceptions.InvalidRequestException;
import protocol.response.AckResponse;
import protocol.response.ExceptionResponse;
import protocol.response.Response;
import server.State;

import java.net.Socket;


public class LogoutRequest extends Request {
    @Override
    public Response process(Socket client) {
        try {
            State.getInstance().logout(client);
        } catch (InvalidRequestException e) {
            return new ExceptionResponse(e);
        }
        return new AckResponse(this);
    }

    @Override
    public String toString() {
        return "Logout Request.";
    }
}
