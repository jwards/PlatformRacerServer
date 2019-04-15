
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

    private GameSessionManager gameSessionManager;

    private ClientInputReciever clientInputReciever;
    private ClientUpdateSender clientUpdateSender;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public ClientConnection(GameSessionManager gameSessionManager, Socket connection,String id){
        this.gameSessionManager = gameSessionManager;
        this.connection = connection;
        this.connectionID = id;
    }

    @Override
    public void run() {
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
                    handleLobbyUpdate(obj);
                } else if(obj instanceof JoinGamePacket){
                    inLobby = handleJoinGame(obj);
                } else if(obj instanceof CreateGamePacket){
                    handleCreateGame();
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

    private boolean handleJoinGame(Object packet) throws IOException {
        //attempted to join game
        JoinGamePacket joinGamePacket = (JoinGamePacket) packet;
        Status status = gameSessionManager.joinSession(joinGamePacket.gameSessionId,this);

        //reply with status of join game request
        JoinGamePacket response = new JoinGamePacket(joinGamePacket.gameSessionId);
        response.status = status;
        objectOutputStream.writeObject(response);
        System.out.println("Sent: " + status + " in response to JoinGameReq id: "+joinGamePacket.gameSessionId);
        return status != Status.BEGIN;
    }

    private void handleCreateGame() throws IOException {
        //create game
        int sessionId = gameSessionManager.createSession(this);

        JoinGamePacket response = new JoinGamePacket(sessionId);
        response.status = Status.OK;
        objectOutputStream.writeObject(response);
    }

    private void handleLobbyUpdate(Object packet) throws IOException {
        //send back a full lobby packet
        LobbyPacket lobbyReq = (LobbyPacket) packet;

        LobbyPacket response;
        if(lobbyReq.getLobbyId() == -1) {
            response = new LobbyPacket(gameSessionManager.getQueuedGameInfo(),-1);
        } else {
            //find specific lobby


            response = new LobbyPacket(gameSessionManager.getQueuedGameInfo(lobbyReq.getLobbyId()), lobbyReq.getLobbyId());
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
