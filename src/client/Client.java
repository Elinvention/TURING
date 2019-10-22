package client;

import protocol.DocumentUri;
import protocol.request.*;
import protocol.Message;
import protocol.response.ExceptionResponse;
import protocol.response.InviteNotification;
import protocol.response.Response;
import server.Server;

import java.io.IOException;
import java.net.*;



public class Client {

    Socket socket = new Socket();

    public Client() {
        SocketAddress addr = null;
        try {
            addr = new InetSocketAddress(InetAddress.getLocalHost(), Server.PORT);
            socket.connect(addr);
            socket.setSoTimeout(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String username, String password) throws IOException, ClassNotFoundException {
        System.out.println("Logging in user " + username + " and password " + password + ".");
        LoginRequest msg = new LoginRequest(username, password);
        msg.send(socket);
        receiveResponse();
    }

    public void logout() throws IOException, ClassNotFoundException {
        LogoutRequest req = new LogoutRequest();
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void createDocument(String docName, int sections) throws IOException, ClassNotFoundException {
        CreateDocumentRequest req = new CreateDocumentRequest(docName, sections);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void showDocument(DocumentUri uri) throws IOException, ClassNotFoundException {
        ShowDocumentRequest req = new ShowDocumentRequest(uri);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void showDocumentSection(DocumentUri uri) throws IOException, ClassNotFoundException {
        ShowDocumentSectionRequest req = new ShowDocumentSectionRequest(uri);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void editDocument(DocumentUri uri) throws IOException, ClassNotFoundException {
        EditRequest req = new EditRequest(uri);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void endEditDocument(DocumentUri uri, String editedText) throws IOException, ClassNotFoundException {
        EndEditRequest req = new EndEditRequest(uri, editedText);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void inviteCollaborator(String docName, String username) throws IOException, ClassNotFoundException {
        InviteCollaboratorRequest req = new InviteCollaboratorRequest(docName, username);
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void listDocuments() throws IOException, ClassNotFoundException {
        ListDocumentsRequest req = new ListDocumentsRequest();
        System.out.println(req.toString());
        req.send(socket);
        receiveResponse();
    }

    public void receiveResponse() throws IOException, ClassNotFoundException {
        Response response = (Response) Message.receive(socket);
        while (response instanceof InviteNotification) {
            System.out.println(response);
            response = (Response) Message.receive(socket);
        }
        printResponse(response);
    }

    public static void printResponse(Response response) {
        if (response instanceof ExceptionResponse) {
            System.err.println(response.toString());
        } else {
            System.out.println(response.toString());
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Client c = new Client();
        Client d = new Client();
        try {
            // test login when user does not exist
            c.login("test1", "testpwd1");
            // test register user
            RmiRegisterUserClient.registerUser("test1", "testpwd1");
            RmiRegisterUserClient.registerUser("test2", "testpwd2");
            // test login with wrong password
            c.login("test1", "testpwd2");
            // test correct login
            c.login("test1", "testpwd1");

            // test logout when not logged in
            c.logout();
            c.logout();

            // test create document
            c.createDocument("testdoc", 10);

            c.login("test1", "testpwd1");
            c.createDocument("testdoc", 10);
            c.createDocument("testdoc", 10);
            c.logout();

            c.createDocument("testdoc", 10);

            c.login("test1", "testpwd1");
            for (int i = 0; i < 10; i++) {
                c.createDocument("doc" + i, 1);
            }
            c.listDocuments();
            c.showDocument(new DocumentUri("test1", "troll"));
            c.showDocument(new DocumentUri("troll", "troll"));
            c.showDocument(new DocumentUri("test1", "testdoc"));
            c.endEditDocument(new DocumentUri("test1", "testdoc", 3), "lol\n");
            c.showDocument(new DocumentUri("test1", "testdoc"));

            d.login("test2", "testpwd2");
            d.listDocuments();
            d.showDocument(new DocumentUri("test1", "testdoc"));
            d.logout();
            c.inviteCollaborator("wrongDoc", "test2");
            c.inviteCollaborator("testdoc", "wrongUser");
            c.inviteCollaborator("testdoc", "test2");
            for (int i = 0; i < 10; i++) {
                c.inviteCollaborator("doc" + i, "test2");
            }
            d.login("test2", "testpwd2");
            d.listDocuments();
            d.showDocument(new DocumentUri("test1", "testdoc"));

            for (int i = 0; i < 10; i++) {
                DocumentUri uri = new DocumentUri("test1", "testdoc", i);
                c.editDocument(uri);
                c.showDocumentSection(uri);
                d.editDocument(uri);
                c.endEditDocument(uri, String.valueOf(i) + "\n");
                c.showDocumentSection(uri);
            }
            c.showDocument(new DocumentUri("test1", "testdoc"));
            c.logout();

        } catch (UnknownHostException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
            System.exit(-2);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-3);
        } finally {
            c.close();
            d.close();
        }
    }
}
