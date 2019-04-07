import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.network.GameUpdatePacket;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static jsward.platformracer.common.util.Constants.SERVER_UPDATE_RATE;

public class ClientUpdateSender extends TickerThread {

    private GameCore gameCore;

    private Socket socket;
    private ObjectOutputStream outputStream;

    private GameUpdatePacket gup;

    public ClientUpdateSender(Socket socket, GameCore game) throws IOException {
        super(SERVER_UPDATE_RATE, false, null);
        this.socket = socket;
        this.gameCore = game;

        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();

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
