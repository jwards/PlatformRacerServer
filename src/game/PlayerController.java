package  game;

public class PlayerController {

    private static final String DEBUG_TAG = "PLAYER_CONTROLLER";


    public static final long BUTTON_NULL = 0x0;
    public static final long BUTTON_LEFT = 0x1;
    public static final long BUTTON_RIGHT = 0x2;
    public static final long BUTTON_JUMP = 0x4;

    private final float JUMP_SPEED = -10f;
    private final float GRAVITY = 2f;

    private PlatformLevel level;
    private Player p;

    public PlayerController(PlatformLevel level, Player p){
        this.level = level;
        this.p = p;
    }

    public void update(long controls){

        p.setVy(p.getVy()+GRAVITY);

        if ((controls & BUTTON_JUMP) != 0) {
            jump();
        }

        if ((controls & BUTTON_LEFT) != 0) {
            move(-1);
        } else if ((controls & BUTTON_RIGHT) != 0) {
            move(1);
        } else {
            //decelerate if not moving
            p.setVx(decay(p.getVx()));
        }


        //update position
        level.detectCollision(p);

    }


    private void jump(){
        if (p.canJump()) {
            p.setVy(JUMP_SPEED);

            //canJump is reset back to true when a collision with ground is detected
            p.setCanJump(false);
        }
    }

    //1 for positive x, -1 for negative x
    private void move(int direction){
        p.setVx(p.getAccel()*direction+p.getVx());
    }


    private float decay(float val){
        if (Math.abs(val) < 0.5) {
            return 0;
        } else {
            return val * 0.8f;
        }
    }

}
