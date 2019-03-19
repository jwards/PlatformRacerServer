package game;

import java.util.ArrayList;

public class PlatformLevel {

    private static final int UP = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;

    private int sizex = 1000;
    private int sizey = 150;

    private ArrayList<RectangleF> levelObjects;
    private float[] playerHitbox = {
            4.7687864f, 1.9444445f,
            8.670521f, 1.9444445f,
            4.7687864f, 18.75f,
            8.670521f, 18.75f,
            3.1791909f, 4.305556f,
            3.1791909f, 15.138889f,
            10.260116f, 4.305556f,
            10.260116f, 15.138889f
    };

    public PlatformLevel() {
        levelObjects = new ArrayList<>();
        loadLevelObjects(lpts);
    }

    public int getHeight(){
        return sizey;
    }

    public int getWidth(){
        return sizex;
    }

    private static final int ITERATIONS= 3;

    public void detectCollision(Player p){
        boolean collisionX, collisionYBottom, collisionYTop;

        for (int i = 0; i < ITERATIONS; i++) {
            float nextdx = p.getVx();
            float nextdy = p.getVy();

            float projdx, projdy, orgdx, orgdy;

            collisionX = false;
            collisionYBottom = false;
            collisionYTop = false;

            orgdx = nextdx;
            orgdy = nextdy;

            float vectorLength;
            int segments;

            for(int obj = 0;obj<levelObjects.size()&& !collisionX && !collisionYBottom && !collisionYTop; obj++) {
                //0:up 1:down 2:left 3:right
                for (int dir = 0; dir < 4; dir++) {
                    if (dir == UP && nextdy > 0) continue;
                    if (dir == DOWN && nextdy < 0) continue;
                    if (dir == LEFT && nextdx > 0) continue;
                    if (dir == RIGHT && nextdx < 0) continue;
                    projdx = projdy = 0;

                    vectorLength = (float) Math.sqrt(nextdx * nextdx + nextdy * nextdy);
                    segments = 0;

                    while(!levelObjects.get(obj).contains(playerHitbox[dir*4] + p.getX() + projdx,playerHitbox[dir*4+1] +p.getY()+projdy)
                            && !levelObjects.get(obj).contains(playerHitbox[dir*4+2] + p.getX() + projdx,playerHitbox[dir*4+3] +p.getY()+projdy)
                            &&segments<vectorLength){
                        projdx += nextdx/vectorLength;
                        projdy += nextdy / vectorLength;
                        segments++;
                    }

                    //collision was found
                    if(segments<vectorLength){
                        if (segments > 0) {
                            projdx -= nextdx / vectorLength;
                            projdy -= nextdy / vectorLength;
                        }
                        //left right
                        if(dir>=2 && dir<=3) nextdx = projdx;
                        //up down
                        if(dir >= 0&& dir <= 1) nextdy = projdy;
                    }
                }
            }


            //detect collisions
            for (int obj = 0; obj < levelObjects.size() && !collisionX && !collisionYBottom && !collisionYTop; obj++) {
                //0:up 1:down 2:left 3:right
                for (int dir = 0; dir < 4; dir++) {
                    if (dir == DOWN && nextdy < 0) continue;
                    if (dir == LEFT && nextdx > 0) continue;
                    if (dir == RIGHT && nextdx < 0) continue;
                    //Left or Right
                    projdx = (dir >= 2 ? nextdx : 0);
                    //Up or down
                    projdy = (dir < 2 ? nextdy : 0);

                    while(levelObjects.get(obj).contains(playerHitbox[dir*4] + p.getX() + projdx,playerHitbox[dir*4+1] +p.getY()+projdy)
                        || levelObjects.get(obj).contains(playerHitbox[dir*4+2] + p.getX() + projdx,playerHitbox[dir*4+3] +p.getY()+projdy)){
                        if (dir == UP) ++projdy;
                        if(dir == DOWN) --projdy;
                        if(dir == LEFT) ++projdx;
                        if(dir == RIGHT) --projdx;
                    }

                    if(dir>= 2 && dir <= 3) nextdx = projdx;
                    if(dir>=0 && dir<=1) nextdy = projdy;
                }

                if (nextdy > orgdy && orgdy < 0) {
                    collisionYTop = true;
                }
                if (nextdy < orgdy && orgdy > 0) {
                    collisionYBottom = true;
                }
                if(Math.abs(nextdx-orgdx)>0.01f){
                    collisionX = true;
                }

                if (collisionX && collisionYTop && p.getVy() < 0) {
                    p.setVy(0);
                    nextdy = 0;
                }
            }


            //resolve collisions
            float px = p.getX(), py = p.getY();
            if(collisionYBottom || collisionYTop){
                py += nextdy;
                p.setVy(0);

                if(collisionYBottom){
                    p.setCanJump(true);
                }
            }

            if(collisionX){
                px += nextdx;
                p.setVx(0);
            }
            p.setPosition(px,py);
        }

        float px = p.getX(); float py = p.getY();
        p.setPosition(px + p.getVx(), py + p.getVy());
    }

