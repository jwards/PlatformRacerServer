package gameserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import static jsward.platformracer.common.util.Constants.SERVER_PORT;

public class ServerThread extends Thread {

    private ArrayList<ClientConnection> clients;
    private ServerSocket server;

    private GameSessionManager gsm;

    private int id;

    public ServerThread() {
        gsm = new GameSessionManager();
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
                ClientConnection cc = new ClientConnection(gsm,connection, UUID.randomUUID().toString());
                cc.start();
                clients.add(cc);
                //todo check for dead connections
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
