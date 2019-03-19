import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static java.lang.Thread.sleep;

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
            //input = new ObjectInputStream(connection.getInputStream());
            output.flush();

            while(true) {
                output.writeObject(new GameUpdatePacket());
                System.out.println("Sending Object...");
                sleep(1000);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
