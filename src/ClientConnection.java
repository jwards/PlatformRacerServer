
import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlayerController;
import jsward.platformracer.common.network.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection extends Thread {

    private final Socket connection;
    private final int connectionID;

    private GameSessionManager gameSessionManager;

    private ClientInputReciever clientInputReciever;
    private ClientUpdateSender clientUpdateSender;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public ClientConnection(GameSessionManager gameSessionManager, Socket connection,int id){
        this.gameSessionManager = gameSessionManager;
        this.connection = connection;
        this.connectionID = id;
    }

    @Override
    public synchronized void start() {
        super.start();
        try {
            objectInputStream = new ObjectInputStream(connection.getInputStream());
            objectOutputStream = new ObjectOutputStream(connection.getOutputStream());

            //lobby communication
            boolean inLobby = true;


            while(inLobby){
                Object obj = objectInputStream.readObject();
                System.out.println("Recieved: " + obj.toString()+ " from client: "+connection.toString());
                if(obj == null) continue;

                if(obj instanceof LobbyPacket){
                    handleLobbyUpdate();
                } else if(obj instanceof JoinGamePacket){
                    inLobby = handleJoinGame(obj);
                } else if(obj instanceof CreateGamePacket){
                    handleCreateGame();
                }
                objectOutputStream.flush();
            }


        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Error in ClientConnection: " + connection.toString());
            e.printStackTrace();
        }

    }

    public void startConnection(){
        if (clientUpdateSender != null && clientInputReciever != null) {
            clientUpdateSender.start();
            clientInputReciever.start();
        }
    }

    public int getID(){
        return connectionID;
    }

    public void createInputReciver(PlayerController controller) throws IOException {
        clientInputReciever = new ClientInputReciever(connection,objectInputStream,controller);
    }

    public void createUpdateSender(GameCore gameCore) throws IOException {
        clientUpdateSender = new ClientUpdateSender(connection,objectOutputStream, gameCore);
    }

    private boolean handleJoinGame(Object packet) throws IOException {
        //attempted to join game
        JoinGamePacket joinGamePacket = (JoinGamePacket) packet;
        Status status = gameSessionManager.joinSession(joinGamePacket.gameSessionId,this);
        //reply with status of join game request
        objectOutputStream.writeInt(status.ordinal());
        System.out.println("Sent: " + status + " in response to JoinGameReq id: "+joinGamePacket.gameSessionId);
        return status != Status.BEGIN;
    }

    private void handleCreateGame() throws IOException {
        //create game
        gameSessionManager.createSession(this);
        objectOutputStream.writeInt(Status.OK.ordinal());
    }

    private void handleLobbyUpdate() throws IOException {
        //send back a full lobby packet
        LobbyPacket lobbyPacket = new LobbyPacket(gameSessionManager.getQueuedGameInfo());
        objectOutputStream.writeUnshared(lobbyPacket);
    }

}
