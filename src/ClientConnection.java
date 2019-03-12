import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnection implements Runnable{

    private final Socket connection;
    private final int connectionID;
    private ObjectInputStream input;
    private ObjectOutputStream output;



    public ClientConnection(Socket connection,int id){
        this.connection =connection;
        this.connectionID = id;
    }



    @Override
    public void run() {

        try {
            //setup streams
            output = new ObjectOutputStream(connection.getOutputStream());
            input = new ObjectInputStream(connection.getInputStream());
            output.flush();
            while(true){
                //send
                //recieve
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
