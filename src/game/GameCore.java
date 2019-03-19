package game;

public class GameCore {

    private Player player;
    private PlayerController playerController;

    public GameCore(PlatformLevel platformLevel){
        player = new Player(0, 1, 1f,6f,3f,-15f);
        playerController = new PlayerController(platformLevel, player);
    }

    public PlayerController getPlayerController(){
        return playerController;
    }

    public Player getPlayer(){
        return player;
    }

    public void tick(){
       // playerController.update();
    }


}
