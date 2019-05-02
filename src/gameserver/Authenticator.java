package gameserver;

import jsward.platformracer.common.network.LoginPacket;

import java.security.SecureRandom;

import org.apache.commons.net.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.UUID;

public class Authenticator {


    // The higher the number of iterations the more
    // expensive computing the hash is for us and
    // also for an attacker.
    private static final int iterations = 50*1000;
    private static final int saltLen = 32;
    private static final int desiredKeyLen = 256;


    //private static HashMap<String, String> users = new HashMap<>();

    private Authenticator(){

    }

    public static LoginPacket autheticate(LoginPacket packet) {

        Database db = Database.getInstance();

        String dbuid = db.getUserId(packet.getUsername());

        if(dbuid == null){
            //user doesn't exist so we create a new one
            try {
                String newId = UUID.randomUUID().toString();
                String pass = getSaltedHash(packet.getPassword());
                if(db.addUser(newId, packet.getUsername(), pass)){
                    //added new user
                    return new LoginPacket(packet.getUsername(), newId, true);
                } else {
                    //failed to add
                    return new LoginPacket("", "", false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new LoginPacket("", "", false);
            }
        }

        //username exists, check if password matches
        try {
            String storedPass = db.getPassword(packet.getUsername());
            if(check(packet.getPassword(), storedPass)){
                //success
                return new LoginPacket(packet.getUsername(), dbuid, false);
            } else {
                //wrong password
                return new LoginPacket("", "", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginPacket("", "", false);
        }
    }



    /** Computes a salted PBKDF2 hash of given plaintext password
     suitable for storing in a database.
     Empty passwords are not supported. */
    public static String getSaltedHash(String password) throws Exception {
        byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen);
        // store the salt with the password
        return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
    }

    /** Checks whether given plaintext password corresponds
     to a stored salted hash of the password. */
    public static boolean check(String password, String stored) throws Exception{
        String[] saltAndHash = stored.split("\\$");
        if (saltAndHash.length != 2) {
            throw new IllegalStateException(
                    "The stored password must have the form 'salt$hash'");
        }
        String hashOfInput = hash(password, Base64.decodeBase64(saltAndHash[0]));
        return hashOfInput.equals(saltAndHash[1]);
    }

    // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
    // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
    private static String hash(String password, byte[] salt) throws Exception {
        if (password == null || password.length() == 0)
            throw new IllegalArgumentException("Empty passwords are not supported.");
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey key = f.generateSecret(new PBEKeySpec(
                password.toCharArray(), salt, iterations, desiredKeyLen));
        return Base64.encodeBase64String(key.getEncoded());
    }

}
