package gameserver;

import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.Player;
import jsward.platformracer.common.game.PlayerController;
import jsward.platformracer.common.network.GameUpdatePacket;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.ObjIntConsumer;

import static jsward.platformracer.common.util.Constants.SERVER_UPDATE_RATE;

public class GameCommunication extends TickerThread {

    private GameCore gameCore;

    private PlayerController playerController;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientId;

    private int counter = 0;

    private GameUpdatePacket gup;

    public GameCommunication(Socket socket, ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream, GameCore game, String clientId){
        super(SERVER_UPDATE_RATE, false, null);
        this.gameCore = game;
        this.clientId = clientId;
        this.outputStream = objectOutputStream;
        this.inputStream = objectInputStream;

        playerController = gameCore.getPlayerController(clientId);

        try {
            outputStream.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

        gup = new GameUpdatePacket();
        counter = 0;
    }

    public void sendPlayerInfo(){
        gup.players = gameCore.getAllPlayers();
        try {
            outputStream.reset();
            outputStream.writeUnshared(gup);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            hault();
        }
    }

    public void receiveControls(){
        try {
            long obj = inputStream.readLong();
            playerController.setControlsActive(obj);
            //System.out.println(obj);
        } catch (IOException e) {
            e.printStackTrace();
            hault();
        }
    }

    @Override
    protected void tick() {
        receiveControls();
        sendPlayerInfo();
    }
}
