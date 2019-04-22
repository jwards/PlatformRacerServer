package gameserver;

import jsward.platformracer.common.game.PlayerController;
import jsward.platformracer.common.network.GameInputPacket;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import static jsward.platformracer.common.util.Constants.CLIENT_INPUT_POLL_RATE;

public class ClientInputReciever extends TickerThread {

    private Socket socket;
    private ObjectInputStream input;

    private PlayerController playerController;

    public ClientInputReciever(Socket socket,ObjectInputStream objectInputStream, PlayerController playerController) throws IOException {
        super(CLIENT_INPUT_POLL_RATE, false, null);
        this.socket = socket;
        this.input = objectInputStream;
        this.playerController = playerController;
    }


    @Override
    protected void tick() {
        GameInputPacket gup;
        try {
            Object obj = input.readObject();
            if(obj != null){
                if (obj instanceof GameInputPacket) {
                    gup = (GameInputPacket) obj;
                    playerController.setControlsActive(gup.getControls());
                    System.out.println("Received: " + gup.toString());
                } else {
                    System.out.println("Error: Expected GameInputPacket, received "+obj.getClass()+" on socket "+socket.toString());
                }
            } else {
                System.out.println("Error: Expected GameInputPacket, received null on socket " + socket.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            hault();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            hault();
        }
    }
}
