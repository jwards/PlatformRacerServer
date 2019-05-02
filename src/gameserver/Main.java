package gameserver;

public class Main {


    public static void main(String[] args) {
        System.out.println("Hello World!");
        ServerThread st = new ServerThread();
       // LoginServer ls = new LoginServer();
        st.start();
      //  ls.start();

        //connect to database
        Database.getInstance().connect();
    }
}
