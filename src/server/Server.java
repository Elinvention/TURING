package server;

import protocol.RmiRegisterUser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
 * Classe principale del server
 */
public class Server {
    // Porta del server TCP
    public static final int PORT = 2000;
    // Porta del servizio di registrazione RMI
    public static final int RMI_PORT = 3000;
    // Numero di threads nel pool.
    public static final int NTHREADS = 16;

    private static State state = State.getInstance();
    private static ExecutorService es = Executors.newFixedThreadPool(NTHREADS);

    // avvia server RMI
    public static void startRmiServer() {
        System.out.println("Starting Java RMI registry.");
        try {
            LocateRegistry.createRegistry(RMI_PORT);
            System.out.println("Java RMI registry created.");
            System.out.println("Starting RegisterUser service.");
            RmiRegisterUserServer service = new RmiRegisterUserServer(state);
            Registry registry = LocateRegistry.getRegistry(RMI_PORT);
            registry.bind(RmiRegisterUser.registryBindName, service);
            System.out.println("RegisterUser service started.");
        } catch (RemoteException e) {
            //do nothing, error means registry already exists
            System.err.println("Remote exception.");
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            System.err.println("Port " + RMI_PORT + " is already bound.");
        }
    }

    // loop del server TCP
    public static void serverLoop() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket client = server.accept();
                System.out.println("Client connected " + client.getRemoteSocketAddress().toString());
                ClientHandler handler = new ClientHandler(client);
                es.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String args[]) {
        startRmiServer();
        serverLoop();
    }
}
