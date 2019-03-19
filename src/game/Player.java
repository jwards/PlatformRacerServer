package game;



public class Player {

    private float x;
    private float y;
    private float vx;
    private float vy;
    private float accel;
    private float maxFallSpeed;
    private float maxJumpSpeed;
    private float maxSpeedX;
    private boolean canJump;



    public Player(float x,float y,float accel,float maxSpeedX,float maxFallSpeed,float maxJumpSpeed){
        this.accel = accel;
        this.maxSpeedX = maxSpeedX;
        this.maxFallSpeed = maxFallSpeed;
        this.maxJumpSpeed = maxJumpSpeed;
        this.x=x;
        this.y=y;
    }

    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }

    public float getVx(){
        return vx;
    }

    public float getVy(){
        return vy;
    }

    public float getAccel(){
        return accel;
    }

    public void setPosition(float x,float y){
        this.x=x;
        this.y=y;
    }

    public void setVx(float vx) {
        this.vx = bound(vx,-maxSpeedX,maxSpeedX);
    }

    public void setVy(float vy){
        this.vy = bound(vy, maxJumpSpeed, maxFallSpeed);
    }

    public boolean isMoving(){
        return vx!=0;
    }

    public boolean isFalling(){
        return vy>2;
    }

    public void setCanJump(boolean val){
        canJump = val;
    }

    public boolean canJump(){
        return canJump;
    }

    private float bound(float test,float lb,float ub){
        if(test<lb){
            return lb;
        }
        if(test>ub){
            return ub;
        }
        return test;
    }

}