    private void loadLevelObjects(float[] pts){
        for (int i = 0; i < pts.length; i=i+4) {
            levelObjects.add(new RectangleF(pts[i+0], pts[i+1], pts[i+2], pts[i+3]));
        }
    }

    public final static float[] lpts2 ={
            0, 0, 1000, 1,
            0, 1, 1, 150,
            999, 1, 1000, 150,
            1, 149, 999, 150
    };

    //test level
    public final static float[] lpts = {
            0, 0, 1000, 1,
            0, 1, 1, 150,
            999, 1, 1000, 150,
            642, 8, 651, 9,
            669, 8, 678, 9,
            693, 8, 704, 9,
            642, 16, 651, 17,
            669, 16, 679, 17,
            697, 16, 707, 17,
            642, 24, 651, 25,
            670, 24, 679, 25,
            698, 24, 707, 25,
            641, 32, 650, 33,
            670, 32, 680, 33,
            698, 32, 707, 33,
            641, 40, 650, 41,
            671, 40, 681, 41,
            698, 40, 708, 41,
            641, 48, 651, 49,
            672, 48, 682, 49,
            699, 48, 709, 49,
            643, 56, 652, 57,
            673, 56, 683, 57,
            700, 56, 709, 57,
            644, 64, 654, 65,
            674, 64, 683, 65,
            700, 64, 709, 65,
            310, 72, 366, 73,
            645, 72, 655, 73,
            674, 72, 683, 73,
            701, 72, 710, 73,
            288, 80, 316, 81,
            359, 80, 392, 81,
            646, 80, 656, 81,
            675, 80, 685, 81,
            702, 80, 712, 81,
            272, 88, 295, 89,
            384, 88, 412, 89,
            648, 88, 657, 89,
            676, 88, 685, 89,
            703, 88, 713, 89,
            254, 96, 278, 97,
            405, 96, 428, 97,
            648, 96, 657, 97,
            677, 96, 686, 97,
            705, 96, 714, 97,
            239, 104, 262, 105,
            421, 104, 447, 105,
            647, 104, 657, 105,
            677, 104, 686, 105,
            705, 104, 715, 105,
            228, 112, 245, 113,
            439, 112, 486, 113,
            647, 112, 656, 113,
            677, 112, 686, 113,
            706, 112, 716, 113,
            106, 120, 166, 121,
            215, 120, 233, 121,
            478, 120, 634, 121,
            647, 120, 656, 121,
            678, 120, 687, 121,
            707, 120, 716, 121,
            1, 128, 113, 129,
            160, 128, 190, 129,
            201, 128, 220, 129,
            582, 128, 626, 129,
            647, 128, 656, 129,
            678, 128, 688, 129,
            707, 128, 717, 129,
            184, 136, 207, 137,
            647, 136, 656, 137,
            679, 136, 690, 137,
            708, 136, 719, 137,
            648, 144, 659, 145,
            682, 144, 691, 145,
            712, 144, 722, 145,
            1, 149, 999, 150
    };



}
