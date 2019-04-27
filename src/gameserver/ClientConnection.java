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

    //true when the player is in the game
    private boolean inGame;

    //true when the game has started but the client hasn't been told to start yet
    private boolean enteringGame;



    public ClientConnection(GameSessionManager gameSessionManager, Socket connection,String id){
        this.gameSessionManager = gameSessionManager;
        this.connection = connection;
        this.connectionID = id;
        enteringGame = false;
        inGame = false;
    }

    @Override
    public synchronized void run() {
        try {
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectOutputStream.flush();

            //first thing to receive is the client's ID
            clientUserId = (String) objectInputStream.readObject();


            while(!inGame || enteringGame){
                Object obj = objectInputStream.readObject();
                System.out.println("Recieved: " + obj.toString()+ " from client: "+connection.toString());
                if(obj == null) continue;

                if(obj instanceof LobbyPacket){
                    if(enteringGame){
                        //tell the client to begin the game
                        sendStartGame();
                    } else {
                        //normal lobby update
                        handleLobbyUpdate(obj, inGame);
                    }
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

            //start the game
            objectOutputStream.writeUnshared(new StartGamePacket(Status.OK));



        }catch (IOException e){
            e.printStackTrace();
            gameSessionManager.disconnect(this);
        } catch (ClassNotFoundException e) {
            System.out.println("Error in ClientConnection: " + connection.toString());
            e.printStackTrace();
        }
    }

    //called when a game lobby that this client is in is started
    //after this is called, on the next lobby update request, the server
    //will send the client a start game packet instread of a lobbypacket
    //when the client receives the packet they will then enter the game.
    public void onStartGame(){
        inGame = true;
        enteringGame = true;
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

        CreateGamePacket response = new CreateGamePacket(sessionId,Status.OK);
        response.status = Status.OK;
        objectOutputStream.writeUnshared(response);
    }

    private void handleLobbyUpdate(Object packet,boolean inGame) throws IOException {
        //if the game has started, send back a lobby packet that signifies it
        LobbyPacket lobbyReq = (LobbyPacket) packet;
        LobbyPacket response;

        if (lobbyReq.getLobbyId() == -1) {
            //return list of lobbies
            response = new LobbyPacket(gameSessionManager.getQueuedGameInfo(), -1);
        } else {
            //find specific lobby
            response = new LobbyPacket(gameSessionManager.getQueuedGameInfo(lobbyReq.getLobbyId()), lobbyReq.getLobbyId());
        }
        objectOutputStream.writeUnshared(response);
    }

    private void handleStartGame() throws IOException {
        StartGamePacket response;

        //if the game can be started the client will be notified.
        //this case is handled above and takes place for all clients

        //however the client still expects a response, so we send something
        if(!gameSessionManager.startGame(this)){
            //tell the client that the game can't be started
            response = new StartGamePacket(Status.BAD);
        } else {
            response = new StartGamePacket(Status.OK);
        }
        objectOutputStream.writeUnshared(response);
    }

    private void sendStartGame() throws IOException {
        objectOutputStream.writeUnshared(new StartGamePacket(Status.OK));
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
