package gameserver;

import jsward.platformracer.common.game.HighScore;
import jsward.platformracer.common.util.Constants;
import jsward.platformracer.common.util.UserInfo;

import java.sql.*;
import java.util.ArrayList;

public class Database {

    private final static String user = "jsward";
    private final static String dbUrl = "jdbc:mysql://" + Constants.SERVER_ADDR +":3306/pracer";
    private final static String password = "pracer10";

    private static Database instance;

    private static Connection connection;

    public synchronized static Database getInstance() {
        if(instance == null){
            instance = new Database();
        }
        return instance;
    }

    private Database(){

    }

    public boolean connect(){
        try {
            // Connect to the database
            connection = DriverManager.getConnection(dbUrl, user, password);
            System.out.println("*** Connected to the database ***");

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            return false;
        }
        return true;
    }

    //returns null if user doesn't exist
    public synchronized String getUserId(String username){
        String result = null;
        try {
            Statement s = connection.prepareStatement("select u.userid from users u where u.username = ?");
            ((PreparedStatement) s).setString(1, username);
            ResultSet results = ((PreparedStatement) s).executeQuery();
            if (results.next()) {
                result = results.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized String getPassword(String username){
        String result = null;
        try {
            Statement s = connection.prepareStatement("select u.pass from users u where u.username = ?");
            ((PreparedStatement) s).setString(1, username);
            ResultSet results = ((PreparedStatement) s).executeQuery();
            if (results.next()) {
                result = results.getString(1);
            }
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized boolean addUser(String id,String name,String pass){
        try {
            Statement s = connection.prepareStatement("insert into users (userid, username, pass) values (?,?,?)");
            ((PreparedStatement) s).setString(1, id);
            ((PreparedStatement) s).setString(2, name);
            ((PreparedStatement) s).setString(3, pass);
            ((PreparedStatement) s).executeUpdate();
            s.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized String getUsername(String userid){
        String result = null;
        try {
            Statement s = connection.prepareStatement("select u.username from users u where u.userid= ?");
            ((PreparedStatement) s).setString(1, userid);
            ResultSet results = ((PreparedStatement) s).executeQuery();
            if(results.next()) {
                result = results.getString(1);
            }
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized ArrayList<HighScore> getScores(){
        ArrayList<HighScore> highScores = new ArrayList<>();

        try{
            Statement s = connection.createStatement();
            ResultSet scoreSet = s.executeQuery("select u.username,u.userid, s.score from highscore s, users u where u.userid = s.userid order by s.score asc limit 50");
            while (scoreSet.next()) {
                String name = scoreSet.getString(1);
                String id = scoreSet.getString(2);
                long score = scoreSet.getLong(3);
                highScores.add(new HighScore(new UserInfo(id, name), score));
            }
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return highScores;
    }

    public synchronized void addScore(String userid,long score){
        try{
            Statement s = connection.prepareStatement("insert into highscore (userid,score) values (?,?)");
            ((PreparedStatement) s).setString(1, userid);
            ((PreparedStatement) s).setLong(2, score);
            ((PreparedStatement) s).executeUpdate();
        } catch (SQLException e) {

        }
    }




    public void close(){
        if(connection!=null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection = null;
        }
    }




}
