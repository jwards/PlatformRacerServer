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

    //returns true when this gamesession should be deleted
    public boolean removeClient(ClientConnection client){
        if (client.getClientId() == host.getClientId()) {
            //if there are any other players in the lobby, promote one of them to host
            if(clients.size()>1){
                //promote other player
                for (ClientConnection cc : clients) {
                    if (!cc.getClientId().equals(client.getClientId())) {
                        host = cc;
                        break;
                    }
                }
            }
        }
        //remove the client
        removeClient(client.getClientId());

        //if the lobby is empty, remove it
        return clients.size() == 0;
    }

    private void removeClient(String clientId){
        Iterator<ClientConnection> members = clients.iterator();
        while(members.hasNext()){
            ClientConnection c = members.next();
            if (c.getClientId().equals(clientId)) {
                members.remove();
                return;
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
            names.add(cc.getPlayerName());
        }

        return new GameSessionInfo(sessionId,names.indexOf(host.getPlayerName()),clients.size(),PLAYERS_PER_GAME,names);
    }


}