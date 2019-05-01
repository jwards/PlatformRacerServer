package gameserver;

import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.HighScore;
import jsward.platformracer.common.game.PlayerController;
import jsward.platformracer.common.network.*;
import jsward.platformracer.common.util.UserInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection extends Thread {

    private final Socket connection;

    private UserInfo clientInfo;

    private GameSessionManager gameSessionManager;

    private GameCommunication gameCommunication;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    //true when the player is in the game
    private boolean inGame;

    //true when the game has started but the client hasn't been told to start yet
    private boolean enteringGame;



    public ClientConnection(GameSessionManager gameSessionManager, Socket connection){
        this.gameSessionManager = gameSessionManager;
        this.connection = connection;
        enteringGame = false;
        inGame = false;
    }

    @Override
    public synchronized void run() {
        try {
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
            objectOutputStream.flush();

            //try to authenticate client
            boolean authenticated = false;
            while(!authenticated){
                Object obj = objectInputStream.readObject();
                if(obj instanceof LoginPacket) {
                    LoginPacket reply = Authenticator.autheticate((LoginPacket) obj);
                    authenticated = reply.isValid();
                    if(authenticated){
                        clientInfo = new UserInfo(reply.getUserid(), reply.getUsername());
                    }
                    objectOutputStream.writeUnshared(reply);
                } else {
                    //user needs to authenticate before handling of other requests
                    objectOutputStream.writeUnshared(new LoginPacket("", "", false));
                }
                objectOutputStream.flush();
            }




            //lobby communication loop
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
                } else if (obj instanceof LeaderBoardPacket) {
                    handleLeaderboard();
                }
                objectOutputStream.flush();
            }

            //ingame communication loop
            if(inGame){
                //first send the client the game info
                gameCommunication.sendPlayerInfo();

                //tell game session we are ready to start
                //when all clients have signaled ready, the game begins
                gameSessionManager.getSession(getClientId()).signalReady(getClientId());

                //begin the game
                startConnection();
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
    //after this is called, on the next lobby update request, the server
    //will send the client a start game packet instread of a lobbypacket
    //when the client receives the packet they will then enter the game.
    public void onStartGame(){
        inGame = true;
        enteringGame = true;
    }

    public void startConnection(){
        if (gameCommunication != null) {
            gameCommunication.start();
            try {
                gameCommunication.join();
            } catch (InterruptedException e) {

            }

        }
    }

    public String getClientId(){
        if (clientInfo == null) {
            return "unknown";
        } else {
            return clientInfo.getId();
        }
    }

    public String getClientName(){
        if (clientInfo == null) {
            return "unknown";
        } else {
            return clientInfo.getId();
        }
    }

    public UserInfo getClientInfo(){
        if (clientInfo == null) {
            return new UserInfo("unknown", "unknown");
        } else {
            return clientInfo;
        }
    }


    public void setupGameCommunication(GameCore gameCore) throws IOException {
        gameCommunication = new GameCommunication(connection,objectOutputStream,objectInputStream, gameCore,getClientId());
    }

    public String getPlayerName(){
        return connection.getInetAddress().getHostAddress();
    }

    private void handleLeaderboard() throws IOException {

        //TODO get values from db
        ArrayList<HighScore> score = new ArrayList<>();
        score.add(new HighScore(new UserInfo("he was number one", "manjenkins"), 123));

        LeaderBoardPacket response = new LeaderBoardPacket();

        response.scores = score;

        objectOutputStream.writeUnshared(response);
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
            //todo instead of sending index, send id
            //this could cause an index out of bounds if the client and server lobby lists are out of sync
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
        enteringGame = false;
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
