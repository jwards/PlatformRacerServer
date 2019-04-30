package gameserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import static jsward.platformracer.common.util.Constants.SERVER_PORT;

public class ServerThread extends Thread {

    private ServerSocket server;

    private GameSessionManager gsm;

    public ServerThread() {
        gsm = new GameSessionManager();
    }


    @Override
    public void run() {
        super.run();

        //listen for connections
        try {
            //create server socket
            server = new ServerSocket(SERVER_PORT, 5);
            System.out.println("Server started on port: " + SERVER_PORT);

            while(true){
                Socket connection = server.accept();
                System.out.println("Client Connected: "+connection.toString());
                ClientConnection cc = new ClientConnection(gsm,connection);
                cc.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
