package gameserver;

import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlatformLevel;
import jsward.platformracer.common.network.GameSessionInfo;
import jsward.platformracer.common.util.TickerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

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

    public void removeClient(ClientConnection client){
        if (client.getClientId() == host.getClientId()) {
            //TODO disband lobby or promote other client to host
        } else {
            Iterator<ClientConnection> members = clients.iterator();
            while(members.hasNext()){
                ClientConnection c = members.next();
                if (c.getClientId().equals(client.getClientId())) {
                    members.remove();
                }
            }
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
        ArrayList<String> names = new ArrayList<>();

        for (ClientConnection cc : clients) {
            names.add(cc.getName());
        }

        return new GameSessionInfo(sessionId,names.indexOf(host.getName()),clients.size(),PLAYERS_PER_GAME,names);
    }


}
