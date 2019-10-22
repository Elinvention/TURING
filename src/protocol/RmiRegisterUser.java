package protocol;

import exceptions.DuplicateUsernameException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUsernameException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiRegisterUser extends Remote {
    String registryBindName = "turing/register";

    void registerUser(String username, String password) throws RemoteException, InvalidUsernameException, InvalidPasswordException, DuplicateUsernameException;
}
