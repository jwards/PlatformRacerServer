package gameserver;

import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlayerController;
import jsward.platformracer.common.network.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection extends Thread {

    private final Socket connection;
    private final String connectionID;
    private String clientUserId;

    private GameSessionManager gameSessionManager;

    private ClientInputReciever clientInputReciever;
    private ClientUpdateSender clientUpdateSender;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    private boolean inGame;

    public ClientConnection(GameSessionManager gameSessionManager, Socket connection,String id){
        this.gameSessionManager = gameSessionManager;
        this.connection = connection;
        this.connectionID = id;
    }

    @Override
    public synchronized void run() {
        try {
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectOutputStream.flush();

            //first thing to receive is the client's ID
            clientUserId = (String) objectInputStream.readObject();


            //lobby communication
            inGame = false;


            while(!inGame){
                Object obj = objectInputStream.readObject();
                System.out.println("Recieved: " + obj.toString()+ " from client: "+connection.toString());
                if(obj == null) continue;

                if(obj instanceof LobbyPacket){
                    handleLobbyUpdate(obj,inGame);
                } else if(obj instanceof JoinGamePacket){
                    handleJoinGame(obj);
                } else if(obj instanceof CreateGamePacket){
                    handleCreateGame();
                } else if(obj instanceof StartGamePacket){
                    //send start game to all other clients
                    //this is done through the lobby update packets except for the host
                    handleStartGame();
                }
                objectOutputStream.flush();
            }

        }catch (IOException e){
            e.printStackTrace();
            gameSessionManager.disconnect(this);
        } catch (ClassNotFoundException e) {
            System.out.println("Error in ClientConnection: " + connection.toString());
            e.printStackTrace();
        }
    }

    //called when a game lobby that this client is in is started
    //not called when this client is the host of the game
    public void onStartGame(){
        inGame = true;
    }

    public void startConnection(){
        if (clientUpdateSender != null && clientInputReciever != null) {
            clientUpdateSender.start();
            clientInputReciever.start();
        }
    }

    public String getClientId(){
        return connectionID;
    }

    public void createInputReciver(PlayerController controller) throws IOException {
        clientInputReciever = new ClientInputReciever(connection,objectInputStream,controller);
    }

    public void createUpdateSender(GameCore gameCore) throws IOException {
        clientUpdateSender = new ClientUpdateSender(connection,objectOutputStream, gameCore);
    }

    public String getPlayerName(){
        return connection.getInetAddress().getHostAddress();
    }

    //handles both joining and leaving game lobbies
    private void handleJoinGame(Object packet) throws IOException {
        //attempted to join game
        JoinGamePacket joinGamePacket = (JoinGamePacket) packet;

        //if session id = -1 then its a leave game request
        if(joinGamePacket.gameSessionId == -1){
            //leave game
            gameSessionManager.disconnect(this);
            JoinGamePacket response = new JoinGamePacket(joinGamePacket.gameSessionId);
            response.status = Status.OK;
            objectOutputStream.writeUnshared(response);
            System.out.println("Sent: " + response.status+ " in response to JoinGameReq id: " + joinGamePacket.gameSessionId);
        } else {
            //join game
            Status status = gameSessionManager.joinSession(joinGamePacket.gameSessionId, this);

            //reply with status of join game request
            JoinGamePacket response = new JoinGamePacket(joinGamePacket.gameSessionId);
            response.status = status;
            objectOutputStream.writeUnshared(response);
            System.out.println("Sent: " + status + " in response to JoinGameReq id: " + joinGamePacket.gameSessionId);
        }
    }

    private void handleCreateGame() throws IOException {
        //create game
        int sessionId = gameSessionManager.createSession(this);

        JoinGamePacket response = new JoinGamePacket(sessionId);
        response.status = Status.OK;
        objectOutputStream.writeUnshared(response);
    }

    private void handleLobbyUpdate(Object packet,boolean inGame) throws IOException {
        //if the game has started, send back a lobby packet that signifies it
        LobbyPacket lobbyReq = (LobbyPacket) packet;
        LobbyPacket response;
        if(!inGame) {
            //game hasn't started, send back normal lobby update
            if (lobbyReq.getLobbyId() == -1) {
                response = new LobbyPacket(gameSessionManager.getQueuedGameInfo(), -1);
            } else {
                //find specific lobby
                response = new LobbyPacket(gameSessionManager.getQueuedGameInfo(lobbyReq.getLobbyId()), lobbyReq.getLobbyId());
            }
        } else {
            //tell client the game has started
            response = new LobbyPacket(null, -2);
        }
        objectOutputStream.writeUnshared(response);
    }

    private void handleStartGame() throws IOException {
        StartGamePacket response;

        if(gameSessionManager.startGame(this)){
            //game was started
            response = new StartGamePacket(Status.OK);
        } else {
            //can't start game
            response = new StartGamePacket(Status.BAD);
        }

        objectOutputStream.writeUnshared(response);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null){
            if(obj instanceof ClientConnection){
                ClientConnection other = (ClientConnection) obj;
                return this.getClientId().equals(other.getClientId());
            }
        }
        return false;
    }
}
