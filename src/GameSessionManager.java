import java.util.ArrayList;

public class GameSessionManager {

    private ArrayList<GameSession> activeSessions;

    private GameSession queuedSession;

    public GameSessionManager(){
        activeSessions = new ArrayList<>();
    }

    public void addClient(ClientConnection client){
        if(queuedSession == null){
            createSession(client);
        }

        queuedSession.addClient(client);
        System.out.println("Adding Client: " + client.toString() + " to session " + queuedSession.toString());
        if (queuedSession.ready()) {
            System.out.println("Session " + queuedSession.toString() + " ready. Launching session...");
            activeSessions.add(queuedSession);
            queuedSession.start();
            queuedSession = null;
        }
    }

    private void createSession(ClientConnection client){
        queuedSession = new GameSession();
        System.out.println("Creating new session... " + queuedSession.toString());
    }


}
