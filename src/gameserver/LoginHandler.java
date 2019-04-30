package gameserver;

import jsward.platformracer.common.network.LoginPacket;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;

public class LoginHandler extends Thread {

    private SSLSocket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream objectOutputStream;

    private HashMap<String, String> users;

    public LoginHandler(SSLSocket socket, HashMap<String, String> users) throws IOException {
        this.users = users;
        this.socket = socket;
        inputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.flush();
    }

    @Override
    public void run() {
        super.run();

        try {

            // Start handshake
            socket.startHandshake();

            // Get session after the connection is established
            SSLSession sslSession = socket.getSession();

            System.out.println("SSLSession :");
            System.out.println("\tProtocol : "+sslSession.getProtocol());
            System.out.println("\tCipher suite : "+sslSession.getCipherSuite());

            LoginPacket packet = (LoginPacket) inputStream.readUnshared();

            if (users.containsKey(packet.getUsername())) {
                if (users.get(packet.getUsername()).equals(packet.getPassword())) {
                    System.out.println("Found User");
                    objectOutputStream.writeUnshared(new LoginPacket(UUID.randomUUID().toString(), false));
                }
            } else {
                System.out.println("Creating new users");
                users.put(packet.getUsername(), packet.getPassword());
                objectOutputStream.writeUnshared(new LoginPacket(UUID.randomUUID().toString(), true));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try{
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
