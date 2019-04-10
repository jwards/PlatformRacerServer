import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.network.GameUpdatePacket;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static jsward.platformracer.common.util.Constants.SERVER_UPDATE_RATE;

public class ClientUpdateSender extends TickerThread {

    private GameCore gameCore;

    private Socket socket;
    private ObjectOutputStream outputStream;

    private GameUpdatePacket gup;

    public ClientUpdateSender(Socket socket, ObjectOutputStream objectOutputStream, GameCore game){
        super(SERVER_UPDATE_RATE, false, null);
        this.socket = socket;
        this.gameCore = game;
        this.outputStream = objectOutputStream;
        try {
            outputStream.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

        gup = new GameUpdatePacket();
    }


    @Override
    protected void tick() {
        gup.setPlayer(gameCore.getPlayer());
        try {
            outputStream.writeUnshared(gup);
        } catch (IOException e) {
            e.printStackTrace();
            hault();
        }
    }
}
