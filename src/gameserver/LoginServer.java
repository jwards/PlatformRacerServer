package gameserver;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;

import static jsward.platformracer.common.util.Constants.LOGIN_PORT;

public class LoginServer extends Thread {

    //TODO use db
    private HashMap<String, String> users = new HashMap<>();

    public LoginServer() {

    }

    // Create the and initialize the SSLContext
    private SSLContext createSSLContext(){
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("servertruststore.jks"),"pracer10".toCharArray());

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,  tm, new SecureRandom());

            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public void run() {
        try{
            SSLContext sslContext = this.createSSLContext();


            SSLServerSocketFactory factory=sslContext.getServerSocketFactory();
            SSLServerSocket sslserversocket=(SSLServerSocket) factory.createServerSocket(LOGIN_PORT);

            while(true) {
                SSLSocket sslsocket = (SSLSocket) sslserversocket.accept();

                System.out.println(sslsocket.toString()+" connected...");
                try {
                    new LoginHandler(sslsocket, users).start();
                } catch (IOException e) {

                }

            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
