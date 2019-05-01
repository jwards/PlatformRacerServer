package gameserver;

import jsward.platformracer.common.network.LoginPacket;

import java.util.HashMap;
import java.util.UUID;

public class Authenticator {


    private static HashMap<String, String> users = new HashMap<>();

    private Authenticator(){

    }

    public static LoginPacket autheticate(LoginPacket packet){
        if (users.containsKey(packet.getUsername())) {
            if (users.get(packet.getUsername()).equals(packet.getPassword())) {
                System.out.println("Found User");
                return new LoginPacket(packet.getUsername(),UUID.randomUUID().toString(), false);
            } else {
                return new LoginPacket("", "", false);
            }
        } else {
            System.out.println("Creating new users");
            users.put(packet.getUsername(), packet.getPassword());
            return new LoginPacket(packet.getUsername(),UUID.randomUUID().toString(), true);
        }
    }

}
