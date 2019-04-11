import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlatformLevel;
import jsward.platformracer.common.network.GameSessionInfo;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.util.ArrayList;

import static jsward.platformracer.common.util.Constants.GAME_LOOP_MAX_TPS;
import static jsward.platformracer.common.util.Constants.PLAYERS_PER_GAME;

public class GameSession extends TickerThread {

    private int sessionId;

    private GameCore gameCore;

    private ArrayList<ClientConnection> clients;
    private ClientConnection host;


    public GameSession(int sessionId,ClientConnection host){
        super(GAME_LOOP_MAX_TPS,false,null);
        this.sessionId = sessionId;
        this.host = host;
        clients = new ArrayList<>();
        gameCore = new GameCore(new PlatformLevel());
        addClient(host);
    }

    public boolean addClient(ClientConnection client){
        if(client == null) return false;
        if(clients.contains(client)) return false;
        clients.add(client);
        try {
            client.createInputReciver(gameCore.getPlayerController());
            client.createUpdateSender(gameCore);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    public int getSessionId(){
        return sessionId;
    }

    public GameSessionInfo getInfo(){
        if(host!=null){
            return new GameSessionInfo(sessionId,host.getName(),clients.size(),PLAYERS_PER_GAME);
        } else {
            return new GameSessionInfo(sessionId,"EMPTY",clients.size(),PLAYERS_PER_GAME);
        }
    }


}
