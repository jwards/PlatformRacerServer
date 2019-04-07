import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlatformLevel;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.util.ArrayList;

import static jsward.platformracer.common.util.Constants.GAME_LOOP_MAX_TPS;
import static jsward.platformracer.common.util.Constants.PLAYERS_PER_GAME;

public class GameSession extends TickerThread {

    private GameCore gameCore;

    private ArrayList<ClientConnection> clients;


    public GameSession(){
        super(GAME_LOOP_MAX_TPS,false,null);
        clients = new ArrayList<>();
        gameCore = new GameCore(new PlatformLevel());
    }

    public void addClient(ClientConnection client){
        clients.add(client);
        try {
            client.createInputReciver(gameCore.getPlayerController());
            client.createUpdateSender(gameCore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean ready(){
        return clients.size() >= PLAYERS_PER_GAME;
    }

    @Override
    public synchronized void start() {
        for (ClientConnection c : clients) {
            c.startConnection();
        }
        super.start();
    }

    @Override
    protected void tick() {
        gameCore.tick();
    }




}
