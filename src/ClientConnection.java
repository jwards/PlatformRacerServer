
import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlayerController;
import jsward.platformracer.common.network.CreateGamePacket;
import jsward.platformracer.common.network.JoinGamePacket;
import jsward.platformracer.common.network.LobbyPacket;
import jsward.platformracer.common.network.Status;

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
                if(obj == null) continue;

                if(obj instanceof LobbyPacket){
                    //send back a full lobby packet
                    LobbyPacket lobbyPacket = new LobbyPacket(gameSessionManager.getQueuedGameInfo());
                    objectOutputStream.writeUnshared(lobbyPacket);
                } else if(obj instanceof JoinGamePacket){
                    //attempted to join game
                    JoinGamePacket joinGamePacket = (JoinGamePacket) obj;
                    Status status = gameSessionManager.joinSession(joinGamePacket.gameSessionId,this);

                    if(status == Status.BEGIN){
                        //begin the game
                        inLobby = false;
                    }
                    //reply with status of join game request
                    objectOutputStream.writeInt(status.ordinal());
                } else if(obj instanceof CreateGamePacket){
                    //create game
                    gameSessionManager.createSession(this);
                    objectOutputStream.writeInt(Status.OK.ordinal());
                }

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
}
