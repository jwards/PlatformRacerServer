
import jsward.platformracer.common.game.GameCore;
import jsward.platformracer.common.game.PlayerController;

import java.io.IOException;
import java.net.Socket;

public class ClientConnection {

    private final Socket connection;
    private final int connectionID;

    private ClientInputReciever clientInputReciever;
    private ClientUpdateSender clientUpdateSender;

    public ClientConnection(Socket connection,int id){
        this.connection = connection;
        this.connectionID = id;
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
        clientInputReciever = new ClientInputReciever(connection,controller);
    }

    public void createUpdateSender(GameCore gameCore) throws IOException {
        clientUpdateSender = new ClientUpdateSender(connection, gameCore);
    }
}
