package server;

import protocol.RmiRegisterUser;
import exceptions.DuplicateUsernameException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUsernameException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiRegisterUserServer extends UnicastRemoteObject implements RmiRegisterUser {

    private State state;

    protected RmiRegisterUserServer(State state) throws RemoteException {
        super();
        this.state = state;
    }

    @Override
    public void registerUser(String username, String password) throws InvalidUsernameException, InvalidPasswordException, DuplicateUsernameException {
        this.state.registerUser(username, password);
    }

}
