import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread{


    private static final int SERVER_PORT = 14914;

    private ArrayList<ClientConnection> clients;
    private ObjectInputStream input;
    private ServerSocket server;
    private int id;

    public ServerThread() {

    }


    @Override
    public synchronized void start() {
        super.start();
        clients = new ArrayList<>();
        id = 1;

        //listen for connections
        try {
            //create server socket
            server = new ServerSocket(SERVER_PORT, 5);
            System.out.println("Server started on port: " + SERVER_PORT);

            while(true){
                Socket connection = server.accept();
                System.out.println("Client Connected: "+connection.toString());
                ClientConnection cc = new ClientConnection(connection, id);
                new Thread(cc).start();
                clients.add(cc);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
