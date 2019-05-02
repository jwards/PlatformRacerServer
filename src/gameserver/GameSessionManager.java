package gameserver;


import jsward.platformracer.common.network.GameSessionInfo;
import jsward.platformracer.common.network.Status;

import java.util.ArrayList;
import java.util.Iterator;

public class GameSessionManager {


    private ArrayList<GameSession> queuedSessions;

    private int sessionCounter = 1;

    public GameSessionManager(){
        queuedSessions = new ArrayList<>();
    }

    public synchronized int createSession(ClientConnection client){
        int sessionId = sessionCounter;
        sessionCounter++;

        GameSession session = new GameSession(sessionId,client,this);
        queuedSessions.add(session);
        System.out.println("Adding Client: " + client.toString() + " to new session " + session.toString());
        return sessionId;
    }

    public synchronized Status joinSession(int sessionId, ClientConnection client){
        Status status = Status.OK;
        GameSession gameSession = getSession(sessionId);
        System.out.println("Client: " + client.toString() + " attempting to join session: "+gameSession.getInfo().toString());
        if(gameSession == null){
            System.out.println("Error: client: "+client.toString() + " attempted to join session: "+sessionId+" . Session not found.");
            return Status.BAD;
        }

        //attempt to add client to game session
        if(!gameSession.addClient(client)){
            System.out.println("Error: client: " + client.toString() + " unable to join game session: " + sessionId);
            return Status.BAD;
        }

        System.out.println("Client: " + client.toString() + " joined game session: " + gameSession.getInfo().toString());
        return status;
    }

    public synchronized void disconnect(ClientConnection client){
        Iterator<GameSession> iter = queuedSessions.iterator();
        while (iter.hasNext()) {
            GameSession gs = iter.next();
            //attempt to remove client from any queued game session
            if(gs.removeClient(client)){
                //remove the lobby
                iter.remove();
                return;
            }
        }
    }

    private GameSession getSession(int sessionId){
        for(GameSession gs:queuedSessions){
            if(gs.getSessionId() == sessionId){
                return gs;
            }
        }
        return null;
    }

    public GameSession getSession(String clientId){
        for(GameSession gs:queuedSessions){
            if (gs.isMember(clientId)) {
                return gs;
            }
        }
        return null;
    }

    public ArrayList<GameSessionInfo> getQueuedGameInfo(){
        ArrayList<GameSessionInfo> infos = new ArrayList<>();
        for(GameSession gs: queuedSessions){
            infos.add(gs.getInfo());
        }
        return infos;
    }

    public ArrayList<GameSessionInfo> getQueuedGameInfo(int lobbyId){
        ArrayList<GameSessionInfo> info = new ArrayList<>();
        GameSession lobby = getSession(lobbyId);
        if(lobby!=null){
            info.add(lobby.getInfo());
        }
        return info;
    }

    public boolean startGame(ClientConnection client){
        GameSession session = getSession(client.getClientId());
        if(session != null) {
            return session.startGame(client.getClientId());
        }
        return false;
    }

    public synchronized void closeSession(GameSession session) {
        queuedSessions.remove(session);
    }
}
