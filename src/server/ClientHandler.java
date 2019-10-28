package server;

import protocol.Message;
import protocol.request.Request;
import protocol.response.ExceptionResponse;
import protocol.response.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private Socket client;

    public ClientHandler(Socket client) {
        this.client = client;
    }

    private void trySendExceptionResponse(Exception e) {
        Response response = new ExceptionResponse(e);
        try {
            response.send(client);
        } catch (IOException ex) {
            System.err.println("Could not inform client about error.");
        }
    }

    private void clientDisconnect() {
        try {
            System.out.println("Client disconnected " + client.getRemoteSocketAddress().toString());
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Socket getClient() {
        return client;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    Request request = (Request) Message.receive(client);
                    System.out.println(request.getClass().getSimpleName() + " received: " + request.toString());
                    Response response = request.process(client);
                    response.send(client);
                } catch (ClassNotFoundException | InvalidClassException e) {
                    System.err.println("Dropping unknown packet received from " + client.getRemoteSocketAddress().toString() + ".");
                    trySendExceptionResponse(e);
                }
            }
        } catch (EOFException e) {

        } catch (Exception e) {
            e.printStackTrace();
            trySendExceptionResponse(e);
        } finally {
            clientDisconnect();
        }
    }
}
