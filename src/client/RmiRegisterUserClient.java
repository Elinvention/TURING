package client;

import exceptions.DuplicateUsernameException;
import exceptions.InvalidPasswordException;
import exceptions.InvalidUsernameException;
import protocol.RmiRegisterUser;
import server.Server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class RmiRegisterUserClient {
    public static void registerUser(String ip, String username, String password) {
        try {
            Registry rmiRegistry = LocateRegistry.getRegistry(ip, Server.RMI_PORT);
            RmiRegisterUser server = (RmiRegisterUser) rmiRegistry.lookup(RmiRegisterUser.registryBindName);
            server.registerUser(username, password);
            System.out.println("New user '" + username + "' successfully registered with password '" + password + "'.");
        } catch (DuplicateUsernameException e) {
            System.err.println("Username '" + username + "' già esistente.");
        } catch (InvalidUsernameException e) {
            System.err.println("Invalid username '" + username + "'.");
        } catch (InvalidPasswordException e) {
            System.err.println("Invalid password '" + password + "'.");
        } catch (RemoteException | NotBoundException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
