import jsward.platformracer.common.network.GameSessionInfo;
import jsward.platformracer.common.network.Status;

import java.util.ArrayList;

public class GameSessionManager {

    private ArrayList<GameSession> activeSessions;

    private ArrayList<GameSession> queuedSessions;

    private int sessionCounter = 1;

    public GameSessionManager(){
        activeSessions = new ArrayList<>();
        queuedSessions = new ArrayList<>();
    }

    public synchronized int createSession(ClientConnection client){
        int sessionId = sessionCounter;
        sessionCounter++;

        GameSession session = new GameSession(sessionId,client);
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
        /*
        if(gameSession.ready()){
            System.out.println("Starting session: "+gameSession.getInfo().toString());
            gameSession.start();
            status = Status.BEGIN;
            activeSessions.add(gameSession);
            queuedSessions.remove(gameSession);
        }
        */
        System.out.println("Client: " + client.toString() + " joined game session: " + gameSession.getInfo().toString());
        return status;
    }

    private GameSession getSession(int sessionId){
        for(GameSession gs:queuedSessions){
            if(gs.getSessionId() == sessionId){
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

}
